package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantCommand;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantUseCase;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestaurantControllerTest {
    private final RegisterRestaurantUseCase useCase = mock(RegisterRestaurantUseCase.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new RestaurantController(useCase, mock(com.mealgo.restaurantservice.application.port.in.restaurant.GetMyRestaurantUseCase.class)))
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registersRestaurantUsingContextOwnerInsteadOfClientHeader() throws Exception {
        UUID ownerId = UUID.randomUUID();
        var principal = new AuthenticatedUser(ownerId, "owner@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        when(useCase.registerRestaurant(any())).thenReturn(new RestaurantId(UUID.randomUUID()));

        mvc.perform(post("/restaurants")
                        .header("X-Owner-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Restaurant",
                                  "phone": "0912345678",
                                  "provinceCode": 1,
                                  "wardCode": 1,
                                  "addressLine": "123 Main Street",
                                  "latitude": 21.0285,
                                  "longitude": 105.8542
                                }
                                """))
                .andExpect(status().isCreated());

        var command = ArgumentCaptor.forClass(RegisterRestaurantCommand.class);
        verify(useCase).registerRestaurant(command.capture());
        assertEquals(ownerId, command.getValue().ownerId());
        assertEquals(Double.valueOf(21.0285), command.getValue().latitude());
        assertEquals(Double.valueOf(105.8542), command.getValue().longitude());
    }
}
