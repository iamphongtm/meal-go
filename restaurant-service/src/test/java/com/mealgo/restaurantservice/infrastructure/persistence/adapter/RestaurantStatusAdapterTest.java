package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.WardJpaRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static com.mealgo.restaurantservice.domain.enums.RestaurantStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantStatusAdapterTest {
    @Test
    void updatesManagedEntityAndClearsReasonOnReopen() {
        var repository = mock(RestaurantJpaRepository.class);
        var adapter = new RestaurantAdapter(repository, mock(WardJpaRepository.class));
        var id = UUID.randomUUID();
        var owner = UUID.randomUUID();
        var entity = new RestaurantJpaEntity();
        when(repository.findByIdAndOwnerId(id, owner)).thenReturn(Optional.of(entity));

        assertTrue(adapter.updateStatus(id, owner, CLOSED, "other"));
        assertEquals(CLOSED, entity.getStatus());
        assertEquals("other", entity.getPauseReason());
        assertTrue(adapter.updateStatus(id, owner, OPENING, null));
        assertEquals(OPENING, entity.getStatus());
        assertNull(entity.getPauseReason());
        assertFalse(adapter.updateStatus(id, UUID.randomUUID(), CLOSED, "other"));
    }
}
