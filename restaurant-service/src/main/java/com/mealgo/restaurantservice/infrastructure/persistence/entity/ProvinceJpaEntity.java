package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "provinces",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_provinces_codename",
                columnNames = "codename"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProvinceJpaEntity {

    @Id
    @Column(name = "code", nullable = false, updatable = false)
    private Integer code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "division_type", nullable = false, length = 50)
    private String divisionType;

    @Column(nullable = false, length = 100)
    private String codename;

    @Column(name = "phone_code", nullable = false)
    private Integer phoneCode;
}
