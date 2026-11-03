package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.restaurantservice.application.event.MenuItemCreatedEvent;
import com.mealgo.restaurantservice.application.event.RestaurantCreatedEvent;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxEventStoreAdapterTest {

    private OutboxEventJpaRepository repository;
    private OutboxEventStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventJpaRepository.class);
        adapter = new OutboxEventStoreAdapter(repository, new ObjectMapper());
    }

    @Test
    void usesRestaurantEventIdAsOutboxId() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        adapter.append(RestaurantCreatedEvent.builder()
                .eventId(eventId)
                .restaurantId(restaurantId)
                .latitude(10.7626)
                .longitude(106.6822)
                .build());

        OutboxEventJpaEntity entity = savedEntity();
        assertEquals(eventId, entity.getId());
        assertEquals(restaurantId, entity.getAggregateId());
        assertEquals("restaurant", entity.getAggregateType());
        var payload = new ObjectMapper().readTree(entity.getPayload());
        assertEquals(10.7626, payload.get("latitude").asDouble());
        assertEquals(106.6822, payload.get("longitude").asDouble());
        assertEquals(eventId.toString(), new ObjectMapper()
                .readTree(entity.getPayload()).get("eventId").asText());
    }

    @Test
    void usesMenuItemEventIdAsOutboxId() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID menuItemId = UUID.randomUUID();
        adapter.append(MenuItemCreatedEvent.builder()
                .eventId(eventId)
                .menuItemId(menuItemId)
                .build());

        OutboxEventJpaEntity entity = savedEntity();
        assertEquals(eventId, entity.getId());
        assertEquals(menuItemId, entity.getAggregateId());
        assertEquals("menu-item", entity.getAggregateType());
        assertEquals(eventId.toString(), new ObjectMapper()
                .readTree(entity.getPayload()).get("eventId").asText());
    }

    private OutboxEventJpaEntity savedEntity() {
        ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
}
