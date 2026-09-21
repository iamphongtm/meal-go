package com.mealgo.restaurant_service.domain.repository;

import com.mealgo.restaurant_service.domain.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvinceRepository extends JpaRepository<Province, Integer> {
    Optional<Province> findByCode(Integer code);
}
