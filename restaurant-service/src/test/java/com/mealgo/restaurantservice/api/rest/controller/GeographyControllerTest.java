package com.mealgo.restaurantservice.api.rest.controller;

import com.mealgo.restaurantservice.application.port.in.geography.GeographyResult;
import com.mealgo.restaurantservice.application.port.out.GeographyQueryPort;
import com.mealgo.restaurantservice.application.usecase.ListGeographyUseCaseImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GeographyControllerTest {
    private final GeographyQueryPort query = mock(GeographyQueryPort.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new GeographyController(new ListGeographyUseCaseImpl(query))).build();

    @Test
    void listsProvinceCodesAndDisplayNames() throws Exception {
        when(query.findProvinces()).thenReturn(List.of(new GeographyResult(1, "Thành phố Hà Nội")));
        mvc.perform(get("/geography/provinces")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value(1))
                .andExpect(jsonPath("$[0].name").value("Thành phố Hà Nội"));
    }

    @Test
    void listsWardsForSelectedProvince() throws Exception {
        when(query.provinceExists(1)).thenReturn(true);
        when(query.findWards(1)).thenReturn(List.of(new GeographyResult(123, "Xã Hòa Lạc")));
        mvc.perform(get("/geography/provinces/1/wards")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Xã Hòa Lạc"));
        verify(query).findWards(1);
    }

    @Test
    void returnsEmptyListForProvinceWithoutWards() throws Exception {
        when(query.provinceExists(1)).thenReturn(true);
        when(query.findWards(1)).thenReturn(List.of());
        mvc.perform(get("/geography/provinces/1/wards")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void rejectsInvalidAndUnknownProvince() throws Exception {
        mvc.perform(get("/geography/provinces/0/wards")).andExpect(status().isBadRequest());
        mvc.perform(get("/geography/provinces/999/wards")).andExpect(status().isNotFound());
        mvc.perform(get("/geography/provinces/abc/wards")).andExpect(status().isBadRequest());
        verify(query, never()).findWards(any());
    }
}
