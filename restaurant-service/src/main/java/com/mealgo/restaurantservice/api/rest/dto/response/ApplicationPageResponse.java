package com.mealgo.restaurantservice.api.rest.dto.response;

import java.util.List;

public record ApplicationPageResponse(List<ApplicationResponse> items, int page, int size, long totalElements) {
}
