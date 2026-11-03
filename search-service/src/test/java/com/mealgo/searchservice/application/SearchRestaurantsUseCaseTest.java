package com.mealgo.searchservice.application;

import com.mealgo.searchservice.application.port.RestaurantSearchPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SearchRestaurantsUseCaseTest {
    private final RestaurantSearchPort port = mock(RestaurantSearchPort.class);
    private final SearchRestaurantsUseCase useCase = new SearchRestaurantsUseCase(port);

    @Test
    void forwardsNearbyCoordinatesAndPagination() {
        useCase.findNearby(10.78, 106.69, 5, 0, 10);
        verify(port).findNearby(10.78, 106.69, 5, 0, 10);
    }

    @Test
    void trimsDishKeyword() {
        useCase.findByDish("  phở bò  ", 1, 10);
        verify(port).findByDish("phở bò", 1, 10);
    }

    @Test
    void rejectsInvalidNearbyInput() {
        assertThatThrownBy(() -> useCase.findNearby(91, 106, 5, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findNearby(10, -181, 5, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findNearby(Double.NaN, 106, 5, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findNearby(10, 106, Double.POSITIVE_INFINITY, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findNearby(10, 106, 0, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(port);
    }

    @Test
    void rejectsBlankKeywordsAndInvalidPagination() {
        assertThatThrownBy(() -> useCase.findByDish(" ", 0, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findByDish(null, 0, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findByDish("x".repeat(201), 0, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findByDish("phở", -1, 10)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findByDish("phở", 0, 101)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findByDish("phở", 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.findByDish("phở", Integer.MAX_VALUE, 10))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(port);
    }
}
