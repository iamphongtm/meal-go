package com.mealgo.searchservice.infrastructure.elasticsearch.mapping;

import com.mealgo.searchservice.domain.CatalogDocument;
import com.mealgo.searchservice.domain.CatalogType;
import com.mealgo.searchservice.domain.MenuItemDocument;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MenuItemDocumentMapper implements CatalogDocumentMapper {

    @Override
    public CatalogType supportedType() {
        return CatalogType.MENU_ITEM;
    }

    @Override
    public Map<String, Object> toBody(CatalogDocument document) {
        if (!(document instanceof MenuItemDocument item)) {
            throw new IllegalArgumentException("Expected MenuItemDocument");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("type", item.type().name());
        body.put("name", item.name());
        body.put("menuItemId", item.getMenuItemId().toString());
        body.put("restaurantId", item.getRestaurantId().toString());
        body.put("categoryId", item.getCategoryId().toString());
        body.put("basePrice", item.getBasePrice());
        body.put("imageUrl", item.getImageUrl());
        body.put("status", item.getStatus());
        body.put("updatedAt", item.getUpdatedAt().toString());
        return body;
    }
}
