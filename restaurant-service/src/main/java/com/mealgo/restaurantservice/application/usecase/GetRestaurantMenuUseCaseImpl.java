package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menu.GetRestaurantMenuUseCase;
import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuTreeResult;
import com.mealgo.restaurantservice.application.port.out.RestaurantMenuQueryPort;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetRestaurantMenuUseCaseImpl implements GetRestaurantMenuUseCase {
    private final RestaurantMenuQueryPort queryPort;

    @Override
    public RestaurantMenuTreeResult getMenuTree(UUID restaurantId) {
        return queryPort.findMenuTree(restaurantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    @Override
    public RestaurantMenuResult getMenu(UUID restaurantId) {
        return queryPort.findMenu(restaurantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    @Override
    public MenuItemDetailResult getMenuItem(UUID restaurantId, UUID menuItemId) {
        return queryPort.findMenuItem(restaurantId, menuItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
    }
}
