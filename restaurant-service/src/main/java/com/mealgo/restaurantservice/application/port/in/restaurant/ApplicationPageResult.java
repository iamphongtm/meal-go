package com.mealgo.restaurantservice.application.port.in.restaurant;

import java.util.List;

public record ApplicationPageResult(List<ApplicationResult> items, int page, int size, long totalElements) {
}
