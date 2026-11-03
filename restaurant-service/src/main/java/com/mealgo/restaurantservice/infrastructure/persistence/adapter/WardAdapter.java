package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.out.WardRepository;
import com.mealgo.restaurantservice.domain.model.geography.Ward;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.WardJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WardAdapter implements WardRepository {
    private final WardJpaRepository wardJpaRepository;

    @Override
    public Optional<Ward> findByCode(Integer code) {
        return wardJpaRepository.findByCode(code);
    }
}
