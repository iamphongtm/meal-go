package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.domain.model.category.MenuCategory;

import java.util.Optional;
import java.util.UUID;

public interface MenuCategoryRepository {

    void save(MenuCategory menuCategory);

    boolean existsByIdAndRestaurantId(UUID id, UUID restaurantId);

    boolean existsByRestaurantIdAndName(UUID restaurantId, String name);

    int getMaxDisplayOrder(UUID restaurantId);

    Optional<MenuCategory> findByIdAndRestaurantId(UUID uuid, UUID uuid1);

    boolean existsByRestaurantIdAndNameAndIdNot(UUID uuid, String name, UUID uuid1);

    void delete(UUID uuid);
}
