package com.mealgo.restaurantservice.application.event;

public enum EventType {
    MENU_ITEM_CREATED("MenuItemCreated"),
    RESTAURANT_CREATED("RestaurantCreated"),
    ;

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
