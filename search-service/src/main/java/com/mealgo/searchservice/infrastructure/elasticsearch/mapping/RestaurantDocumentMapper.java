package com.mealgo.searchservice.infrastructure.elasticsearch.mapping;

import com.mealgo.searchservice.domain.CatalogDocument;
import com.mealgo.searchservice.domain.CatalogType;
import com.mealgo.searchservice.domain.RestaurantDocument;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RestaurantDocumentMapper implements CatalogDocumentMapper {

    @Override
    public CatalogType supportedType() {
        return CatalogType.RESTAURANT;
    }

    @Override
    public Map<String, Object> toBody(CatalogDocument document) {
        if (!(document instanceof RestaurantDocument restaurant)) {
            throw new IllegalArgumentException("Expected RestaurantDocument");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("type", restaurant.type().name());
        body.put("name", restaurant.name());
        body.put("restaurantId", restaurant.getRestaurantId().toString());
        body.put("status", restaurant.getStatus());
        body.put("addressLine", restaurant.getAddressLine());
        body.put("wardCode", restaurant.getWardCode());
        body.put("updatedAt", restaurant.getUpdatedAt().toString());

        Double latitude = restaurant.getLatitude();
        Double longitude = restaurant.getLongitude();

        if (latitude != null && longitude != null) {
            body.put("location", Map.of(
                    "lat", latitude,
                    "lon", longitude
            ));
        }

        body.put("updatedAt", restaurant.getUpdatedAt().toString());
        return body;
    }
}
