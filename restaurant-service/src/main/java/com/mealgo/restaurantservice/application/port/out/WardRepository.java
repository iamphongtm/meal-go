package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.domain.model.geography.Ward;

import java.util.Optional;

public interface WardRepository {
    Optional<Ward> findByCode(Integer integer);
}
