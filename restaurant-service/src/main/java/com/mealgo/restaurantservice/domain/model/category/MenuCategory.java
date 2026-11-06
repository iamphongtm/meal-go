package com.mealgo.restaurantservice.domain.model.category;

import com.mealgo.restaurantservice.domain.enums.CategoryStatus;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.domain.shared.Entity;
import com.mealgo.restaurantservice.domain.shared.IAggregateRoot;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class MenuCategory extends Entity<MenuCategoryId> implements IAggregateRoot {
    private final RestaurantId restaurantId;
    private  String name;
    private final Integer displayOrder;
    private final CategoryStatus status;

    public MenuCategory(MenuCategoryId id, RestaurantId restaurantId, String name,
                        Integer displayOrder) {
        super(id);
        this.restaurantId = restaurantId;
        this.name = name;
        this.displayOrder = displayOrder;
        this.status = CategoryStatus.ACTIVE;
    }

    public static MenuCategory create(RestaurantId restaurantId, String name, Integer displayOrder) {
        return new MenuCategory(MenuCategoryId.newId(), restaurantId, name, displayOrder);
    }

    public void rename(String name) {
        if(name == null || name.isBlank()) throw  new IllegalArgumentException("Name must be not blank!");

        this.name = name;
    }
}
