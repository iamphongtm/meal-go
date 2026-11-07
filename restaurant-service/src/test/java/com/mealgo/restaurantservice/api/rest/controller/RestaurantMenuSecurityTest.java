package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.menu.GetRestaurantMenuUseCase;
import com.mealgo.restaurantservice.infrastructure.config.HeaderRequestFilter;
import com.mealgo.restaurantservice.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantMenuController.class)
@Import({SecurityConfig.class, HeaderRequestFilter.class})
class RestaurantMenuSecurityTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private GetRestaurantMenuUseCase useCase;

    @Test
    void anonymousCustomersCanReadAllThreeMenuRoutes() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        mvc.perform(get("/restaurants/{id}/menu", restaurantId)).andExpect(status().isOk());
        mvc.perform(get("/restaurants/{id}/menu/tree", restaurantId)).andExpect(status().isOk());
        mvc.perform(get("/restaurants/{id}/menu-items/{itemId}", restaurantId, itemId))
                .andExpect(status().isOk());
        verify(useCase).getMenu(restaurantId);
        verify(useCase).getMenuTree(restaurantId);
        verify(useCase).getMenuItem(restaurantId, itemId);
    }

    @Test
    void anonymousCustomersCannotUseManagementRoutesOrWriteMethods() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        mvc.perform(get("/restaurants/me")).andExpect(status().isForbidden());
        mvc.perform(post("/restaurants")).andExpect(status().isForbidden());
        mvc.perform(patch("/restaurants/{id}/status", restaurantId)).andExpect(status().isForbidden());
        mvc.perform(post("/restaurants/{id}/menu", restaurantId)).andExpect(status().isForbidden());
        mvc.perform(delete("/restaurants/{id}/menu-items/{itemId}", restaurantId, UUID.randomUUID()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(useCase);
    }
}
