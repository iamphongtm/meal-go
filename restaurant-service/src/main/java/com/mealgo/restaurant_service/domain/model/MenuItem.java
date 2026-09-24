package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.MenuItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    MenuCategory category;

    String name;

    String description;

    BigDecimal basePrice;

    String imageUrl;

    @Enumerated(EnumType.STRING)
    MenuItemStatus status;
    Integer displayOrder;

    @OneToMany(mappedBy = "menuItem")
    List<OptionGroup> optionGroups;
}
