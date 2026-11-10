package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationQueryPort {
    ApplicationPageResult list(ListApplicationsQuery query);
    Optional<ApplicationResult> findById(UUID id);
    Optional<ApplicationResult> findByOwnerId(UUID ownerId);
}
