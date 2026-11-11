package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.restaurant.GetMyRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusUseCase;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import com.mealgo.restaurantservice.api.rest.exception.GlobalExceptionHandler;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MyRestaurantControllerTest {
    @Test
    void looksUpAuthenticatedOwnerAndReturnsRestaurantOr404() throws Exception {
        var useCase = mock(GetMyRestaurantUseCase.class);
        var controller = new RestaurantController(mock(RegisterRestaurantUseCase.class), useCase,
                mock(UpdateRestaurantStatusUseCase.class),
                mock(com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressUseCase.class),
                mock(com.mealgo.restaurantservice.api.rest.mapper.RestaurantAddressApiMapper.class),
                mock(com.mealgo.restaurantservice.application.port.in.restaurant.UpdateAvatarUseCase.class),
                mock(com.mealgo.restaurantservice.api.rest.mapper.RestaurantAvatarApiMapper.class),
                mock(com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlUseCase.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(new StaticMessageSource()))
                .build();
        var ownerId = UUID.randomUUID();
        var restaurantId = UUID.randomUUID();
        var principal = new AuthenticatedUser(ownerId, "restaurant@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));
        try {
            when(useCase.getMyRestaurant(ownerId)).thenReturn(
                    new MyRestaurantResult(restaurantId, "Mạnh Phong", RestaurantStatus.CLOSED));
            mvc.perform(get("/restaurants/me").param("ownerId", UUID.randomUUID().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.restaurantId").value(restaurantId.toString()))
                    .andExpect(jsonPath("$.name").value("Mạnh Phong"))
                    .andExpect(jsonPath("$.status").value("CLOSED"));
            verify(useCase).getMyRestaurant(ownerId);

            when(useCase.getMyRestaurant(ownerId)).thenThrow(new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
            mvc.perform(get("/restaurants/me")).andExpect(status().isNotFound());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
