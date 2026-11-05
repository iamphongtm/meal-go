package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.ProvinceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProvinceJpaRepository extends JpaRepository<ProvinceJpaEntity, Integer> {
    List<ProvinceJpaEntity> findAllByOrderByNameAscCodeAsc();
}
