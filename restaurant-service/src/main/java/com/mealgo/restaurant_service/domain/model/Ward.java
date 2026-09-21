package com.mealgo.restaurant_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "wards",
        indexes = {
                @Index(name = "idx_wards_province_code", columnList = "province_code"),
                @Index(name = "idx_wards_codename", columnList = "codename")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wards_code_province", columnNames = {"code", "province_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Ward {

    @Id
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
    private Province province;
}
