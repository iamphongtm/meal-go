package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupItems;
import com.mealgo.restaurantservice.domain.enums.OptionGroupStatus;
import com.mealgo.restaurantservice.domain.enums.OptionStatus;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.*;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OptionGroupItemsAdapterTest {
    private final OptionGroupJpaRepository groups = mock(OptionGroupJpaRepository.class);
    private final MenuItemJpaRepository items = mock(MenuItemJpaRepository.class);
    private final MenuItemOptionGroupJpaRepository links = mock(MenuItemOptionGroupJpaRepository.class);
    private final MenuItemOptionPriceJpaRepository prices = mock(MenuItemOptionPriceJpaRepository.class);
    private final OptionGroupItemsAdapter adapter = new OptionGroupItemsAdapter(groups, items, links, prices);
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID groupId = UUID.randomUUID();
    private final UUID optionId = UUID.randomUUID();

    private void existingGroup() {
        var group = mock(OptionGroupJpaEntity.class);
        var option = new OptionJpaEntity();
        option.setId(optionId);
        option.setStatus(OptionStatus.ACTIVE);
        when(group.getStatus()).thenReturn(OptionGroupStatus.ACTIVE);
        when(group.getOptions()).thenReturn(List.of(option));
        when(groups.findByIdAndRestaurantId(groupId, restaurantId)).thenReturn(Optional.of(group));
    }

    @Test
    void rejectsGroupOutsideRestaurant() {
        assertThrows(BusinessException.class, () -> adapter.replaceItems(restaurantId, groupId, List.of()));
        verifyNoInteractions(items, links, prices);
    }

    @Test
    void rejectsItemOutsideRestaurantBeforeWriting() {
        existingGroup();
        var configuration = new OptionGroupItems.ItemConfiguration(UUID.randomUUID(), 0, 1, List.of());
        assertThrows(BusinessException.class, () -> adapter.replaceItems(restaurantId, groupId, List.of(configuration)));
        verifyNoInteractions(links, prices);
    }

    @Test
    void rejectsOptionsFromAnotherGroupBeforeWriting() {
        existingGroup();
        var configuration = new OptionGroupItems.ItemConfiguration(UUID.randomUUID(), 0, 1,
                List.of(new OptionGroupItems.OptionPrice(UUID.randomUUID(), BigDecimal.ONE)));
        assertThrows(BusinessException.class, () -> adapter.replaceItems(restaurantId, groupId, List.of(configuration)));
        verifyNoInteractions(items, links, prices);
    }

    @Test
    void rejectsInvalidSelectionRange() {
        existingGroup();
        var configuration = new OptionGroupItems.ItemConfiguration(UUID.randomUUID(), 2, 1, List.of());
        assertThrows(BusinessException.class, () -> adapter.replaceItems(restaurantId, groupId, List.of(configuration)));
        verifyNoInteractions(items, links, prices);
    }

    @Test
    void updatesExistingLinkAndItemSpecificPriceWithoutCreatingDuplicates() {
        existingGroup();
        var item = new MenuItemJpaEntity();
        item.setId(UUID.randomUUID());
        var link = new MenuItemOptionGroupJpaEntity();
        link.setId(UUID.randomUUID());
        link.setMenuItem(item);
        link.setOptionGroupId(groupId);
        var price = new MenuItemOptionPriceJpaEntity();
        price.setOptionId(optionId);
        when(items.findByCategoryRestaurantIdAndIdIn(eq(restaurantId), any())).thenReturn(List.of(item));
        when(links.findByMenuItemCategoryRestaurantIdAndOptionGroupId(restaurantId, groupId)).thenReturn(List.of(link));
        when(links.save(link)).thenReturn(link);
        when(prices.findByMenuItemOptionGroupId(link.getId())).thenReturn(List.of(price));
        adapter.replaceItems(restaurantId, groupId, List.of(new OptionGroupItems.ItemConfiguration(
                item.getId(), 1, 1, List.of(new OptionGroupItems.OptionPrice(optionId, new BigDecimal("5000"))))));
        assertEquals(1, link.getMinSelect());
        assertEquals(new BigDecimal("5000"), price.getAdditionalPrice());
        verify(prices).save(price);
        verify(links, never()).delete(any());
    }

    @Test
    void removesOnlyTheRequestedGroupsLinksAndTheirPrices() {
        existingGroup();
        var link = new MenuItemOptionGroupJpaEntity();
        link.setId(UUID.randomUUID());
        var item = new MenuItemJpaEntity();
        item.setId(UUID.randomUUID());
        link.setMenuItem(item);
        var price = new MenuItemOptionPriceJpaEntity();
        when(links.findByMenuItemCategoryRestaurantIdAndOptionGroupId(restaurantId, groupId)).thenReturn(List.of(link));
        when(prices.findByMenuItemOptionGroupId(link.getId())).thenReturn(List.of(price));
        adapter.replaceItems(restaurantId, groupId, List.of());
        verify(prices).deleteAll(List.of(price));
        verify(links).delete(link);
        verify(links, never()).deleteAll();
    }
}
