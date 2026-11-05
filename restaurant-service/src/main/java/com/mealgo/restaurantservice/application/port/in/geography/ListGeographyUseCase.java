package com.mealgo.restaurantservice.application.port.in.geography;

import java.util.List;

public interface ListGeographyUseCase {
    List<GeographyResult> listProvinces();

    List<GeographyResult> listWards(Integer provinceCode);
}
