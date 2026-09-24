package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.CategoryStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "menu_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MenuCategory extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    Integer displayOrder;

    @Enumerated(EnumType.STRING)
    CategoryStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "restaurant_id",
            nullable = false
    )
    private Restaurant restaurant;

    @OneToMany(mappedBy = "category")
    List<MenuItem> items;

}
