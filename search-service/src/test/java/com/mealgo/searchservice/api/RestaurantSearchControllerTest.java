package com.mealgo.searchservice.api;

import com.mealgo.searchservice.application.SearchRestaurantsUseCase;
import com.mealgo.searchservice.application.port.RestaurantSearchPort;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestaurantSearchControllerTest {
    private final RestaurantSearchPort port = mock(RestaurantSearchPort.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new RestaurantSearchController(new SearchRestaurantsUseCase(port))).build();

    @Test
    void nearbyUsesDefaultRadiusAndPagination() throws Exception {
        mvc.perform(get("/api/search/restaurants/nearby")
                        .param("latitude", "10.78").param("longitude", "106.69"))
                .andExpect(status().isOk());
        verify(port).findNearby(10.78, 106.69, 5, 0, 10);
    }

    @Test
    void dishSearchUsesDefaultPagination() throws Exception {
        mvc.perform(get("/api/search/restaurants/by-dish").param("keyword", "phở"))
                .andExpect(status().isOk());
        verify(port).findByDish("phở", 0, 10);
    }

    @Test
    void invalidInputReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/search/restaurants/nearby")
                        .param("latitude", "91").param("longitude", "106"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/search/restaurants/by-dish").param("keyword", " "))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/search/restaurants/by-dish"))
                .andExpect(status().isBadRequest());
    }
}
