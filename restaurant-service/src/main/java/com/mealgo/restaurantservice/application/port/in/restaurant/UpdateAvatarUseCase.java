package com.mealgo.restaurantservice.application.port.in.restaurant;

public interface UpdateAvatarUseCase {
    RestaurantAvatarResult updateAvatar(UpdateAvatarCommand command);
}
