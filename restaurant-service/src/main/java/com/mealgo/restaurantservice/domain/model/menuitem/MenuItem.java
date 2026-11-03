package com.mealgo.restaurantservice.domain.model.menuitem;

import com.mealgo.restaurantservice.domain.enums.MenuItemStatus;
import com.mealgo.restaurantservice.domain.model.category.MenuCategoryId;
import com.mealgo.restaurantservice.domain.shared.Entity;
import com.mealgo.restaurantservice.domain.shared.IAggregateRoot;

import java.math.BigDecimal;
import java.util.List;

public final class MenuItem extends Entity<MenuItemId> implements IAggregateRoot {
    private final MenuCategoryId categoryId;
    private final String name;
    private final String description;
    private final BigDecimal basePrice;
    private final String imageUrl;
    private final MenuItemStatus status;
    private final Integer displayOrder;
    private final List<MenuItemOptionGroup> optionGroups;

    public MenuItem(MenuItemId id, MenuCategoryId categoryId, String name, String description,
                    BigDecimal basePrice, String imageUrl, MenuItemStatus status,
                    Integer displayOrder, List<MenuItemOptionGroup> optionGroups) {
        super(id);
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.imageUrl = imageUrl;
        this.status = status;
        this.displayOrder = displayOrder;
        this.optionGroups = List.copyOf(optionGroups);
    }

    public static MenuItem create(MenuCategoryId categoryId, String name, String description,
                                  BigDecimal basePrice, String imageUrl, int displayOrder,
                                  List<MenuItemOptionGroup> optionGroups) {
        return new MenuItem(MenuItemId.newId(), categoryId, name, description, basePrice, imageUrl,
                MenuItemStatus.ACTIVE, displayOrder, optionGroups);
    }

    public MenuCategoryId getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getImageUrl() { return imageUrl; }
    public MenuItemStatus getStatus() { return status; }
    public Integer getDisplayOrder() { return displayOrder; }
    public List<MenuItemOptionGroup> getOptionGroups() { return optionGroups; }
}
