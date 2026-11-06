package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupItems;
import com.mealgo.restaurantservice.application.port.out.OptionGroupItemsRepository;
import com.mealgo.restaurantservice.domain.enums.OptionGroupStatus;
import com.mealgo.restaurantservice.domain.enums.OptionStatus;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionGroupJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuItemOptionPriceJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.OptionGroupJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.OptionJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.MenuItemJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.MenuItemOptionGroupJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.MenuItemOptionPriceJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.OptionGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.MENU_ITEM_CONFLICT;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.MENU_ITEM_NOT_FOUND;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.OPTION_CONFLICT;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.OPTION_GROUP_CONFIGURATION_INVALID;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.OPTION_GROUP_NOT_FOUND;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.OPTION_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class OptionGroupItemsAdapter implements OptionGroupItemsRepository {
    private final OptionGroupJpaRepository optionGroupRepository;
    private final MenuItemJpaRepository menuItemRepository;
    private final MenuItemOptionGroupJpaRepository itemOptionGroupRepository;
    private final MenuItemOptionPriceJpaRepository optionPriceRepository;

    @Override
    public List<OptionGroupItems> findAll(UUID restaurantId) {
        List<MenuItemOptionGroupJpaEntity> assignments = itemOptionGroupRepository
                .findByMenuItemCategoryRestaurantId(restaurantId);
        Map<UUID, List<MenuItemOptionGroupJpaEntity>> assignmentsByGroupId = assignments.stream()
                .collect(Collectors.groupingBy(MenuItemOptionGroupJpaEntity::getOptionGroupId));

        return optionGroupRepository.findByRestaurantIdOrderByNameAsc(restaurantId).stream()
                .filter(group -> group.getStatus() == OptionGroupStatus.ACTIVE)
                .map(group -> toOptionGroupItems(
                        group, assignmentsByGroupId.getOrDefault(group.getId(), List.of())))
                .toList();
    }

    private OptionGroupItems toOptionGroupItems(
            OptionGroupJpaEntity group,
            List<MenuItemOptionGroupJpaEntity> assignments) {

        List<OptionGroupItems.OptionView> options = group.getOptions().stream()
                .filter(option -> option.getStatus() == OptionStatus.ACTIVE)
                .map(option -> new OptionGroupItems.OptionView(option.getId(), option.getName()))
                .toList();

        List<OptionGroupItems.ItemConfiguration> configurations = assignments.stream()
                .map(this::toItemConfiguration)
                .toList();

        return new OptionGroupItems(group.getId(), group.getName(), options, configurations);
    }

    private OptionGroupItems.ItemConfiguration toItemConfiguration(
            MenuItemOptionGroupJpaEntity assignment) {

        List<OptionGroupItems.OptionPrice> optionPrices = assignment.getOptionPrices().stream()
                .map(price -> new OptionGroupItems.OptionPrice(
                        price.getOptionId(), price.getAdditionalPrice()))
                .toList();

        return new OptionGroupItems.ItemConfiguration(
                assignment.getMenuItem().getId(),
                assignment.getMinSelect(),
                assignment.getMaxSelect(),
                optionPrices);
    }

    @Override
    public void replaceItems(UUID restaurantId, UUID groupId, List<OptionGroupItems.ItemConfiguration> requested) {
        OptionGroupJpaEntity group = optionGroupRepository.findByIdAndRestaurantId(groupId, restaurantId)
                .orElseThrow(() -> new BusinessException(OPTION_GROUP_NOT_FOUND, groupId));
        List<OptionJpaEntity> activeOptions = requireConfigurableOptions(group);
        Set<UUID> requestedItemIds = validateConfigurations(groupId, activeOptions, requested);
        Map<UUID, MenuItemJpaEntity> itemsById = findMenuItemsById(restaurantId, requestedItemIds);
        List<MenuItemOptionGroupJpaEntity> existingAssignments = itemOptionGroupRepository
                .findByMenuItemCategoryRestaurantIdAndOptionGroupId(restaurantId, groupId);
        Map<UUID, MenuItemOptionGroupJpaEntity> assignmentsByItemId = existingAssignments.stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getMenuItem().getId(), Function.identity()));

        removeUnrequestedAssignments(existingAssignments, requestedItemIds);

        for (OptionGroupItems.ItemConfiguration configuration : requested) {
            MenuItemOptionGroupJpaEntity assignment = assignmentsByItemId.get(configuration.menuItemId());
            if (assignment == null) {
                assignment = createAssignment(groupId, itemsById.get(configuration.menuItemId()));
            }
            assignment.setMinSelect(configuration.minSelect());
            assignment.setMaxSelect(configuration.maxSelect());

            MenuItemOptionGroupJpaEntity savedAssignment = itemOptionGroupRepository.save(assignment);
            updateOptionPrices(savedAssignment, activeOptions, configuration.optionPrices());
        }
    }

    private List<OptionJpaEntity> requireConfigurableOptions(OptionGroupJpaEntity group) {
        List<OptionJpaEntity> activeOptions = group.getOptions().stream()
                .filter(option -> option.getStatus() == OptionStatus.ACTIVE)
                .toList();
        if (group.getStatus() != OptionGroupStatus.ACTIVE || activeOptions.isEmpty()) {
            throw new BusinessException(OPTION_GROUP_CONFIGURATION_INVALID, group.getId());
        }
        return activeOptions;
    }

    private Set<UUID> validateConfigurations(
            UUID groupId,
            List<OptionJpaEntity> activeOptions,
            List<OptionGroupItems.ItemConfiguration> configurations) {
        Set<UUID> activeOptionIds = activeOptions.stream()
                .map(OptionJpaEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> itemIds = new HashSet<>();
        for (OptionGroupItems.ItemConfiguration configuration : configurations) {
            if (!itemIds.add(configuration.menuItemId())) {
                throw new BusinessException(MENU_ITEM_CONFLICT, configuration.menuItemId());
            }
            if (configuration.minSelect() < 0
                    || configuration.maxSelect() < 1
                    || configuration.minSelect() > configuration.maxSelect()
                    || configuration.maxSelect() > activeOptions.size()) {
                throw new BusinessException(OPTION_GROUP_CONFIGURATION_INVALID, groupId);
            }

            validateOptionPrices(activeOptionIds, configuration.optionPrices());
        }
        return itemIds;
    }

    private void validateOptionPrices(
            Set<UUID> activeOptionIds,
            List<OptionGroupItems.OptionPrice> optionPrices) {
        Set<UUID> seenOptionIds = new HashSet<>();
        for (OptionGroupItems.OptionPrice price : optionPrices) {
            if (!activeOptionIds.contains(price.optionId())) {
                throw new BusinessException(OPTION_NOT_FOUND, price.optionId());
            }
            if (!seenOptionIds.add(price.optionId()) || price.additionalPrice().signum() < 0) {
                throw new BusinessException(OPTION_CONFLICT, price.optionId());
            }
        }
    }

    private Map<UUID, MenuItemJpaEntity> findMenuItemsById(UUID restaurantId, Set<UUID> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, MenuItemJpaEntity> itemsById = menuItemRepository
                .findByCategoryRestaurantIdAndIdIn(restaurantId, itemIds).stream()
                .collect(Collectors.toMap(MenuItemJpaEntity::getId, Function.identity()));
        for (UUID itemId : itemIds) {
            if (!itemsById.containsKey(itemId)) {
                throw new BusinessException(MENU_ITEM_NOT_FOUND, itemId);
            }
        }
        return itemsById;
    }

    private void removeUnrequestedAssignments(
            List<MenuItemOptionGroupJpaEntity> existingAssignments,
            Set<UUID> requestedItemIds) {
        for (MenuItemOptionGroupJpaEntity assignment : existingAssignments) {
            if (!requestedItemIds.contains(assignment.getMenuItem().getId())) {
                optionPriceRepository.deleteAll(
                        optionPriceRepository.findByMenuItemOptionGroupId(assignment.getId()));
                // Delete child prices before their parent assignment to respect the foreign key.
                optionPriceRepository.flush();
                itemOptionGroupRepository.delete(assignment);
            }
        }
        itemOptionGroupRepository.flush();
    }

    private MenuItemOptionGroupJpaEntity createAssignment(UUID groupId, MenuItemJpaEntity item) {
        int nextDisplayOrder = item.getOptionGroups().stream()
                .mapToInt(MenuItemOptionGroupJpaEntity::getDisplayOrder)
                .max()
                .orElse(0) + 1;

        MenuItemOptionGroupJpaEntity assignment = new MenuItemOptionGroupJpaEntity();
        assignment.setId(UUID.randomUUID());
        assignment.setMenuItem(item);
        assignment.setOptionGroupId(groupId);
        assignment.setDisplayOrder(nextDisplayOrder);
        return assignment;
    }

    private void updateOptionPrices(
            MenuItemOptionGroupJpaEntity assignment,
            List<OptionJpaEntity> activeOptions,
            List<OptionGroupItems.OptionPrice> requestedPrices) {
        Map<UUID, MenuItemOptionPriceJpaEntity> existingPricesByOptionId = optionPriceRepository
                .findByMenuItemOptionGroupId(assignment.getId()).stream()
                .collect(Collectors.toMap(MenuItemOptionPriceJpaEntity::getOptionId, Function.identity()));
        Map<UUID, BigDecimal> requestedPricesByOptionId = requestedPrices.stream()
                .collect(Collectors.toMap(
                        OptionGroupItems.OptionPrice::optionId,
                        OptionGroupItems.OptionPrice::additionalPrice));

        for (OptionJpaEntity option : activeOptions) {
            MenuItemOptionPriceJpaEntity price = existingPricesByOptionId.get(option.getId());
            if (price == null) {
                price = new MenuItemOptionPriceJpaEntity();
                price.setId(UUID.randomUUID());
                price.setMenuItemOptionGroup(assignment);
                price.setOptionId(option.getId());
            }
            price.setAdditionalPrice(requestedPricesByOptionId.getOrDefault(option.getId(), BigDecimal.ZERO));
            optionPriceRepository.save(price);
        }
    }
}
