package com.mealgo.restaurantservice.api.rest.mapper;

import com.mealgo.restaurantservice.api.rest.dto.request.UpdateAvatarRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.RestaurantAvatarResponse;
import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantAvatarResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateAvatarCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RestaurantAvatarApiMapper {
    UpdateAvatarCommand toCommand(UUID restaurantId, UUID ownerId, UpdateAvatarRequest request);

    RestaurantAvatarResponse toResponse(RestaurantAvatarResult result);
}
