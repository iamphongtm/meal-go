package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import com.mealgo.restaurantservice.domain.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "menu_categories")
@Getter
@Setter
@NoArgsConstructor
public class MenuCategoryJpaEntity extends BaseJpaEntity {

    @Column(nullable = false, length = 100)
    private String name;

    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoryStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "restaurant_id",
            nullable = false
    )
    private RestaurantJpaEntity restaurant;

    @OneToMany(mappedBy = "category")
    @OrderBy("displayOrder ASC, id ASC")
    private List<MenuItemJpaEntity> items = new ArrayList<>();

    public List<MenuItemJpaEntity> getItems() {
        return Collections.unmodifiableList(items);
    }
}
