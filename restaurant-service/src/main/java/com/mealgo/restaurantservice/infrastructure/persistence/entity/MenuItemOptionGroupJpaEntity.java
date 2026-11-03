package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "menu_item_option_groups", uniqueConstraints =
        @UniqueConstraint(name = "uk_menu_item_option_group", columnNames = {"menu_item_id", "option_group_id"}))
@Getter
@Setter
@NoArgsConstructor
public class MenuItemOptionGroupJpaEntity extends BaseJpaEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItemJpaEntity menuItem;

    @Column(name = "option_group_id", nullable = false, updatable = false)
    private UUID optionGroupId;

    @Column(name = "min_select", nullable = false)
    private Integer minSelect;

    @Column(name = "max_select", nullable = false)
    private Integer maxSelect;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @OneToMany(mappedBy = "menuItemOptionGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 50)
    private List<MenuItemOptionPriceJpaEntity> optionPrices = new ArrayList<>();

    public List<MenuItemOptionPriceJpaEntity> getOptionPrices() {
        return Collections.unmodifiableList(optionPrices);
    }
}
