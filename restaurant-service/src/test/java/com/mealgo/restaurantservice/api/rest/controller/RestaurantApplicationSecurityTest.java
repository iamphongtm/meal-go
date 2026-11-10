package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.mapper.ApplicationApiMapperImpl;
import com.mealgo.restaurantservice.application.port.in.restaurant.ApplicationPageResult;
import com.mealgo.restaurantservice.application.usecase.RestaurantApplicationUseCase;
import com.mealgo.restaurantservice.infrastructure.config.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({AdminRestaurantApplicationController.class, RestaurantApplicationController.class})
@Import({SecurityConfig.class, HeaderRequestFilter.class, ApplicationApiMapperImpl.class})
class RestaurantApplicationSecurityTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private RestaurantApplicationUseCase useCase;

    @Test
    void customerCannotReviewOrListApplications() throws Exception {
        String account = UUID.randomUUID().toString();
        mvc.perform(get("/admin/restaurant-applications")
                .header(Constants.HEADER_ACCOUNT_ID, account).header(Constants.HEADER_USER_ROLES, "CUSTOMER"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/admin/restaurant-applications/{id}/review", UUID.randomUUID())
                .header(Constants.HEADER_ACCOUNT_ID, account).header(Constants.HEADER_USER_ROLES, "CUSTOMER")
                .contentType("application/json").content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(useCase);
    }

    @Test
    void customerCanReadOwnApplicationWithoutRestaurantRole() throws Exception {
        UUID owner = UUID.randomUUID();
        mvc.perform(get("/restaurants/me/application")
                .header(Constants.HEADER_ACCOUNT_ID, owner).header(Constants.HEADER_USER_ROLES, "CUSTOMER"))
                .andExpect(status().isOk());
        verify(useCase).getMine(owner);
    }

    @Test
    void adminCanListAndReview() throws Exception {
        UUID admin = UUID.randomUUID();
        when(useCase.list(any())).thenReturn(new ApplicationPageResult(List.of(), 0, 20, 0));
        mvc.perform(get("/admin/restaurant-applications")
                .header(Constants.HEADER_ACCOUNT_ID, admin).header(Constants.HEADER_USER_ROLES, "ADMIN"))
                .andExpect(status().isOk()).andExpect(jsonPath("items").isEmpty());
        UUID restaurant = UUID.randomUUID();
        mvc.perform(patch("/admin/restaurant-applications/{id}/review", restaurant)
                .header(Constants.HEADER_ACCOUNT_ID, admin).header(Constants.HEADER_USER_ROLES, "ADMIN")
                .contentType("application/json").content("{\"decision\":\"APPROVED\"}"))
                .andExpect(status().isOk());
        verify(useCase).review(argThat(command -> command.adminId().equals(admin) && command.restaurantId().equals(restaurant)));
    }

}
