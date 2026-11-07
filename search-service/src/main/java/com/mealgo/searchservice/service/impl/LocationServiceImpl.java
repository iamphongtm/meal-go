package com.mealgo.searchservice.service.impl;

import com.mealgo.searchservice.client.VietMapClient;
import com.mealgo.searchservice.dto.ResolvedAddress;
import com.mealgo.searchservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final VietMapClient vietMapClient;

    @Override
    public ResolvedAddress resolve(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid coordinates");
        }
        return vietMapClient.reverse(latitude, longitude);
    }
}
