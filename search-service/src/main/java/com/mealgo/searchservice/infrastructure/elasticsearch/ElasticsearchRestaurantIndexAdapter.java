package com.mealgo.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import com.mealgo.searchservice.application.port.RestaurantIndexPort;
import com.mealgo.searchservice.domain.MenuItemDocument;
import com.mealgo.searchservice.domain.RestaurantDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ElasticsearchRestaurantIndexAdapter implements RestaurantIndexPort {
    @Value("${app.search.index:restaurants}")
    private String indexAlias;
    private final ElasticsearchClient client;

    // Each entity has its own timestamp: a later menu event must not hide a restaurant event.
    static final String RESTAURANT_SCRIPT = """
            if (ctx._source.restaurantUpdatedAt == null ||
                Instant.parse(params.restaurant.updatedAt).compareTo(Instant.parse(ctx._source.restaurantUpdatedAt)) > 0) {
                for (def entry : params.restaurant.entrySet()) {
                    if (entry.getKey() != 'updatedAt') {
                        ctx._source[entry.getKey()] = entry.getValue();
                    }
                }
                ctx._source.restaurantUpdatedAt = params.restaurant.updatedAt;
                if (ctx._source.updatedAt == null || Instant.parse(params.restaurant.updatedAt).compareTo(Instant.parse(ctx._source.updatedAt)) > 0) {
                    ctx._source.updatedAt = params.restaurant.updatedAt;
                }
            } else {
                ctx.op = 'noop';
            }
            """;

    static final String MENU_ITEM_SCRIPT = """
            if (ctx._source.menuItems == null) {
                ctx._source.menuItems = [];
            }
            boolean found = false;
            for (int i = 0; i < ctx._source.menuItems.size(); i++) {
                if (ctx._source.menuItems[i].menuItemId == params.item.menuItemId) {
                    found = true;
                    if (Instant.parse(params.item.updatedAt).compareTo(Instant.parse(ctx._source.menuItems[i].updatedAt)) > 0) {
                        ctx._source.menuItems[i] = params.item;
                    } else {
                        ctx.op = 'noop';
                    }
                    break;
                }
            }
            if (!found) {
                ctx._source.menuItems.add(params.item);
            }
            if (ctx.op != 'noop' && (ctx._source.updatedAt == null ||
                Instant.parse(params.item.updatedAt).compareTo(Instant.parse(ctx._source.updatedAt)) > 0)) {
                ctx._source.updatedAt = params.item.updatedAt;
            }
            """;

    @Override
    public void upsertRestaurant(RestaurantDocument restaurant) {
        Map<String, Object> body = buildRestaurantBody(restaurant);
        update(restaurant.getRestaurantId().toString(), RESTAURANT_SCRIPT, "restaurant", body);
    }

    private Map<String, Object> buildRestaurantBody(RestaurantDocument restaurant) {
        Map<String, Object> body = new HashMap<>();
        body.put("restaurantId", restaurant.getRestaurantId().toString());
        body.put("name", restaurant.getName());
        body.put("status", restaurant.getStatus());
        body.put("addressLine", restaurant.getAddressLine());
        body.put("updatedAt", restaurant.getUpdatedAt().toString());
        body.put("location", buildLocation(restaurant));
        return body;
    }

    private Map<String, Double> buildLocation(RestaurantDocument restaurant) {
        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            return null;
        }
        return Map.of("lat", restaurant.getLatitude(), "lon", restaurant.getLongitude());
    }

    @Override
    public void upsertMenuItem(MenuItemDocument item) {
        Map<String, Object> body = buildMenuItemBody(item);
        update(item.getRestaurantId().toString(), MENU_ITEM_SCRIPT, "item", body);
    }

    private Map<String, Object> buildMenuItemBody(MenuItemDocument item) {
        Map<String, Object> body = new HashMap<>();
        body.put("menuItemId", item.getMenuItemId().toString());
        body.put("name", item.getName());
        body.put("status", item.getStatus());
        body.put("basePrice", item.getBasePrice());
        body.put("imageUrl", item.getImageUrl());
        body.put("updatedAt", item.getUpdatedAt().toString());
        return body;
    }

    private void update(String restaurantId, String script, String parameter, Map<String, Object> body) {
        try {
            client.update(request -> request.index(indexAlias)
                    .id(restaurantId)
                    .requireAlias(true)
                    .retryOnConflict(5)
                    .scriptedUpsert(true)
                    .upsert(Map.of("restaurantId", restaurantId, "menuItems", java.util.List.of()))
                    .script(s -> s.lang("painless")
                            .source(script)
                            .params(parameter, JsonData.of(body))), Map.class);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot update restaurant " + restaurantId, e);
        }
    }
}
