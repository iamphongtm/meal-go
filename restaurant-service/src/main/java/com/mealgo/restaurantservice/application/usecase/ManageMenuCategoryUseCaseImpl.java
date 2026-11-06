package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menucategory.DeleteMenuCategoryCommand;
import com.mealgo.restaurantservice.application.port.in.menucategory.ManageMenuCategoryUseCase;
import com.mealgo.restaurantservice.application.port.in.menucategory.CreateMenuCategoryCommand;
import com.mealgo.restaurantservice.application.port.in.menucategory.UpdateMenuCategoryCommand;
import com.mealgo.restaurantservice.application.port.out.MenuCategoryRepository;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.model.category.MenuCategory;
import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageMenuCategoryUseCaseImpl implements ManageMenuCategoryUseCase {
    private final MenuCategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public MenuCategoryId create(CreateMenuCategoryCommand command) {
        log.debug("Create Menu Category Command, restaurantId: {}", command.restaurantId());
        //check Restaurant & Ownership
        if(!restaurantRepository.existsByIdAndOwnerId(command.restaurantId(), command.ownerId()))
            throw new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId());

        // check unique name
        if(categoryRepository.existsByRestaurantIdAndName(command.restaurantId(), command.name()))
            throw new BusinessException(MENU_CATEGORY_CONFLICT, command.name());

        //cal displayOrder
        int displayOrder  = categoryRepository.getMaxDisplayOrder(command.restaurantId()) + 1;
        //create Aggregate
        MenuCategory menuCategory = MenuCategory
                .create(RestaurantId.of(command.restaurantId()),
                        command.name(),
                         displayOrder);
        categoryRepository.save(menuCategory);
        log.info("Create Menu Category successfully, restaurantId: {}", command.restaurantId());
        return menuCategory.getId();
    }

    @Override
    public void update(UpdateMenuCategoryCommand command) {
        if(!restaurantRepository.existsByIdAndOwnerId(command.restaurantId(), command.ownerId()))
            throw new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId());

        MenuCategory category = categoryRepository
                .findByIdAndRestaurantId(
                        command.categoryId(), command.restaurantId())
                .orElseThrow(() -> new BusinessException(
                        MENU_CATEGORY_NOT_FOUND, command.categoryId()));


        if (categoryRepository.existsByRestaurantIdAndNameAndIdNot(
                command.restaurantId(),
                command.name(),
                command.categoryId())) {
            throw new BusinessException(
                    MENU_CATEGORY_CONFLICT, command.name());
        }

        category.rename(command.name());
        categoryRepository.save(category);

        log.info(
                "Menu category update saved: restaurantId={}, categoryId={}, ownerId={}",
                command.restaurantId(),
                command.categoryId(),
                command.ownerId());
    }

    @Override
    public void delete(DeleteMenuCategoryCommand command) {
        if (!restaurantRepository.existsByIdAndOwnerId(
                command.restaurantId(), command.ownerId())) {
            throw new BusinessException(
                    RESTAURANT_NOT_FOUND, command.restaurantId());
        }

        categoryRepository.delete(command.categoryId());
        log.info("Delete Menu Category successfully, categoryId: {}", command.categoryId());
    }
}
