package com.mealgo.restaurantservice.application.port.in.restaurant;

public interface UpdateRestaurantStatusUseCase {
    RestaurantStatusResult updateStatus(UpdateRestaurantStatusCommand command);
}
