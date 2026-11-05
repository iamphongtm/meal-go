package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.geography.GeographyResult;
import com.mealgo.restaurantservice.application.port.in.geography.ListGeographyUseCase;
import com.mealgo.restaurantservice.application.port.out.GeographyQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListGeographyUseCaseImpl implements ListGeographyUseCase {
    private final GeographyQueryPort geography;

    @Override
    public List<GeographyResult> listProvinces() {
        return geography.findProvinces();
    }

    @Override
    public List<GeographyResult> listWards(Integer provinceCode) {
        if (provinceCode == null || provinceCode <= 0) {
            throw new IllegalArgumentException("Province code must be positive");
        }
        if (!geography.provinceExists(provinceCode)) {
            throw new NoSuchElementException("Province not found");
        }
        return geography.findWards(provinceCode);
    }
}
