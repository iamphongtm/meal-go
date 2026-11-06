package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantStatusCommand;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static com.mealgo.restaurantservice.domain.enums.RestaurantStatus.*;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateRestaurantStatusUseCaseImplTest {
    private final RestaurantRepository repository = mock(RestaurantRepository.class);
    private final UpdateRestaurantStatusUseCaseImpl useCase = new UpdateRestaurantStatusUseCaseImpl(repository);
    private final UUID id = UUID.randomUUID();
    private final UUID owner = UUID.randomUUID();

    @Test
    void closesWithReasonAndOwnerScopedUpdate() {
        when(repository.updateStatus(id, owner, CLOSED, "overload")).thenReturn(true);
        var result = useCase.updateStatus(new UpdateRestaurantStatusCommand(id, owner, CLOSED, "overload"));
        assertEquals(CLOSED, result.status());
        assertEquals("overload", result.pauseReason());
        verify(repository).updateStatus(id, owner, CLOSED, "overload");
    }

    @Test
    void openingClearsReason() {
        when(repository.updateStatus(id, owner, OPENING, null)).thenReturn(true);
        var result = useCase.updateStatus(new UpdateRestaurantStatusCommand(id, owner, OPENING, "overload"));
        assertNull(result.pauseReason());
        verify(repository).updateStatus(id, owner, OPENING, null);
    }

    @Test
    void rejectsMissingOrInvalidReason() {
        for (String reason : new String[]{null, "", "invalid"}) {
            var error = assertThrows(BusinessException.class, () ->
                    useCase.updateStatus(new UpdateRestaurantStatusCommand(id, owner, CLOSED, reason)));
            assertEquals(RESTAURANT_PAUSE_REASON_INVALID, error.getErrorCode());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsAnotherOwnersOrMissingRestaurant() {
        var error = assertThrows(BusinessException.class, () ->
                useCase.updateStatus(new UpdateRestaurantStatusCommand(id, owner, CLOSED, "other")));
        assertEquals(RESTAURANT_NOT_FOUND, error.getErrorCode());
    }

    @Test
    void rejectsMissingStatus() {
        var error = assertThrows(BusinessException.class, () ->
                useCase.updateStatus(new UpdateRestaurantStatusCommand(id, owner, null, null)));
        assertEquals(RESTAURANT_STATUS_INVALID, error.getErrorCode());
        verifyNoInteractions(repository);
    }
}
