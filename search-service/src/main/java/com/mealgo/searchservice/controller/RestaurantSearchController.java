package com.mealgo.searchservice.controller;

import com.mealgo.searchservice.service.RestaurantSearchService;
import com.mealgo.searchservice.dto.RestaurantSearchPage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search/restaurants")
@RequiredArgsConstructor
public class RestaurantSearchController {
    private final RestaurantSearchService searchRestaurants;

    @Operation(summary = "Find nearby restaurants", description = "Straight-line surface distance in kilometers, nearest first.")
    @GetMapping("/nearby")
    public RestaurantSearchPage nearby(
            @RequestParam(name = "latitude") double latitude,
            @RequestParam(name = "longitude") double longitude,
            @RequestParam(name = "radiusKm", defaultValue = "5") double radiusKm,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return searchRestaurants.findNearby(latitude, longitude, radiusKm, page, size);
    }

    @Operation(summary = "Find restaurants by dish name", description = "Each restaurant includes up to five best-matching menu items.")
    @GetMapping("/by-dish")
    public RestaurantSearchPage byDish(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return searchRestaurants.findByDish(keyword, page, size);
    }

}
