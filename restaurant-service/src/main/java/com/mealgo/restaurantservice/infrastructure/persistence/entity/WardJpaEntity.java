package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "wards",
        indexes = {
                @Index(name = "idx_wards_province_code", columnList = "province_code"),
                @Index(name = "idx_wards_codename", columnList = "codename")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class WardJpaEntity {

    @Id
    @Column(name = "code", nullable = false, updatable = false)
    private Integer code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "division_type", nullable = false, length = 50)
    private String divisionType;

    @Column(nullable = false, length = 100)
    private String codename;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "province_code",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_wards_province")
    )
    private ProvinceJpaEntity province;
}
