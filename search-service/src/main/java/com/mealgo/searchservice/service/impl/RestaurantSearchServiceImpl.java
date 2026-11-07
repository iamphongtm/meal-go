package com.mealgo.searchservice.service.impl;

import com.mealgo.searchservice.dto.RestaurantSearchPage;
import com.mealgo.searchservice.repository.RestaurantSearchRepository;
import com.mealgo.searchservice.service.RestaurantSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantSearchServiceImpl implements RestaurantSearchService {
    private final RestaurantSearchRepository searchRepository;

    @Override
    public RestaurantSearchPage findNearby(double latitude, double longitude, double radiusKm, int page, int size) {
        validatePagination(page, size);
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
        if (!Double.isFinite(radiusKm) || radiusKm <= 0) {
            throw new IllegalArgumentException("radiusKm must be a finite positive number");
        }
        return searchRepository.findNearby(latitude, longitude, radiusKm, page, size);
    }

    @Override
    public RestaurantSearchPage findByDish(String keyword, int page, int size) {
        validatePagination(page, size);
        if (keyword == null || keyword.isBlank() || keyword.length() > 200) {
            throw new IllegalArgumentException("keyword must contain between 1 and 200 characters");
        }
        return searchRepository.findByDish(keyword.strip(), page, size);
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        }
        if ((long) page * size + size > 10_000) {
            throw new IllegalArgumentException("Requested page exceeds the 10000-result search window");
        }
    }
}
