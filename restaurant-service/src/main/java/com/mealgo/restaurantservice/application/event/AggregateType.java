package com.mealgo.restaurantservice.application.event;

public enum AggregateType {
    MENU_ITEM("menu-item"),
    RESTAURANT("restaurant"),
    MENU_CATEGORY("menu-category");

    private final String value;

    AggregateType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
