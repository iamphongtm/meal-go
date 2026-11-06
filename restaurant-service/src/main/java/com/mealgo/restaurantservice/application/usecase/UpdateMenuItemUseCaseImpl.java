package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menuitem.*;
import com.mealgo.restaurantservice.application.port.out.*;
import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItem;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UpdateMenuItemUseCaseImpl implements UpdateMenuItemUseCase {
    private final RestaurantRepository restaurants;
    private final MenuCategoryRepository categories;
    private final MenuItemRepository items;

    private MenuItem ownedItem(UUID restaurantId, UUID ownerId, UUID itemId) {
        if (!restaurants.existsByIdAndOwnerId(restaurantId, ownerId)) {
            throw new BusinessException(RESTAURANT_NOT_FOUND, restaurantId);
        }
        return items.findByIdForUpdate(restaurantId, itemId)
                .orElseThrow(() -> new BusinessException(MENU_ITEM_NOT_FOUND, itemId));
    }

    @Override
    @Transactional
    public void update(UpdateMenuItemCommand command) {
        var item = ownedItem(command.restaurantId(), command.ownerId(), command.menuItemId());
        if (!categories.existsByIdAndRestaurantId(command.categoryId(), command.restaurantId())) {
            throw new BusinessException(MENU_CATEGORY_NOT_FOUND, command.categoryId());
        }
        int order = item.getCategoryId().value().equals(command.categoryId()) ? item.getDisplayOrder()
                : items.getMaxDisplayOrder(command.categoryId()) + 1;
        items.updateDetails(item.revise(MenuCategoryId.of(command.categoryId()), command.name().trim(),
                command.description() == null ? null : command.description().trim(), command.basePrice(),
                command.imageUrl(), order));
    }

    @Override
    @Transactional
    public void updateStatus(UpdateMenuItemStatusCommand command) {
        var item = ownedItem(command.restaurantId(), command.ownerId(), command.menuItemId());
        items.updateDetails(item.changeStatus(command.status()));
    }
}
