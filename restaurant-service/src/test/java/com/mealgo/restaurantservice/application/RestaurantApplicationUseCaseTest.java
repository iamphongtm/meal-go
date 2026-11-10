package com.mealgo.restaurantservice.application;

import com.mealgo.restaurantservice.application.event.RestaurantApprovedEvent;
import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import com.mealgo.restaurantservice.application.port.out.*;
import com.mealgo.restaurantservice.application.usecase.RestaurantApplicationUseCase;
import com.mealgo.restaurantservice.domain.enums.*;
import com.mealgo.restaurantservice.domain.model.restaurant.*;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantApplicationUseCaseTest {
    private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
    private final ApplicationQueryPort queries = mock(ApplicationQueryPort.class);
    private final WardRepository wards = mock(WardRepository.class);
    private final OutboxEventStore outbox = mock(OutboxEventStore.class);
    private final RestaurantApplicationUseCase useCase = new RestaurantApplicationUseCase(restaurants, queries, wards, outbox);

    private Restaurant setup() {
        Restaurant restaurant = Restaurant.register(UUID.randomUUID(), "Test", "0900000000", RestaurantStatus.CLOSED,
                new Address("Address", 1), 10.0, 106.0);
        UUID id = restaurant.getId().value();
        when(restaurants.findByIdForUpdate(id)).thenReturn(Optional.of(restaurant));
        when(queries.findById(id)).thenReturn(Optional.of(new ApplicationResult(id, restaurant.getOwnerId(),
                "Test", "0900000000", "Address", 1, 10.0, 106.0, ApprovalStatus.APPROVED,
                restaurant.getSubmittedAt(), null, null, null)));
        return restaurant;
    }

    @Test
    void approvalSavesAggregateAndOutboxEventWithOwnerId() {
        Restaurant restaurant = setup();
        UUID admin = UUID.randomUUID();
        useCase.review(new ReviewApplicationCommand(restaurant.getId().value(), admin, ApprovalStatus.APPROVED, null));
        verify(restaurants).updateState(restaurant);
        verify(outbox).append(argThat((RestaurantApprovedEvent event) ->
                event.ownerId().equals(restaurant.getOwnerId()) && event.reviewedBy().equals(admin)
                        && event.eventType().equals("RestaurantApproved")));
    }

    @Test
    void rejectionDoesNotEmitApprovalEvent() {
        Restaurant restaurant = setup();
        useCase.review(new ReviewApplicationCommand(restaurant.getId().value(), UUID.randomUUID(), ApprovalStatus.REJECTED, "Missing data"));
        verify(restaurants).updateState(restaurant);
        verifyNoInteractions(outbox);
    }

    @Test
    void differentOwnerCannotResubmit() {
        Restaurant restaurant = setup();
        assertThrows(BusinessException.class, () -> useCase.resubmit(new ResubmitApplicationCommand(
                restaurant.getId().value(), UUID.randomUUID(), "Test", "0900000000", 1, 1, "Address", 10.0, 106.0)));
        verify(restaurants, never()).updateState(any());
        verifyNoInteractions(wards, outbox);
    }
}
