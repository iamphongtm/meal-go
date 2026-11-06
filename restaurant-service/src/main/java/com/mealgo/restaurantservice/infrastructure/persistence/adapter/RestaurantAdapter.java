package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import org.springframework.transaction.annotation.Transactional;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.RestaurantPersistenceMapper;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.WardJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantAdapter implements RestaurantRepository {
    @Override
    @Transactional
    public boolean updateStatus(UUID id, UUID ownerId, RestaurantStatus status, String pauseReason) {
        return restaurantJpaRepository.findByIdAndOwnerId(id, ownerId).map(entity -> {
            entity.setStatus(status);
            entity.setPauseReason(pauseReason);
            return true;
        }).orElse(false);
    }
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final WardJpaRepository wardJpaRepository;

    @Override
    public void save(Restaurant restaurant) {
        var wardReference = wardJpaRepository.getReferenceById(restaurant.getAddress().wardCode());

        var entity = RestaurantPersistenceMapper.toNewEntity(restaurant, wardReference);

        restaurantJpaRepository.save(entity);
    }

    @Override
    public boolean existByOwnerId(UUID ownerId) {
        return restaurantJpaRepository.existsByOwnerId(ownerId);
    }

    @Override
    public boolean existsByIdAndOwnerId(UUID id, UUID restaurantId) {
        return restaurantJpaRepository.existsByIdAndOwnerId(id, restaurantId);
    }

    @Override
    public Optional<String> findNameByIdAndOwnerId(UUID id, UUID ownerId) {
        return restaurantJpaRepository.findNameByIdAndOwnerId(id, ownerId);
    }
}
