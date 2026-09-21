package com.mealgo.restaurant_service.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ward_code",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_restaurants_ward")
    )
    private Ward ward;
}
