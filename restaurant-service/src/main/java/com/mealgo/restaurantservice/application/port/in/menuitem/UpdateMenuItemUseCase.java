package com.mealgo.restaurantservice.application.port.in.menuitem;

public interface UpdateMenuItemUseCase {
    void update(UpdateMenuItemCommand command);
    void updateStatus(UpdateMenuItemStatusCommand command);
}
