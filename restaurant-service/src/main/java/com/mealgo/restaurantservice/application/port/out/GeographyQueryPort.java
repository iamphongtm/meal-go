package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.geography.GeographyResult;

import java.util.List;

public interface GeographyQueryPort {
    List<GeographyResult> findProvinces();

    boolean provinceExists(Integer provinceCode);

    List<GeographyResult> findWards(Integer provinceCode);
}
