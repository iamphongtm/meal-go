package com.mealgo.restaurantservice.application;

import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateAvatarCommand;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.application.usecase.UpdateAvatarUseCaseImpl;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.model.restaurant.*;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.*;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.RestaurantPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateAvatarUseCaseImplTest {
    private final RestaurantRepository repository = mock(RestaurantRepository.class);
    private final UpdateAvatarUseCaseImpl useCase = new UpdateAvatarUseCaseImpl(repository);
    private final UUID ownerId = UUID.randomUUID();
    private final Restaurant restaurant = new Restaurant(RestaurantId.newId(), ownerId, "Cafe", "123",
            RestaurantStatus.OPENING, new Address("Address", 1), 10.0, 106.0,
            ApprovalStatus.APPROVED, Instant.now(), null, null, null, null);

    @Test
    void updatesWhileOpenAndSkipsUnchangedUrl() {
        when(repository.findByIdForUpdate(restaurant.getId().value())).thenReturn(Optional.of(restaurant));
        UpdateAvatarCommand command = new UpdateAvatarCommand(restaurant.getId().value(), ownerId, " https://cdn.example.com/a.jpg ");
        assertThat(useCase.updateAvatar(command).avatarUrl()).isEqualTo("https://cdn.example.com/a.jpg");
        useCase.updateAvatar(command);
        verify(repository, times(1)).updateState(restaurant);
        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.OPENING);
    }

    @Test
    void rejectsForeignOwnerAndMissingRestaurant() {
        when(repository.findByIdForUpdate(any())).thenReturn(Optional.of(restaurant));
        assertThatThrownBy(() -> useCase.updateAvatar(new UpdateAvatarCommand(
                restaurant.getId().value(), UUID.randomUUID(), "https://cdn.example.com/a.jpg")))
                .isInstanceOf(BusinessException.class);
        when(repository.findByIdForUpdate(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.updateAvatar(new UpdateAvatarCommand(
                restaurant.getId().value(), ownerId, "https://cdn.example.com/a.jpg")))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).updateState(any());
    }

    @Test
    void rejectsPendingRestaurant() {
        Restaurant pending = Restaurant.register(ownerId, "Cafe", "123", RestaurantStatus.CLOSED,
                new Address("Address", 1), null, null);
        assertThatThrownBy(() -> pending.updateAvatar("https://cdn.example.com/a.jpg"))
                .isInstanceOf(BusinessException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "javascript:alert(1)", "/avatar.jpg", "https://", "https://user:pass@example.com/a", "https://example.com/a b"})
    void rejectsInvalidUrl(String url) {
        assertThatThrownBy(() -> restaurant.updateAvatar(url)).isInstanceOf(BusinessException.class);
        assertThat(restaurant.getAvatarUrl()).isNull();
    }

    @Test
    void rejectsOversizedUrl() {
        assertThatThrownBy(() -> restaurant.updateAvatar("https://example.com/" + "a".repeat(2048)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void persistenceMapperRoundTripsAvatar() {
        restaurant.updateAvatar("https://cdn.example.com/a.jpg");
        WardJpaEntity ward = new WardJpaEntity();
        ward.setCode(1);
        RestaurantPersistenceMapper mapper = Mappers.getMapper(RestaurantPersistenceMapper.class);
        RestaurantJpaEntity entity = mapper.toEntity(restaurant, ward);
        assertThat(entity.getAvatarUrl()).isEqualTo(restaurant.getAvatarUrl());
        assertThat(mapper.toDomain(entity).getAvatarUrl()).isEqualTo(restaurant.getAvatarUrl());
        restaurant.updateAvatar("https://cdn.example.com/b.jpg");
        mapper.updateState(restaurant, entity, ward);
        assertThat(entity.getAvatarUrl()).isEqualTo(restaurant.getAvatarUrl());
    }
}
