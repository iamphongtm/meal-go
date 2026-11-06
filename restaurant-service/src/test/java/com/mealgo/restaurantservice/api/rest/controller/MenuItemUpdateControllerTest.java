package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.menuitem.*;
import com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlUseCase;
import com.mealgo.restaurantservice.domain.enums.MenuItemStatus;
import com.mealgo.restaurantservice.infrastructure.config.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MenuItemUpdateControllerTest {
    @Test
    void updatesDetailsAndStatusWithAuthenticatedOwnerAndRejectsInvalidBodies() throws Exception {
        var useCase = mock(UpdateMenuItemUseCase.class);
        var controller = new MenuItemController(mock(GenerateUploadUrlUseCase.class), mock(CreateMenuItemUseCase.class), useCase);
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
        var owner = UUID.randomUUID();
        var restaurant = UUID.randomUUID();
        var item = UUID.randomUUID();
        var category = UUID.randomUUID();
        String path = "/restaurants/" + restaurant + "/menu-items/" + item;
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(owner, "owner@example.com"), null));
        try {
            mvc.perform(put(path).contentType("application/json").content("""
                    {"categoryId":"%s","name":"Tea","description":"Milk tea","basePrice":35000,"imageUrl":null}
                    """.formatted(category))).andExpect(status().isNoContent());
            verify(useCase).update(new UpdateMenuItemCommand(restaurant, owner, item, category,
                    "Tea", "Milk tea", new java.math.BigDecimal("35000"), null));
            mvc.perform(patch(path + "/status").contentType("application/json").content("{\"status\":\"SOLD_OUT\"}"))
                    .andExpect(status().isNoContent());
            verify(useCase).updateStatus(new UpdateMenuItemStatusCommand(restaurant, owner, item, MenuItemStatus.SOLD_OUT));
            clearInvocations(useCase);
            mvc.perform(put(path).contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
            mvc.perform(patch(path + "/status").contentType("application/json").content("{}" )).andExpect(status().isBadRequest());
            mvc.perform(patch(path + "/status").contentType("application/json").content("{\"status\":\"INVALID\"}"))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(useCase);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
