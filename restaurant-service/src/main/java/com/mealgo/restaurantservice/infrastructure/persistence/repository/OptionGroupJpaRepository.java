package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.OptionGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface OptionGroupJpaRepository extends JpaRepository<OptionGroupJpaEntity, UUID> {
    @EntityGraph(attributePaths = "options")
    java.util.List<OptionGroupJpaEntity> findByRestaurantIdAndIdIn(
            UUID restaurantId, java.util.Collection<UUID> ids);

    boolean existsByRestaurantIdAndName(UUID restaurantId, String name);

    @EntityGraph(attributePaths = "options")
    Optional<OptionGroupJpaEntity> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
