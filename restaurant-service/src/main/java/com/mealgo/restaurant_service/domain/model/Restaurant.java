package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.RestaurantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends BaseEntity {

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    //address
    @Embedded
    private Address address;

    @OneToMany(mappedBy = "restaurant")
    private List<MenuCategory> categories;
}
