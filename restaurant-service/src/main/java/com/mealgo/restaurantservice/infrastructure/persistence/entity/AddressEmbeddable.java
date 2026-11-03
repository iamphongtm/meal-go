package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AddressEmbeddable {

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ward_code",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_restaurants_ward")
    )
    private WardJpaEntity ward;
}
