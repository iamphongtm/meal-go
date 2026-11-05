package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;
import com.mealgo.restaurantservice.application.port.out.MyRestaurantQueryPort;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMyRestaurantUseCaseImplTest {
    private final MyRestaurantQueryPort query = mock(MyRestaurantQueryPort.class);
    private final GetMyRestaurantUseCaseImpl useCase = new GetMyRestaurantUseCaseImpl(query);

    @Test
    void returnsRestaurantForOwner() {
        var ownerId = UUID.randomUUID();
        var result = new MyRestaurantResult(UUID.randomUUID(), "Mạnh Phong", RestaurantStatus.OPENING);
        when(query.findByOwnerId(ownerId)).thenReturn(Optional.of(result));

        assertEquals(result, useCase.getMyRestaurant(ownerId));
        verify(query).findByOwnerId(ownerId);
    }

    @Test
    void rejectsOwnerWithoutRestaurant() {
        var ownerId = UUID.randomUUID();
        when(query.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        var exception = assertThrows(BusinessException.class, () -> useCase.getMyRestaurant(ownerId));
        assertEquals(ErrorCode.RESTAURANT_NOT_FOUND, exception.getErrorCode());
    }
}
