package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.event.MenuItemCreatedEvent;
import com.mealgo.restaurantservice.application.event.RestaurantCreatedEvent;

public interface OutboxEventStore {
    void append(MenuItemCreatedEvent event);
    void append(RestaurantCreatedEvent event);
}
