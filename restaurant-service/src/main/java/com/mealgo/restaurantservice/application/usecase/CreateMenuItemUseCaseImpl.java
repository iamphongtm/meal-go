package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.event.EventType;
import com.mealgo.restaurantservice.application.event.MenuItemCreatedEvent;
import com.mealgo.restaurantservice.application.port.in.menuitem.CreateMenuItemCommand;
import com.mealgo.restaurantservice.application.port.in.menuitem.CreateMenuItemUseCase;
import com.mealgo.restaurantservice.application.port.out.*;
import com.mealgo.restaurantservice.domain.enums.OptionGroupStatus;
import com.mealgo.restaurantservice.domain.enums.OptionStatus;
import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItem;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItemId;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItemOptionGroup;
import com.mealgo.restaurantservice.domain.model.menuitem.MenuItemOptionPrice;
import com.mealgo.restaurantservice.domain.model.optiongroup.Option;
import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroup;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.mealgo.restaurantservice.application.event.EventType.MENU_ITEM_CREATED;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CreateMenuItemUseCaseImpl implements CreateMenuItemUseCase {
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final MenuItemRepository menuItemRepository;
    private final OutboxEventStore outboxEventStore;

    @Override
    @Transactional
    public MenuItemId createMenuItem(CreateMenuItemCommand command) {
        String restaurantName = restaurantRepository.findNameByIdAndOwnerId(command.restaurantId(), command.ownerId())
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId()));
        if (!menuCategoryRepository.existsByIdAndRestaurantId(command.categoryId(), command.restaurantId())) {
            throw new BusinessException(MENU_CATEGORY_NOT_FOUND, command.categoryId());
        }

        List<MenuItemOptionGroup> groups = createOptionGroups(command);
        int displayOrder = menuItemRepository.getMaxDisplayOrder(command.categoryId()) + 1;
        MenuItem menuItem = MenuItem.create(MenuCategoryId.of(command.categoryId()), command.name(),
                command.description(), command.basePrice(), command.imageUrl(), displayOrder, groups);
        menuItemRepository.save(menuItem);

        outboxEventStore.append(MenuItemCreatedEvent
                .builder()
                .eventId(UUID.randomUUID())
                .eventType(MENU_ITEM_CREATED.value())
                .occurredAt(Instant.now())
                .menuItemId(menuItem.getId().value())
                .restaurantId( command.restaurantId())
                .categoryId(command.categoryId())
                .name( menuItem.getName())
                .basePrice(menuItem.getBasePrice())
                .imageUrl( menuItem.getImageUrl())
                .status(menuItem.getStatus().name())
                .build());
        return menuItem.getId();
    }

    private List<MenuItemOptionGroup> createOptionGroups(CreateMenuItemCommand command) {
        List<MenuItemOptionGroup> groups = new ArrayList<>();
        Set<UUID> groupIds = new HashSet<>();
        for (CreateMenuItemCommand.OptionGroupCommand requested : command.optionGroups()) {
            UUID groupId = requested.optionGroupId();
            if (!groupIds.add(groupId)) {
                throw new BusinessException(OPTION_GROUP_DUPLICATE, groupId);
            }

            OptionGroup group = optionGroupRepository.findByIdAndRestaurantId(groupId, command.restaurantId())
                    .orElseThrow(() -> new BusinessException(OPTION_GROUP_NOT_FOUND, groupId));

            if (requested.minSelect() == null || requested.maxSelect() == null
                    || requested.minSelect() < 0 || requested.maxSelect() < 0) {
                throw new BusinessException(OPTION_GROUP_CONFIGURATION_INVALID, groupId);
            }

            if (requested.minSelect() > requested.maxSelect()) {
                throw new BusinessException(OPTION_GROUP_SELECTION_RANGE_INVALID,
                        requested.minSelect(), requested.maxSelect());
            }

            List<Option> activeOptions = group.getOptions().stream()
                    .filter(option -> option.getStatus() == OptionStatus.ACTIVE).toList();

            if (group.getStatus() != OptionGroupStatus.ACTIVE || activeOptions.isEmpty()
                    || requested.maxSelect() > activeOptions.size()) {
                throw new BusinessException(OPTION_GROUP_CONFIGURATION_INVALID, groupId);
            }

            Set<UUID> optionIds = new HashSet<>();

            group.getOptions().forEach(option -> optionIds.add(option.getId()));

            Map<UUID, BigDecimal> prices = new HashMap<>();

            for (CreateMenuItemCommand.OptionPriceCommand price : requested.optionPrices()) {
                if (!optionIds.contains(price.optionId())) {
                    throw new BusinessException(OPTION_NOT_FOUND, price.optionId());
                }
                if (prices.putIfAbsent(price.optionId(), price.additionalPrice()) != null) {
                    throw new BusinessException(OPTION_CONFLICT, price.optionId());
                }
            }

            // Every option gets an item-specific price; omitted prices default to zero.
            List<MenuItemOptionPrice> optionPrices = group.getOptions().stream()
                    .map(option -> new MenuItemOptionPrice(UUID.randomUUID(), option.getId(),
                            prices.getOrDefault(option.getId(), BigDecimal.ZERO)))
                    .toList();
            groups.add(new MenuItemOptionGroup(UUID.randomUUID(), group.getId(), requested.minSelect(),
                    requested.maxSelect(), groups.size() + 1, optionPrices));
        }
        return groups;
    }
}
