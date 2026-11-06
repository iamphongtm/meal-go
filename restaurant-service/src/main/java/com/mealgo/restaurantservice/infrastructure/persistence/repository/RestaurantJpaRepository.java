package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RestaurantJpaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
    Optional<RestaurantJpaEntity> findByOwnerId(UUID ownerId);

    boolean existsByOwnerId(UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("select r.name from RestaurantJpaEntity r where r.id = :id and r.ownerId = :ownerId")
    Optional<String> findNameByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
}
