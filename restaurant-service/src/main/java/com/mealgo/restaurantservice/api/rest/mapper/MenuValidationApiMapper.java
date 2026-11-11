package com.mealgo.restaurantservice.api.rest.mapper;

import com.mealgo.restaurantservice.api.rest.dto.request.ValidateMenuRequest;
import com.mealgo.restaurantservice.api.rest.dto.response.ValidateMenuResponse;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuQuery;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface MenuValidationApiMapper {

    @Mapping(target = "restaurantId", source = "restaurantId")
    @Mapping(target = "items", source = "request.items")
    ValidateMenuQuery toQuery(
            UUID restaurantId,
            ValidateMenuRequest request
    );

    ValidateMenuQuery.Item toQueryItem(
            ValidateMenuRequest.Item item
    );

    ValidateMenuResponse toResponse(
            ValidateMenuResult result
    );

    ValidateMenuResponse.Item toResponseItem(
            ValidateMenuResult.Item item
    );

    ValidateMenuResponse.Option toResponseOption(
            ValidateMenuResult.Option option
    );

    ValidateMenuResponse.Issue toResponseIssue(
            ValidateMenuResult.Issue issue
    );
}