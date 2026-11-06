package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.restaurant.GetMyRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantUseCase;
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

class RestaurantControllerTest {
    @Test
    void updatesStatusUsingAuthenticatedOwner() throws Exception {
        var useCase = mock(com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusUseCase.class);
        var controller = new RestaurantController(mock(RegisterRestaurantUseCase.class), mock(GetMyRestaurantUseCase.class), useCase);
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
        var owner = UUID.randomUUID();
        var id = UUID.randomUUID();
        var command = new com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusCommand(
                id, owner, RestaurantStatus.CLOSED, "overload");
        when(useCase.updateStatus(command)).thenReturn(
                new com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantStatusResult(id, RestaurantStatus.CLOSED, "overload"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(owner, "owner@example.com"), null));
        try {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/restaurants/" + id + "/status")
                            .contentType("application/json").content("{\"status\":\"CLOSED\",\"pauseReason\":\"overload\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.pauseReason").value("overload"));
            verify(useCase).updateStatus(command);
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/restaurants/" + id + "/status")
                            .contentType("application/json").content("{}"))
                    .andExpect(status().isBadRequest());
        } finally { SecurityContextHolder.clearContext(); }
    }

    @Test
    void looksUpAuthenticatedOwnerAndReturnsRestaurantOr404() throws Exception {
        var useCase = mock(GetMyRestaurantUseCase.class);
        var controller = new RestaurantController(mock(RegisterRestaurantUseCase.class), useCase,
                mock(com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusUseCase.class));
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
