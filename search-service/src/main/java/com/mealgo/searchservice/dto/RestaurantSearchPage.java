package com.mealgo.searchservice.dto;

import java.util.List;

public record RestaurantSearchPage(
        List<RestaurantSearchResult> content,
        int page,
        int size,
        long totalElements,
        long totalPages
) {
    public static RestaurantSearchPage of(List<RestaurantSearchResult> content, int page, int size, long total) {
        return new RestaurantSearchPage(content, page, size, total, (total + size - 1) / size);
    }
}
