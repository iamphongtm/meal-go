package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menucategory.CreateMenuCategoryCommand;
import com.mealgo.restaurantservice.application.port.out.MenuCategoryRepository;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.model.category.MenuCategory;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManageMenuCategoryUseCaseImplTest {

    private MenuCategoryRepository categoryRepository;
    private RestaurantRepository restaurantRepository;
    private ManageMenuCategoryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(MenuCategoryRepository.class);
        restaurantRepository = mock(RestaurantRepository.class);
        useCase = new ManageMenuCategoryUseCaseImpl(categoryRepository, restaurantRepository);
    }

    @Test
    void allowsSameNameInDifferentRestaurants() {
        UUID firstRestaurant = UUID.randomUUID();
        UUID secondRestaurant = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String name = "Bún bò";
        when(categoryRepository.existsByRestaurantIdAndName(firstRestaurant, name)).thenReturn(true);
        when(categoryRepository.existsByRestaurantIdAndName(secondRestaurant, name)).thenReturn(false);
        when(restaurantRepository.existsByIdAndOwnerId(secondRestaurant, ownerId)).thenReturn(true);

        var id = useCase.create(new CreateMenuCategoryCommand(secondRestaurant, ownerId, name));

        var captor = ArgumentCaptor.forClass(MenuCategory.class);
        verify(categoryRepository).save(captor.capture());
        assertEquals(id, captor.getValue().getId());
        assertEquals(secondRestaurant, captor.getValue().getRestaurantId().value());
        assertEquals(name, captor.getValue().getName());
        verify(categoryRepository).existsByRestaurantIdAndName(secondRestaurant, name);
        verify(categoryRepository, never()).existsByRestaurantIdAndName(firstRestaurant, name);
    }

    @Test
    void rejectsDuplicateNameInSameRestaurant() {
        UUID restaurantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String name = "Bún bò";
        when(restaurantRepository.existsByIdAndOwnerId(restaurantId, ownerId)).thenReturn(true);
        when(categoryRepository.existsByRestaurantIdAndName(restaurantId, name)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> useCase.create(new CreateMenuCategoryCommand(restaurantId, ownerId, name)));

        verify(categoryRepository, never()).save(any());
    }
}
