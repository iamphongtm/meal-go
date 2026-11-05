package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.domain.model.geography.Ward;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.WardJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface WardJpaRepository extends JpaRepository<WardJpaEntity, Integer> {
    List<WardJpaEntity> findByProvince_CodeOrderByNameAscCodeAsc(Integer provinceCode);

    Optional<Ward> findByCode(Integer code);
}
