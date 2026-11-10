package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.event.MenuItemCreatedEvent;
import com.mealgo.restaurantservice.application.event.RestaurantCreatedEvent;

public interface OutboxEventStore {
    void append(com.mealgo.restaurantservice.application.event.RestaurantAddressUpdatedEvent event);

    void append(com.mealgo.restaurantservice.application.event.RestaurantApprovedEvent event);
    void append(MenuItemCreatedEvent event);
    void append(RestaurantCreatedEvent event);
}
