package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Integer> {
    Optional<Ward> findByCode(Integer code);
}
