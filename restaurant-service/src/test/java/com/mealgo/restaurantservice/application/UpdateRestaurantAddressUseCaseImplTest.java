package com.mealgo.restaurantservice.application;

import com.mealgo.restaurantservice.application.event.RestaurantAddressUpdatedEvent;
import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantAddressResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressCommand;
import com.mealgo.restaurantservice.application.port.out.OutboxEventStore;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.application.port.out.WardRepository;
import com.mealgo.restaurantservice.application.usecase.UpdateRestaurantAddressUseCaseImpl;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.model.geography.Ward;
import com.mealgo.restaurantservice.domain.model.restaurant.Address;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateRestaurantAddressUseCaseImplTest {
    private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
    private final WardRepository wards = mock(WardRepository.class);
    private final OutboxEventStore outbox = mock(OutboxEventStore.class);
    private final UpdateRestaurantAddressUseCaseImpl useCase =
            new UpdateRestaurantAddressUseCaseImpl(restaurants, wards, outbox);

    private Restaurant setup() {
        Restaurant restaurant = Restaurant.register(
                UUID.randomUUID(),
                "Test restaurant",
                "0900000000",
                RestaurantStatus.CLOSED,
                new Address("Old address", 1),
                10.0,
                106.0
        );

        restaurant.review(ApprovalStatus.APPROVED, UUID.randomUUID(), null, Instant.now());

        when(restaurants.findByIdForUpdate(restaurant.getId().value()))
                .thenReturn(Optional.of(restaurant));
        when(wards.findByCode(2)).thenReturn(Optional.of(new Ward(2, "Ward", "ward", "ward", 79)));

        return restaurant;
    }

    private UpdateRestaurantAddressCommand command(Restaurant restaurant) {
        return new UpdateRestaurantAddressCommand(
                restaurant.getId().value(),
                restaurant.getOwnerId(),
                79,
                2,
                "  New address  ",
                11.0,
                107.0
        );
    }

    @Test
    void updatesAddressAndWritesSnapshotEvent() {
        Restaurant restaurant = setup();

        RestaurantAddressResult result = useCase.updateAddress(command(restaurant));

        assertEquals("New address", result.addressLine());
        assertEquals(2, result.wardCode());
        assertEquals(11.0, result.latitude());
        assertEquals(107.0, result.longitude());
        assertEquals(ApprovalStatus.APPROVED, restaurant.getApprovalStatus());
        assertEquals(RestaurantStatus.CLOSED, restaurant.getStatus());
        verify(restaurants).updateState(restaurant);

        ArgumentCaptor<RestaurantAddressUpdatedEvent> events =
                ArgumentCaptor.forClass(RestaurantAddressUpdatedEvent.class);
        verify(outbox).append(events.capture());

        RestaurantAddressUpdatedEvent event = events.getValue();
        assertEquals("RestaurantAddressUpdated", event.eventType());
        assertEquals(restaurant.getId().value(), event.restaurantId());
        assertEquals(restaurant.getName(), event.name());
        assertEquals("CLOSED", event.status());
        assertEquals(result.addressLine(), event.addressLine());
        assertEquals(result.latitude(), event.latitude());
    }

    @Test
    void unchangedAddressDoesNotWriteOrEmitEvent() {
        Restaurant restaurant = setup();
        UpdateRestaurantAddressCommand request = command(restaurant);
        useCase.updateAddress(request);
        clearInvocations(restaurants, wards, outbox);

        useCase.updateAddress(request);

        verify(restaurants, never()).updateState(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void differentOwnerCannotUpdate() {
        Restaurant restaurant = setup();
        UpdateRestaurantAddressCommand request = new UpdateRestaurantAddressCommand(
                restaurant.getId().value(), UUID.randomUUID(), 79, 2, "New address", 11.0, 107.0
        );

        assertThrows(BusinessException.class, () -> useCase.updateAddress(request));

        verifyNoInteractions(wards, outbox);
        verify(restaurants, never()).updateState(any());
    }

    @Test
    void rejectsMissingRestaurant() {
        Restaurant restaurant = setup();
        when(restaurants.findByIdForUpdate(restaurant.getId().value())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> useCase.updateAddress(command(restaurant)));

        verifyNoInteractions(wards, outbox);
    }

    @Test
    void rejectsMissingWard() {
        Restaurant restaurant = setup();
        when(wards.findByCode(2)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> useCase.updateAddress(command(restaurant)));

        verifyNoInteractions(outbox);
        verify(restaurants, never()).updateState(any());
    }

    @Test
    void rejectsWardFromAnotherProvince() {
        Restaurant restaurant = setup();
        when(wards.findByCode(2)).thenReturn(Optional.of(new Ward(2, "Ward", "ward", "ward", 80)));

        assertThrows(BusinessException.class, () -> useCase.updateAddress(command(restaurant)));

        verifyNoInteractions(outbox);
        verify(restaurants, never()).updateState(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -91, 91})
    void rejectsInvalidLatitude(Double latitude) {
        Restaurant restaurant = setup();
        UpdateRestaurantAddressCommand request = new UpdateRestaurantAddressCommand(
                restaurant.getId().value(), restaurant.getOwnerId(), 79, 2, "New address", latitude, 107.0
        );

        assertThrows(BusinessException.class, () -> useCase.updateAddress(request));

        verifyNoInteractions(restaurants, wards, outbox);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, -181, 181})
    void rejectsInvalidLongitude(Double longitude) {
        Restaurant restaurant = setup();
        UpdateRestaurantAddressCommand request = new UpdateRestaurantAddressCommand(
                restaurant.getId().value(), restaurant.getOwnerId(), 79, 2, "New address", 11.0, longitude
        );

        assertThrows(BusinessException.class, () -> useCase.updateAddress(request));

        verifyNoInteractions(restaurants, wards, outbox);
    }

    @Test
    void rejectsNullCommand() {
        assertThrows(BusinessException.class, () -> useCase.updateAddress(null));

        verifyNoInteractions(restaurants, wards, outbox);
    }

    @Test
    void rejectsOpenRestaurant() {
        Restaurant restaurant = setup();
        restaurant.changeOperatingStatus(RestaurantStatus.OPENING, null);

        assertThrows(BusinessException.class, () -> useCase.updateAddress(command(restaurant)));

        verifyNoInteractions(outbox);
        verify(restaurants, never()).updateState(any());
        assertEquals("Old address", restaurant.getAddress().addressLine());
    }

    @Test
    void rejectsUnapprovedRestaurant() {
        Restaurant restaurant = Restaurant.register(
                UUID.randomUUID(), "Pending", "0900000000", RestaurantStatus.CLOSED,
                new Address("Old address", 1), 10.0, 106.0
        );
        when(restaurants.findByIdForUpdate(restaurant.getId().value())).thenReturn(Optional.of(restaurant));
        when(wards.findByCode(2)).thenReturn(Optional.of(new Ward(2, "Ward", "ward", "ward", 79)));

        assertThrows(BusinessException.class, () -> useCase.updateAddress(command(restaurant)));

        verifyNoInteractions(outbox);
        verify(restaurants, never()).updateState(any());
    }
}
