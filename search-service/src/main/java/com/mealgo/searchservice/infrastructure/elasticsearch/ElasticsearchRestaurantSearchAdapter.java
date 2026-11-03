package com.mealgo.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.DistanceUnit;
import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch._types.GeoLocation;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.ChildScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.GeoDistanceQuery;
import co.elastic.clients.elasticsearch.core.search.InnerHits;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.InnerHitsResult;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.JsonNode;
import com.mealgo.searchservice.application.model.RestaurantSearchPage;
import com.mealgo.searchservice.application.model.RestaurantSearchResult;
import com.mealgo.searchservice.application.model.RestaurantSearchResult.MatchedMenuItem;
import com.mealgo.searchservice.application.port.RestaurantSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ElasticsearchRestaurantSearchAdapter implements RestaurantSearchPort {
    private static final String MATCHED_ITEMS = "matched_menu_items";
    private final ElasticsearchClient client;

    @Value("${app.search.index:restaurants}")
    private String indexAlias;

    @Override
    public RestaurantSearchPage findNearby(double latitude, double longitude, double radiusKm, int page, int size) {
        GeoLocation origin = GeoLocation.of(location -> location.latlon(coordinates -> {
            coordinates.lat(latitude);
            coordinates.lon(longitude);
            return coordinates;
        }));

        GeoDistanceQuery.Builder distanceQuery = new GeoDistanceQuery.Builder();
        distanceQuery.field("location");
        distanceQuery.location(origin);
        distanceQuery.distance(radiusKm + "km");
        distanceQuery.distanceType(GeoDistanceType.Arc);

        SortOptions distanceSort = SortOptions.of(sort -> sort.geoDistance(distance -> {
            distance.field("location");
            distance.location(origin);
            distance.unit(DistanceUnit.Kilometers);
            distance.distanceType(GeoDistanceType.Arc);
            distance.order(SortOrder.Asc);
            return distance;
        }));

        SearchRequest.Builder request = baseRequest(page, size);
        request.query(completeRestaurantQuery(distanceQuery.build()._toQuery()));
        request.sort(distanceSort);
        request.sort(sort -> sort.field(field -> field.field("restaurantId").order(SortOrder.Asc)));
        return execute(request.build(), page, size, true);
    }

    @Override
    public RestaurantSearchPage findByDish(String keyword, int page, int size) {
        MatchQuery.Builder dishNameQuery = new MatchQuery.Builder();
        dishNameQuery.field("menuItems.name");
        dishNameQuery.query(keyword);
        dishNameQuery.operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And);

        InnerHits.Builder innerHits = new InnerHits.Builder();
        innerHits.name(MATCHED_ITEMS);
        innerHits.size(5);

        NestedQuery.Builder menuQuery = new NestedQuery.Builder();
        menuQuery.path("menuItems");
        menuQuery.query(dishNameQuery.build()._toQuery());
        menuQuery.scoreMode(ChildScoreMode.Max);
        menuQuery.innerHits(innerHits.build());

        SearchRequest.Builder request = baseRequest(page, size);
        request.query(completeRestaurantQuery(menuQuery.build()._toQuery()));
        request.sort(sort -> sort.score(score -> score.order(SortOrder.Desc)));
        request.sort(sort -> sort.field(field -> field.field("restaurantId").order(SortOrder.Asc)));
        return execute(request.build(), page, size, false);
    }

    private SearchRequest.Builder baseRequest(int page, int size) {
        SearchRequest.Builder request = new SearchRequest.Builder();
        request.index(indexAlias);
        request.from(page * size);
        request.size(size);
        request.trackTotalHits(total -> total.enabled(true));
        request.source(source -> source.filter(filter -> filter.excludes("menuItems")));
        return request;
    }

    private Query completeRestaurantQuery(Query query) {
        // Menu events can create a placeholder before restaurant details arrive.
        return Query.of(builder -> builder.bool(bool -> {
            bool.must(query);
            bool.filter(filter -> filter.exists(exists -> exists.field("restaurantUpdatedAt")));
            return bool;
        }));
    }

    private RestaurantSearchPage execute(SearchRequest request, int page, int size, boolean nearby) {
        try {
            SearchResponse<JsonData> response = client.search(request, JsonData.class);
            List<RestaurantSearchResult> restaurants = new ArrayList<>();
            for (Hit<JsonData> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    restaurants.add(toRestaurant(hit, nearby));
                }
            }
            long total = response.hits().total() == null ? 0 : response.hits().total().value();
            return RestaurantSearchPage.of(restaurants, page, size, total);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot search restaurants", exception);
        }
    }

    private RestaurantSearchResult toRestaurant(Hit<JsonData> hit, boolean nearby) {
        JsonNode restaurant = hit.source().to(JsonNode.class);
        Double distanceKm = nearby ? hit.sort().getFirst().doubleValue() : null;
        List<MatchedMenuItem> items = new ArrayList<>();
        InnerHitsResult matches = hit.innerHits().get(MATCHED_ITEMS);
        if (matches != null) {
            for (Hit<JsonData> itemHit : matches.hits().hits()) {
                if (itemHit.source() == null) {
                    continue;
                }
                JsonNode item = itemHit.source().to(JsonNode.class);
                items.add(new MatchedMenuItem(
                        text(item, "menuItemId"), text(item, "name"), text(item, "status"),
                        item.hasNonNull("basePrice") ? item.get("basePrice").decimalValue() : null,
                        text(item, "imageUrl")));
            }
        }
        return new RestaurantSearchResult(
                text(restaurant, "restaurantId"), text(restaurant, "name"),
                text(restaurant, "status"), text(restaurant, "addressLine"), distanceKm, List.copyOf(items));
    }

    private String text(JsonNode source, String field) {
        return source.hasNonNull(field) ? source.get(field).asText() : null;
    }
}
