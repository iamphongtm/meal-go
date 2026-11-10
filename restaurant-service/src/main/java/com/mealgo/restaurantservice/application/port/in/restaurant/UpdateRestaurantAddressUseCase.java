package com.mealgo.restaurantservice.application.port.in.restaurant;

public interface UpdateRestaurantAddressUseCase {
    RestaurantAddressResult updateAddress(UpdateRestaurantAddressCommand command);
}
