package com.mealgo.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.searchservice.domain.MenuItemDocument;
import com.mealgo.searchservice.domain.RestaurantDocument;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "RUN_SEARCH_INTEGRATION_TESTS", matches = "true")
class RestaurantProjectionIntegrationTest {
    @Test
    void projectsOutOfOrderEventsWithoutLosingMenuItems() throws Exception {
        String index = "test-restaurants-" + UUID.randomUUID();
        String alias = index + "-alias";
        var mapper = new ObjectMapper();
        String url = System.getenv().getOrDefault("TEST_ELASTICSEARCH_URL", "http://localhost:29200");
        try (var transport = new RestClientTransport(RestClient.builder(HttpHost.create(url)).build(),
                new JacksonJsonpMapper())) {
            var client = new ElasticsearchClient(transport);
            try (var resource = getClass().getResourceAsStream("/elasticsearch/restaurants_v1.json")) {
                var mapping = mapper.readTree(resource);
                ((com.fasterxml.jackson.databind.node.ObjectNode) mapping).remove("aliases");
                client.indices().create(c -> c.index(index).withJson(new StringReader(mapping.toString()))
                        .aliases(alias, a -> a.isWriteIndex(true)));
            }
            try {
                var writer = new ElasticsearchRestaurantIndexAdapter(client);
                ReflectionTestUtils.setField(writer, "indexAlias", alias);
                UUID restaurantId = UUID.randomUUID();
                UUID itemId = UUID.randomUUID();
                Instant initial = Instant.parse("2026-11-03T10:00:00Z");
                var item = MenuItemDocument.builder().restaurantId(restaurantId).menuItemId(itemId)
                        .name("Phở bò").status("ACTIVE").basePrice(new BigDecimal("65000"))
                        .updatedAt(initial.plusSeconds(20)).build();
                writer.upsertMenuItem(item);
                writer.upsertMenuItem(item);
                JsonNode incomplete = mapper.readTree(client.get(g -> g.index(index).id(restaurantId.toString()),
                        JsonData.class).source().toJson().toString());
                assertThat(incomplete.path("menuItems").size()).isEqualTo(1);
                assertThat(incomplete.has("name")).isFalse();

                writer.upsertRestaurant(restaurant(restaurantId, "Phở Hòa", "OPENING", initial, 10.78));
                writer.upsertRestaurant(restaurant(restaurantId, "Updated name", "OPENING", initial.plusSeconds(1), 10.78));
                writer.upsertRestaurant(restaurant(restaurantId, "Stale name", "OPENING", initial, 10.78));
                writer.upsertMenuItem(MenuItemDocument.builder().restaurantId(restaurantId).menuItemId(itemId)
                        .name("Stale item").status("ACTIVE").basePrice(BigDecimal.ONE).updatedAt(initial).build());
                writer.upsertMenuItem(MenuItemDocument.builder().restaurantId(restaurantId).menuItemId(UUID.randomUUID())
                        .name("Phở gà").status("SOLD_OUT").basePrice(BigDecimal.TEN).updatedAt(initial).build());

                JsonNode source = mapper.readTree(client.get(g -> g.index(index).id(restaurantId.toString()),
                        JsonData.class).source().toJson().toString());
                assertThat(source.path("menuItems").size()).isEqualTo(2);
                assertThat(source.path("name").asText()).isEqualTo("Updated name");
                assertThat(source.path("updatedAt").asText()).isEqualTo(initial.plusSeconds(20).toString());

                assertThat(source.path("menuItems").get(0).path("name").asText()).isEqualTo("Phở bò");
                assertThat(source.path("menuItems").get(1).path("status").asText()).isEqualTo("SOLD_OUT");
            } finally {
                client.indices().delete(d -> d.index(index));
            }
        }
    }

    private RestaurantDocument restaurant(UUID id, String name, String status, Instant time, double latitude) {
        return RestaurantDocument.builder().restaurantId(id).name(name).status(status)
                .addressLine("Test address").latitude(latitude).longitude(106.69).updatedAt(time).build();
    }
}
