package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.application.port.in.restaurant.ApplicationResult;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApplicationResultMapper {
    @Mapping(target = "restaurantId", source = "id")
    @Mapping(target = "addressLine", source = "address.addressLine")
    @Mapping(target = "wardCode", source = "address.ward.code")
    ApplicationResult toResult(RestaurantJpaEntity entity);
}
