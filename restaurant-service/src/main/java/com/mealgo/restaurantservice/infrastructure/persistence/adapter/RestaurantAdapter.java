package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import org.springframework.transaction.annotation.Transactional;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.RestaurantPersistenceMapper;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.WardJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.WardJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantAdapter implements RestaurantRepository {
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final WardJpaRepository wardJpaRepository;
    private final RestaurantPersistenceMapper mapper;

    @Override
    public Optional<Restaurant> findByIdForUpdate(UUID id) {
        return restaurantJpaRepository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Restaurant> findById(UUID id) {
        return restaurantJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void updateState(Restaurant restaurant) {
        RestaurantJpaEntity entity = restaurantJpaRepository.findById(restaurant.getId().value()).orElseThrow();
        mapper.updateState(restaurant, entity, wardJpaRepository.getReferenceById(restaurant.getAddress().wardCode()));
    }

    @Override
    public void save(Restaurant restaurant) {
        WardJpaEntity wardReference = wardJpaRepository.getReferenceById(restaurant.getAddress().wardCode());

        RestaurantJpaEntity entity = mapper.toEntity(restaurant, wardReference);

        restaurantJpaRepository.save(entity);
    }

    @Override
    public boolean existByOwnerId(UUID ownerId) {
        return restaurantJpaRepository.existsByOwnerId(ownerId);
    }

    @Override
    public boolean existsByIdAndOwnerId(UUID id, UUID restaurantId) {
        return findById(id).filter(value -> value.getOwnerId().equals(restaurantId)).map(value -> {
            value.requireApproved();
            return true;
        }).orElse(false);
    }

    @Override
    public Optional<String> findNameByIdAndOwnerId(UUID id, UUID ownerId) {
        return findById(id).filter(value -> value.getOwnerId().equals(ownerId)).map(value -> {
            value.requireApproved();
            return value.getName();
        });
    }
}
