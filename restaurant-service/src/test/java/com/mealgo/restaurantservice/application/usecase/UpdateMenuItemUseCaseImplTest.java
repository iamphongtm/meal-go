package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menuitem.*;
import com.mealgo.restaurantservice.application.port.out.*;
import com.mealgo.restaurantservice.domain.enums.MenuItemStatus;
import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;
import com.mealgo.restaurantservice.domain.model.menuitem.*;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateMenuItemUseCaseImplTest {
    private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
    private final MenuCategoryRepository categories = mock(MenuCategoryRepository.class);
    private final MenuItemRepository items = mock(MenuItemRepository.class);
    private final UpdateMenuItemUseCase service = new UpdateMenuItemUseCaseImpl(restaurants, categories, items);
    private final UUID restaurantId = UUID.randomUUID(), ownerId = UUID.randomUUID(), itemId = UUID.randomUUID(), categoryId = UUID.randomUUID();
    private final MenuItemOptionGroup group = new MenuItemOptionGroup(UUID.randomUUID(),
            com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroupId.newId(), 0, 1, 1, List.of());

    private void ownedItem() {
        when(restaurants.existsByIdAndOwnerId(restaurantId, ownerId)).thenReturn(true);
        when(items.findByIdForUpdate(restaurantId, itemId)).thenReturn(Optional.of(new MenuItem(
                MenuItemId.of(itemId), MenuCategoryId.of(categoryId), "Old", "Description", BigDecimal.TEN,
                "old.webp", MenuItemStatus.ACTIVE, 4, List.of(group))));
    }

    @Test
    void rejectsWrongOwnerBeforeAccessingItem() {
        assertThrows(BusinessException.class, () -> service.updateStatus(new UpdateMenuItemStatusCommand(
                restaurantId, ownerId, itemId, MenuItemStatus.SOLD_OUT)));
        verifyNoInteractions(items);
    }

    @Test
    void rejectsItemOutsideRestaurant() {
        when(restaurants.existsByIdAndOwnerId(restaurantId, ownerId)).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.updateStatus(new UpdateMenuItemStatusCommand(
                restaurantId, ownerId, itemId, MenuItemStatus.ACTIVE)));
        verify(items, never()).updateDetails(any());
    }

    @Test
    void rejectsCategoryOutsideRestaurant() {
        ownedItem();
        assertThrows(BusinessException.class, () -> service.update(new UpdateMenuItemCommand(
                restaurantId, ownerId, itemId, UUID.randomUUID(), "New", null, BigDecimal.ONE, null)));
        verify(items, never()).updateDetails(any());
    }

    @Test
    void editsDetailsWithoutChangingGroupsStatusOrOrder() {
        ownedItem();
        when(categories.existsByIdAndRestaurantId(categoryId, restaurantId)).thenReturn(true);
        service.update(new UpdateMenuItemCommand(restaurantId, ownerId, itemId, categoryId, " New ", null, BigDecimal.ONE, null));
        var captured = ArgumentCaptor.forClass(MenuItem.class);
        verify(items).updateDetails(captured.capture());
        var updated = captured.getValue();
        assertEquals("New", updated.getName());
        assertNull(updated.getImageUrl());
        assertEquals(4, updated.getDisplayOrder());
        assertEquals(MenuItemStatus.ACTIVE, updated.getStatus());
        assertEquals(List.of(group), updated.getOptionGroups());
    }

    @Test
    void movingCategoriesAppendsItemToNewCategory() {
        ownedItem();
        var newCategory = UUID.randomUUID();
        when(categories.existsByIdAndRestaurantId(newCategory, restaurantId)).thenReturn(true);
        when(items.getMaxDisplayOrder(newCategory)).thenReturn(8);
        service.update(new UpdateMenuItemCommand(restaurantId, ownerId, itemId, newCategory, "New", "", BigDecimal.ONE, "new.webp"));
        var captured = ArgumentCaptor.forClass(MenuItem.class);
        verify(items).updateDetails(captured.capture());
        assertEquals(newCategory, captured.getValue().getCategoryId().value());
        assertEquals(9, captured.getValue().getDisplayOrder());
    }

    @Test
    void changesStatusWithoutChangingDetailsOrGroups() {
        ownedItem();
        service.updateStatus(new UpdateMenuItemStatusCommand(restaurantId, ownerId, itemId, MenuItemStatus.SOLD_OUT));
        var captured = ArgumentCaptor.forClass(MenuItem.class);
        verify(items).updateDetails(captured.capture());
        assertEquals(MenuItemStatus.SOLD_OUT, captured.getValue().getStatus());
        assertEquals("Old", captured.getValue().getName());
        assertEquals(List.of(group), captured.getValue().getOptionGroups());
    }
}
