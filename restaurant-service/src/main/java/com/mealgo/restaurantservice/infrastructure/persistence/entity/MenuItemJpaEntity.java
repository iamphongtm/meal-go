package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import com.mealgo.restaurantservice.domain.enums.MenuItemStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "menu_items")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class MenuItemJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategoryJpaEntity category;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MenuItemStatus status;

    private Integer displayOrder;

    @OneToMany(mappedBy = "menuItem", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("displayOrder ASC, id ASC")
    private List<MenuItemOptionGroupJpaEntity> optionGroups = new ArrayList<>();

    public List<MenuItemOptionGroupJpaEntity> getOptionGroups() {
        return Collections.unmodifiableList(optionGroups);
    }
}
