package com.mealgo.restaurantservice.application.port.in.menucategory;

import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;

public interface ManageMenuCategoryUseCase {

    MenuCategoryId create(CreateMenuCategoryCommand command);
}
