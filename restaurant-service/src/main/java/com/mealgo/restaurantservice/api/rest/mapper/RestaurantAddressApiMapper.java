package com.mealgo.restaurantservice.api.rest.mapper;

import com.mealgo.restaurantservice.api.rest.dto.request.UpdateRestaurantAddressRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.RestaurantAddressResponse;
import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantAddressResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RestaurantAddressApiMapper {
    UpdateRestaurantAddressCommand toCommand(
            UUID restaurantId,
            UUID ownerId,
            UpdateRestaurantAddressRequest request
    );

    RestaurantAddressResponse toResponse(RestaurantAddressResult result);
}
