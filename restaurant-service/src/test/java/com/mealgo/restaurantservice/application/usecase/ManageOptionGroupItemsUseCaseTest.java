package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.out.OptionGroupItemsRepository;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManageOptionGroupItemsUseCaseTest {
    private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
    private final OptionGroupItemsRepository groups = mock(OptionGroupItemsRepository.class);
    private final ManageOptionGroupItemsUseCase service = new ManageOptionGroupItemsUseCase(restaurants, groups);
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void rejectsListingAnotherOwnersGroups() {
        assertThrows(BusinessException.class, () -> service.list(restaurantId, ownerId));
        verifyNoInteractions(groups);
    }

    @Test
    void rejectsUpdatingAnotherOwnersGroup() {
        assertThrows(BusinessException.class, () -> service.replace(restaurantId, ownerId, UUID.randomUUID(), List.of()));
        verifyNoInteractions(groups);
    }

    @Test
    void permitsClearingAllItemsOfOneGroup() {
        var groupId = UUID.randomUUID();
        when(restaurants.existsByIdAndOwnerId(restaurantId, ownerId)).thenReturn(true);
        service.replace(restaurantId, ownerId, groupId, List.of());
        verify(groups).replaceItems(restaurantId, groupId, List.of());
    }
}
