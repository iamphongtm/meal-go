package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.out.MenuCategoryRepository;
import com.mealgo.restaurantservice.domain.model.category.MenuCategory;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.MenuCategoryPersistenceMapper;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.MenuCategoryJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MenuCategoryAdapter implements MenuCategoryRepository {
    private final MenuCategoryJpaRepository menuCategoryJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;

    @Override
    public boolean existsByIdAndRestaurantId(UUID id, UUID restaurantId) {
        return menuCategoryJpaRepository.existsByIdAndRestaurantId(id, restaurantId);
    }

    @Override
    public void save(MenuCategory menuCategory) {
        var restaurantReference = restaurantJpaRepository.getReferenceById(menuCategory.getRestaurantId().value());
        var entity = MenuCategoryPersistenceMapper.toNewEntity(menuCategory, restaurantReference);
        menuCategoryJpaRepository.save(entity);
    }

    @Override
    public boolean existsByRestaurantIdAndName(UUID restaurantId, String name) {
        return menuCategoryJpaRepository.existsByRestaurantIdAndName(restaurantId, name);
    }

    @Override
    public int getMaxDisplayOrder(UUID restaurantId) {
        return menuCategoryJpaRepository.getMaxDisplayOrderByRestaurantId(restaurantId);
    }
}
