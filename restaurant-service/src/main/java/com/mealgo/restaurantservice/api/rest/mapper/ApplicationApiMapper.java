package com.mealgo.restaurantservice.api.rest.mapper;

import com.mealgo.restaurantservice.api.rest.dto.request.*;
import com.mealgo.restaurantservice.api.rest.dto.response.*;
import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import org.mapstruct.*;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApplicationApiMapper {
    ApplicationResponse toResponse(ApplicationResult result);
    ApplicationPageResponse toResponse(ApplicationPageResult result);
    ReviewApplicationCommand toCommand(UUID restaurantId, UUID adminId, ReviewApplicationRequest request);
    ResubmitApplicationCommand toCommand(UUID restaurantId, UUID ownerId, RegisterRestaurantRequest request);
    ListApplicationsQuery toQuery(ApprovalStatus status, int page, int size);
}
