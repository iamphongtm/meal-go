package com.mealgo.restaurantservice.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
@Slf4j
@Component
public class HeaderRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(Constants.HEADER_ACCOUNT_ID);
        String email = request.getHeader(Constants.HEADER_USER_EMAIL);
        String roles = request.getHeader(Constants.HEADER_USER_ROLES);
        if (userId != null && roles != null) {

            AuthenticatedUser principal = new AuthenticatedUser(
                    UUID.fromString(userId),
                    email
            );

            List<GrantedAuthority> authorities =
                    Arrays.stream(roles.split(","))
                            .map(String::trim)
                            .filter(role -> !role.isBlank())
                            .map(role -> new SimpleGrantedAuthority(
                                    "ROLE_" + role
                            ))
                            .map(role -> (GrantedAuthority) role )
                            .toList();
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
