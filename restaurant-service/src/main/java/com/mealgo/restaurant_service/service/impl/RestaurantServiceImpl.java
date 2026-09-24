package com.mealgo.restaurant_service.service.impl;

import com.mealgo.restaurant_service.domain.enums.CategoryStatus;
import com.mealgo.restaurant_service.domain.enums.OptionGroupStatus;
import com.mealgo.restaurant_service.domain.enums.OptionStatus;
import com.mealgo.restaurant_service.domain.enums.RestaurantStatus;
import com.mealgo.restaurant_service.domain.model.Address;
import com.mealgo.restaurant_service.domain.model.MenuCategory;
import com.mealgo.restaurant_service.domain.model.MenuItem;
import com.mealgo.restaurant_service.domain.model.Option;
import com.mealgo.restaurant_service.domain.model.OptionGroup;
import com.mealgo.restaurant_service.domain.model.Restaurant;
import com.mealgo.restaurant_service.domain.model.Ward;
import com.mealgo.restaurant_service.domain.repository.MenuCategoryRepository;
import com.mealgo.restaurant_service.domain.repository.MenuItemRepository;
import com.mealgo.restaurant_service.domain.repository.OptionGroupRepository;
import com.mealgo.restaurant_service.domain.repository.OptionRepository;
import com.mealgo.restaurant_service.domain.repository.ProvinceRepository;
import com.mealgo.restaurant_service.domain.repository.RestaurantRepository;
import com.mealgo.restaurant_service.domain.repository.WardRepository;
import com.mealgo.restaurant_service.dto.request.CategoryCreationRequest;
import com.mealgo.restaurant_service.dto.request.MenuCreationRequest;
import com.mealgo.restaurant_service.dto.request.OptionCreationRequest;
import com.mealgo.restaurant_service.dto.request.OptionGroupCreationRequest;
import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;
import com.mealgo.restaurant_service.dto.response.CategoryCreationResponse;
import com.mealgo.restaurant_service.dto.response.OptionCreationResponse;
import com.mealgo.restaurant_service.dto.response.OptionGroupCreationResponse;
import com.mealgo.restaurant_service.exception.BusinessException;
import com.mealgo.restaurant_service.exception.ErrorCode;
import com.mealgo.restaurant_service.service.MenuInitializationService;
import com.mealgo.restaurant_service.service.RestaurantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RESTAURANT-SERVICE")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RestaurantServiceImpl implements RestaurantService, MenuInitializationService {
    RestaurantRepository restaurantRepository;
    WardRepository wardRepository;
    ProvinceRepository provinceRepository;
    MenuCategoryRepository categoryRepository;
    MenuItemRepository menuItemRepository;
    OptionGroupRepository optionGroupRepository;
    OptionRepository optionRepository;

    @Override
    public void init(RestaurantCreationRequest request, UUID ownerId) {
        log.debug("Initializing restaurant: ownerId={}", ownerId);

        Ward ward = wardRepository.findByCode(request.wardCode()).orElseThrow(
                () -> new BusinessException(ErrorCode.WARD_NOT_FOUND, request.wardCode().toString())
        );

        provinceRepository.findByCode(request.provinceCode()).orElseThrow(
                () -> new BusinessException(ErrorCode.PROVINCE_NOT_FOUND, request.provinceCode().toString())
        );

        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .ownerId(ownerId)
                .name(request.name())
                .phone(request.phone())
                .status(RestaurantStatus.OPENING)
                .address(Address.builder()
                        .addressLine(request.addressLine())
                        .ward(ward)
                        .build())
                .build());

        log.info("Restaurant initialized successfully: restaurantId={}, ownerId={}",
                restaurant.getId(), ownerId);
    }

    @Override
    public CategoryCreationResponse createCategory(UUID restaurantId, CategoryCreationRequest request) {
        log.debug("Creating menu category: restaurantId={}, categoryName={}", restaurantId, request.getName());

        if (categoryRepository.existsByRestaurantIdAndNameIgnoreCaseAndDeletedAtIsNull(
                restaurantId, request.getName())) {
            throw new BusinessException(ErrorCode.MENU_CATEGORY_CONFLICT, request.getName());
        }

        int displayOrder = getDisplayOrderDefault(
                categoryRepository.getMaxDisplayOrderByRestaurantId(restaurantId));

        MenuCategory category = categoryRepository.save(MenuCategory.builder()
                .name(request.getName())
                .displayOrder(displayOrder)
                .status(CategoryStatus.ACTIVE)
                .restaurant(getRestaurant(restaurantId))
                .build());

        log.info("Menu category created successfully: categoryId={}, restaurantId={}, displayOrder={}",
                category.getId(), restaurantId, category.getDisplayOrder());

        return CategoryCreationResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .displayOrder(category.getDisplayOrder())
                .build();
    }

    @Override
    public void initMenu(UUID restaurantId, MenuCreationRequest request) {
    }

    @Override
    public OptionGroupCreationResponse createOptionGroup(UUID menuItemId, OptionGroupCreationRequest request) {
        log.debug("Creating option group: menuItemId={}, optionGroupName={}", menuItemId, request.name());

        MenuItem item = menuItemRepository.findById(menuItemId).orElseThrow(
                () -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND, menuItemId.toString())
        );

        if (optionGroupRepository.existsByMenuItemIdAndNameIgnoreCaseAndDeletedAtIsNull(
                menuItemId, request.name())) {
            throw new BusinessException(ErrorCode.OPTION_GROUP_CONFLICT, request.name());
        }

        if (request.minSelect() > request.maxSelect()) {
            throw new BusinessException(
                    ErrorCode.OPTION_GROUP_SELECTION_RANGE_INVALID,
                    request.minSelect(),
                    request.maxSelect()
            );
        }

        int displayOrder = getDisplayOrderDefault(
                optionGroupRepository.getMaxDisplayOrderByMenuItemId(menuItemId));

        OptionGroup optionGroup = optionGroupRepository.save(OptionGroup.builder()
                .menuItem(item)
                .name(request.name())
                .minSelect(request.minSelect())
                .maxSelect(request.maxSelect())
                .displayOrder(displayOrder)
                .status(OptionGroupStatus.ACTIVE)
                .build());

        log.info("Option group created successfully: optionGroupId={}, menuItemId={}, displayOrder={}",
                optionGroup.getId(), menuItemId, optionGroup.getDisplayOrder());

        return OptionGroupCreationResponse.builder()
                .id(optionGroup.getId())
                .name(optionGroup.getName())
                .minSelect(optionGroup.getMinSelect())
                .maxSelect(optionGroup.getMaxSelect())
                .build();
    }

    @Override
    public OptionCreationResponse createOption(UUID optionGroupId, OptionCreationRequest request) {
        log.debug("Creating option: optionGroupId={}, optionName={}", optionGroupId, request.name());

        OptionGroup optionGroup = optionGroupRepository.findById(optionGroupId).orElseThrow(
                () -> new BusinessException(ErrorCode.OPTION_GROUP_NOT_FOUND, optionGroupId.toString())
        );

        if (optionRepository.existsByOptionGroupIdAndNameIgnoreCaseAndDeletedAtIsNull(
                optionGroupId, request.name())) {
            throw new BusinessException(ErrorCode.OPTION_CONFLICT, request.name());
        }

        int displayOrder = getDisplayOrderDefault(
                optionRepository.getMaxDisplayOrderByOptionGroupId(optionGroupId));

        Option option = optionRepository.save(Option.builder()
                .optionGroup(optionGroup)
                .name(request.name())
                .additionalPrice(request.additionalPrice())
                .status(OptionStatus.ACTIVE)
                .displayOrder(displayOrder)
                .build());

        log.info("Option created successfully: optionId={}, optionGroupId={}, displayOrder={}",
                option.getId(), optionGroupId, option.getDisplayOrder());

        return OptionCreationResponse.builder()
                .id(option.getId())
                .name(option.getName())
                .additionalPrice(option.getAdditionalPrice())
                .build();
    }

    private int getDisplayOrderDefault(Integer maxDisplayOrder) {
        return maxDisplayOrder == null ? 10 : maxDisplayOrder + 10;
    }

    private Restaurant getRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new BusinessException(ErrorCode.RESTAURANT_NOT_FOUND, restaurantId.toString()));
    }
}
