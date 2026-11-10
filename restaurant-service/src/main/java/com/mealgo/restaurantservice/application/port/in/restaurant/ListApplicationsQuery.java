package com.mealgo.restaurantservice.application.port.in.restaurant;

import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;

public record ListApplicationsQuery(ApprovalStatus status, int page, int size) {
}
