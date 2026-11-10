package com.mealgo.searchservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mealgo.searchservice.model.CatalogEventType;
import com.mealgo.searchservice.model.RestaurantDocument;
import com.mealgo.searchservice.repository.RestaurantIndexRepository;
import com.mealgo.searchservice.service.impl.CatalogIndexServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantAddressIndexTest {
    private final RestaurantIndexRepository repository = mock(RestaurantIndexRepository.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final CatalogIndexServiceImpl service = new CatalogIndexServiceImpl(repository, mapper);

    @Test
    void recognizesAddressUpdateAndIndexesSnapshot() {
        UUID restaurantId = UUID.randomUUID();
        Instant occurredAt = Instant.now();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("restaurantId", restaurantId.toString());
        payload.put("name", "Restaurant");
        payload.put("status", "CLOSED");
        payload.put("addressLine", "New address");
        payload.put("latitude", 11.0);
        payload.put("longitude", 107.0);

        CatalogEventType type = CatalogEventType.fromExternal("RestaurantAddressUpdated").orElseThrow();
        service.handle(type, restaurantId, occurredAt, payload);

        ArgumentCaptor<RestaurantDocument> documents = ArgumentCaptor.forClass(RestaurantDocument.class);
        verify(repository).upsertRestaurant(documents.capture());

        RestaurantDocument document = documents.getValue();
        assertEquals(restaurantId, document.getRestaurantId());
        assertEquals("New address", document.getAddressLine());
        assertEquals(11.0, document.getLatitude());
        assertEquals(107.0, document.getLongitude());
        assertEquals(occurredAt, document.getUpdatedAt());
        verify(repository, never()).upsertMenuItem(any());
    }

    @Test
    void rejectsMismatchedIdentity() {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("restaurantId", UUID.randomUUID().toString());

        assertThrows(IllegalArgumentException.class, () -> service.handle(
                CatalogEventType.RESTAURANT_ADDRESS_UPDATED,
                UUID.randomUUID(),
                Instant.now(),
                payload
        ));

        verifyNoInteractions(repository);
    }
}
