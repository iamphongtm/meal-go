package com.mealgo.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElasticsearchRestaurantSearchAdapterTest {
    private final ElasticsearchClient client = mock(ElasticsearchClient.class);
    private final ElasticsearchRestaurantSearchAdapter adapter = new ElasticsearchRestaurantSearchAdapter(client);
    private final ObjectMapper mapper = new ObjectMapper();

    ElasticsearchRestaurantSearchAdapterTest() {
        ReflectionTestUtils.setField(adapter, "indexAlias", "restaurants");
    }

    @Test
    void nearbyUsesArcDistanceAndReturnsDistanceInKilometers() throws Exception {
        stubResponse("""
                {"took":1,"timed_out":false,"_shards":{"total":1,"successful":1,"failed":0},
                 "hits":{"total":{"value":1,"relation":"eq"},"hits":[
                   {"_index":"restaurants_v1","_id":"r1","sort":[1.25,"r1"],
                    "_source":{"restaurantId":"r1","name":"Quán A","addressLine":"Address"}}
                 ]}}
                """);

        var result = adapter.findNearby(10.78, 106.69, 5, 0, 10);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().distanceKm()).isEqualTo(1.25);
        assertThat(result.content().getFirst().matchedMenuItems()).isEmpty();
        var query = mapper.readTree(JsonpUtils.toJsonString(capturedRequest(), new JacksonJsonpMapper()));
        assertThat(query.at("/query/bool/must/0/geo_distance/distance").asText()).isEqualTo("5.0km");
        assertThat(query.at("/sort/0/_geo_distance/unit").asText()).isEqualTo("km");
        assertThat(query.at("/sort/0/_geo_distance/distance_type").asText()).isEqualTo("arc");
        assertThat(query.at("/sort/0/_geo_distance/order").asText()).isEqualTo("asc");
    }

    @Test
    void dishSearchRequestsFiveNestedMatchesAndMapsOnlyInnerHits() throws Exception {
        stubResponse("""
                {"took":1,"timed_out":false,"_shards":{"total":1,"successful":1,"failed":0},
                 "hits":{"total":{"value":12,"relation":"eq"},"hits":[
                   {"_index":"restaurants_v1","_id":"r1",
                    "_source":{"restaurantId":"r1","name":"Quán A"},
                    "inner_hits":{"matched_menu_items":{"hits":{"total":{"value":1,"relation":"eq"},
                      "hits":[{"_index":"restaurants_v1","_id":"r1",
                        "_nested":{"field":"menuItems","offset":0},
                        "_source":{"menuItemId":"m1","name":"Phở bò","basePrice":50000}}]}}}}
                 ]}}
                """);

        var result = adapter.findByDish("phở", 1, 10);

        assertThat(result.totalElements()).isEqualTo(12);
        assertThat(result.totalPages()).isEqualTo(2);
        var restaurant = result.content().getFirst();
        assertThat(restaurant.distanceKm()).isNull();
        assertThat(restaurant.matchedMenuItems()).hasSize(1);
        assertThat(restaurant.matchedMenuItems().getFirst().name()).isEqualTo("Phở bò");
        assertThat(restaurant.matchedMenuItems().getFirst().basePrice()).isEqualByComparingTo("50000");
        var request = mapper.readTree(JsonpUtils.toJsonString(capturedRequest(), new JacksonJsonpMapper()));
        assertThat(request.path("from").asInt()).isEqualTo(10);
        var nested = request.at("/query/bool/must/0/nested");
        assertThat(nested.path("path").asText()).isEqualTo("menuItems");
        assertThat(nested.at("/inner_hits/size").asInt()).isEqualTo(5);
        assertThat(nested.at("/query/match/menuItems.name/query").asText()).isEqualTo("phở");
        assertThat(request.at("/_source/excludes/0").asText()).isEqualTo("menuItems");
    }

    @Test
    void returnsEmptyPageWhenNoRestaurantsMatch() throws Exception {
        stubResponse("""
                {"took":1,"timed_out":false,"_shards":{"total":1,"successful":1,"failed":0},
                 "hits":{"total":{"value":0,"relation":"eq"},"hits":[]}}
                """);
        var result = adapter.findByDish("unknown", 0, 10);
        assertThat(result.content()).isEmpty();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void wrapsIoFailure() throws Exception {
        when(client.search(any(SearchRequest.class), eq(JsonData.class))).thenThrow(new IOException("offline"));
        assertThatThrownBy(() -> adapter.findByDish("phở", 0, 10))
                .isInstanceOf(IllegalStateException.class).hasCauseInstanceOf(IOException.class);
    }

    private void stubResponse(String json) throws IOException {
        JacksonJsonpMapper jsonMapper = new JacksonJsonpMapper();
        SearchResponse<JsonData> response;
        try (var parser = jsonMapper.jsonProvider().createParser(new StringReader(json))) {
            response = SearchResponse.createSearchResponseDeserializer(JsonData._DESERIALIZER)
                    .deserialize(parser, jsonMapper);
        }
        when(client.search(any(SearchRequest.class), eq(JsonData.class))).thenReturn(response);
    }

    private SearchRequest capturedRequest() throws IOException {
        ArgumentCaptor<SearchRequest> request = ArgumentCaptor.forClass(SearchRequest.class);
        verify(client).search(request.capture(), eq(JsonData.class));
        return request.getValue();
    }
}
