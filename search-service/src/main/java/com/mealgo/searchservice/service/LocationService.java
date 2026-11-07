package com.mealgo.searchservice.service;

import com.mealgo.searchservice.dto.ResolvedAddress;

public interface LocationService {
    ResolvedAddress resolve(double latitude, double longitude);
}
