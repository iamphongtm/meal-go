package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.out.MenuItemRepository;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItem;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.MenuItemPersistenceMapper;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.MenuCategoryJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.MenuItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MenuItemAdapter implements MenuItemRepository {
    private final MenuItemJpaRepository menuItemJpaRepository;
    private final MenuCategoryJpaRepository menuCategoryJpaRepository;

    @Override
    public int getMaxDisplayOrder(UUID categoryId) {
        return menuItemJpaRepository.getMaxDisplayOrderByCategoryId(categoryId);
    }

    @Override
    public void save(MenuItem menuItem) {
        var categoryReference = menuCategoryJpaRepository.getReferenceById(menuItem.getCategoryId().value());
        menuItemJpaRepository.save(MenuItemPersistenceMapper.toNewEntity(menuItem, categoryReference));
    }

    @Override
    public java.util.Optional<MenuItem> findByIdForUpdate(UUID restaurantId, UUID menuItemId) {
        return menuItemJpaRepository.findMerchantItemForUpdate(restaurantId, menuItemId)
                .map(MenuItemPersistenceMapper::toDomain);
    }

    @Override
    public void updateDetails(MenuItem item) {
        var entity = menuItemJpaRepository.getReferenceById(item.getId().value());
        entity.setCategory(menuCategoryJpaRepository.getReferenceById(item.getCategoryId().value()));
        entity.setName(item.getName());
        entity.setDescription(item.getDescription());
        entity.setBasePrice(item.getBasePrice());
        entity.setImageUrl(item.getImageUrl());
        entity.setStatus(item.getStatus());
        entity.setDisplayOrder(item.getDisplayOrder());
        // Do not replace optionGroups: this operation updates scalar fields only.
        menuItemJpaRepository.save(entity);
    }
}
