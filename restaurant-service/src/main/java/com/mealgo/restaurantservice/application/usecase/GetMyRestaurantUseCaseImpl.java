package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.restaurant.GetMyRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.in.restaurant.MyRestaurantResult;
import com.mealgo.restaurantservice.application.port.out.MyRestaurantQueryPort;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMyRestaurantUseCaseImpl implements GetMyRestaurantUseCase {
    private final MyRestaurantQueryPort queryPort;

    @Override
    public MyRestaurantResult getMyRestaurant(UUID ownerId) {
        return queryPort.findByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }
}
