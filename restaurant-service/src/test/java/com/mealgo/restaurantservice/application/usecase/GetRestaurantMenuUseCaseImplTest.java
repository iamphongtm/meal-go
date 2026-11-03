package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import com.mealgo.restaurantservice.application.port.out.RestaurantMenuQueryPort;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetRestaurantMenuUseCaseImplTest {
    private final RestaurantMenuQueryPort port = mock(RestaurantMenuQueryPort.class);
    private final GetRestaurantMenuUseCaseImpl useCase = new GetRestaurantMenuUseCaseImpl(port);

    @Test
    void returnsMenu() {
        UUID id = UUID.randomUUID();
        var menu = new RestaurantMenuResult(null, List.of());
        when(port.findMenu(id)).thenReturn(Optional.of(menu));
        assertSame(menu, useCase.getMenu(id));
    }

    @Test
    void reportsRestaurantNotFound() {
        UUID id = UUID.randomUUID();
        when(port.findMenu(id)).thenReturn(Optional.empty());
        assertEquals(ErrorCode.RESTAURANT_NOT_FOUND,
                assertThrows(BusinessException.class, () -> useCase.getMenu(id)).getErrorCode());
    }

    @Test
    void reportsItemNotFound() {
        UUID restaurantId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(port.findMenuItem(restaurantId, itemId)).thenReturn(Optional.empty());
        assertEquals(ErrorCode.MENU_ITEM_NOT_FOUND, assertThrows(BusinessException.class,
                () -> useCase.getMenuItem(restaurantId, itemId)).getErrorCode());
    }
}
