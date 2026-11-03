package com.mealgo.searchservice.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealgo.searchservice.application.port.RestaurantIndexPort;
import com.mealgo.searchservice.domain.CatalogEventType;
import com.mealgo.searchservice.domain.MenuItemDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class IndexOutboxEventUseCaseTest {
    private final RestaurantIndexPort port = mock(RestaurantIndexPort.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final IndexOutboxEventUseCase useCase = new IndexOutboxEventUseCase(port, mapper);

    @Test
    void menuItemDoesNotRequireCategory() throws Exception {
        UUID id = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        var payload = mapper.readTree("""
                {"menuItemId":"%s","restaurantId":"%s","name":"Pho bo","status":"ACTIVE","basePrice":65000}
                """.formatted(id, restaurantId));
        useCase.handle(CatalogEventType.MENU_ITEM_CREATED, id, Instant.now(), payload);
        var captor = ArgumentCaptor.forClass(MenuItemDocument.class);
        verify(port).upsertMenuItem(captor.capture());
        assertThat(captor.getValue().getRestaurantId()).isEqualTo(restaurantId);
    }

    @Test
    void rejectsMismatchedIdentity() throws Exception {
        var payload = mapper.readTree("""
                {"restaurantId":"%s","name":"Pho Hoa","status":"OPENING"}
                """.formatted(UUID.randomUUID()));
        assertThatThrownBy(() -> useCase.handle(CatalogEventType.RESTAURANT_CREATED,
                UUID.randomUUID(), Instant.now(), payload)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(port);
    }
}
