package com.mealgo.searchservice.domain;

import java.util.UUID;

public interface CatalogDocument {
    CatalogType type();
    UUID entityId();
    String name();

    default String documentId() {
        String prefix = switch (type()) {
            case RESTAURANT -> "restaurant:";
            case MENU_ITEM -> "menu_item:";
        };
        return prefix + entityId();
    }
}