package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.geography.GeographyResult;
import com.mealgo.restaurantservice.application.port.out.GeographyQueryPort;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.ProvinceJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.WardJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GeographyQueryAdapter implements GeographyQueryPort {
    private final ProvinceJpaRepository provinces;
    private final WardJpaRepository wards;

    @Override
    public List<GeographyResult> findProvinces() {
        return provinces.findAllByOrderByNameAscCodeAsc().stream()
                .map(province -> new GeographyResult(province.getCode(), province.getName()))
                .toList();
    }

    @Override
    public boolean provinceExists(Integer provinceCode) {
        return provinces.existsById(provinceCode);
    }

    @Override
    public List<GeographyResult> findWards(Integer provinceCode) {
        return wards.findByProvince_CodeOrderByNameAscCodeAsc(provinceCode).stream()
                .map(ward -> new GeographyResult(ward.getCode(), ward.getName()))
                .toList();
    }
}
