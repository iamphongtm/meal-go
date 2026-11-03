package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "menu_item_option_prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_menu_item_group_option_price",
                columnNames = {"menu_item_option_group_id", "option_id"}
        )
)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class MenuItemOptionPriceJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_option_group_id", nullable = false)
    private MenuItemOptionGroupJpaEntity menuItemOptionGroup;

    @Column(name = "option_id", nullable = false, updatable = false)
    private UUID optionId;

    @Column(name = "additional_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal additionalPrice;
}
