package com.mealgo.identify_service.config;

import com.mealgo.identify_service.security.ProblemDetailsAccessDeniedHandler;
import com.mealgo.identify_service.security.ProblemDetailsAuthenticationEntryPoint;
import io.apikit.apicommon.problem.ProblemProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint(
			ObjectMapper objectMapper,
			ProblemProperties problemProperties) {
		return new ProblemDetailsAuthenticationEntryPoint(objectMapper, problemProperties);
	}

	@Bean
	ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler(
			ObjectMapper objectMapper,
			ProblemProperties problemProperties) {
		return new ProblemDetailsAccessDeniedHandler(objectMapper, problemProperties);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint,
			ProblemDetailsAccessDeniedHandler accessDeniedHandler) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler));
		return http.build();
	}
}
