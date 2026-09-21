package com.mealgo.restaurant_service.domain.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "provinces",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_provinces_codename",
                        columnNames = "codename"
                )
        }
)
public class Province {

    @Id
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