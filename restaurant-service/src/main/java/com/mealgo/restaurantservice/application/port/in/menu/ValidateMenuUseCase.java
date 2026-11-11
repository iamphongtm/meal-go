package com.mealgo.restaurantservice.application.port.in.menu;

public interface ValidateMenuUseCase {

    ValidateMenuResult validate(ValidateMenuQuery query);
}