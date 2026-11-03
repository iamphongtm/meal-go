package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.menu.GetRestaurantMenuUseCase;
import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestaurantMenuControllerTest {
    private final GetRestaurantMenuUseCase useCase = mock(GetRestaurantMenuUseCase.class);
    private final org.springframework.test.web.servlet.MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new RestaurantMenuController(useCase)).build();

    @Test
    void exposesMenuWithoutOwnerHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.getMenu(id)).thenReturn(new RestaurantMenuResult(
                new RestaurantMenuResult.RestaurantSummary(id, "Restaurant", "Address", true, null),
                List.of()));
        mvc.perform(get("/restaurants/{id}/menu", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restaurant.restaurantId").value(id.toString()))
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    void exposesItemDetail() throws Exception {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(useCase.getMenuItem(restaurantId, itemId)).thenReturn(new MenuItemDetailResult(
                itemId, restaurantId, "Item", null, new BigDecimal("65000"), "VND", null, true, List.of()));
        mvc.perform(get("/restaurants/{id}/menu-items/{itemId}", restaurantId, itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuItemId").value(itemId.toString()))
                .andExpect(jsonPath("$.optionGroups").isArray());
    }

    @Test
    void rejectsInvalidUuid() throws Exception {
        mvc.perform(get("/restaurants/not-a-uuid/menu")).andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }
}
