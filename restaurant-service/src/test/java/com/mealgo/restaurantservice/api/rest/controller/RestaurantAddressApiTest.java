package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.api.rest.mapper.RestaurantAddressApiMapperImpl;
import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import com.mealgo.restaurantservice.infrastructure.config.Constants;
import com.mealgo.restaurantservice.infrastructure.config.HeaderRequestFilter;
import com.mealgo.restaurantservice.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
@Import({SecurityConfig.class, HeaderRequestFilter.class, RestaurantAddressApiMapperImpl.class,
        com.mealgo.restaurantservice.api.rest.mapper.RestaurantAvatarApiMapperImpl.class})
class RestaurantAddressApiTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegisterRestaurantUseCase registerUseCase;

    @MockitoBean
    private GetMyRestaurantUseCase getMyRestaurantUseCase;

    @MockitoBean
    private UpdateRestaurantStatusUseCase updateStatusUseCase;

    @MockitoBean
    private UpdateRestaurantAddressUseCase updateAddressUseCase;

    @MockitoBean
    private UpdateAvatarUseCase updateAvatarUseCase;

    @MockitoBean
    private com.mealgo.restaurantservice.application.port.in.upload.GenerateUploadUrlUseCase generateUploadUrlUseCase;

    @Test
    void updatesAvatarWithAuthenticatedOwner() throws Exception {
        when(updateAvatarUseCase.updateAvatar(any())).thenReturn(
                new RestaurantAvatarResult(restaurantId, "https://cdn.example.com/avatar.jpg"));
        mvc.perform(patch("/restaurants/{id}/avatar", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "RESTAURANT")
                        .contentType("application/json")
                        .content("{\"avatarUrl\":\"https://cdn.example.com/avatar.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("avatarUrl").value("https://cdn.example.com/avatar.jpg"));
        verify(updateAvatarUseCase).updateAvatar(new UpdateAvatarCommand(
                restaurantId, ownerId, "https://cdn.example.com/avatar.jpg"));
    }

    @Test
    void customerCannotChangeAvatar() throws Exception {
        mvc.perform(patch("/restaurants/{id}/avatar", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "CUSTOMER")
                        .contentType("application/json")
                        .content("{\"avatarUrl\":\"https://cdn.example.com/avatar.jpg\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(updateAvatarUseCase);
    }

    @Test
    void rejectsMissingAvatar() throws Exception {
        mvc.perform(patch("/restaurants/{id}/avatar", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "RESTAURANT")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(updateAvatarUseCase);
    }

    private final UUID restaurantId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    private static final String VALID_BODY = """
            {"provinceCode":79,"wardCode":2,"addressLine":"New address","latitude":11,"longitude":107}
            """;

    @Test
    void mapsAuthenticatedOwnerAndReturnsAddress() throws Exception {
        when(updateAddressUseCase.updateAddress(any())).thenReturn(new RestaurantAddressResult(
                restaurantId, 79, 2, "New address", 11.0, 107.0
        ));

        mvc.perform(patch("/restaurants/{id}/address", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "RESTAURANT")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("restaurantId").value(restaurantId.toString()))
                .andExpect(jsonPath("addressLine").value("New address"))
                .andExpect(jsonPath("latitude").value(11.0));

        verify(updateAddressUseCase).updateAddress(argThat(command ->
                command.ownerId().equals(ownerId)
                        && command.restaurantId().equals(restaurantId)
                        && command.wardCode().equals(2)));
    }

    @Test
    void customerCannotChangeAddress() throws Exception {
        mvc.perform(patch("/restaurants/{id}/address", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "CUSTOMER")
                        .contentType("application/json")
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(updateAddressUseCase);
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mvc.perform(patch("/restaurants/{id}/address", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "RESTAURANT")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updateAddressUseCase);
    }

    @Test
    void rejectsInvalidCoordinatesAndBlankAddress() throws Exception {
        mvc.perform(patch("/restaurants/{id}/address", restaurantId)
                        .header(Constants.HEADER_ACCOUNT_ID, ownerId)
                        .header(Constants.HEADER_USER_ROLES, "RESTAURANT")
                        .contentType("application/json")
                        .content("""
                                {"provinceCode":-1,"wardCode":0,"addressLine":" ","latitude":91,"longitude":181}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(updateAddressUseCase);
    }
}
