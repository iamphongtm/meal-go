package com.mealgo.restaurantservice.infrastructure.persistence.mapper;

import com.mealgo.restaurantservice.domain.model.restaurant.*;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RestaurantPersistenceMapper {
    default Restaurant toDomain(RestaurantJpaEntity entity) {
        return new Restaurant(RestaurantId.of(entity.getId()), entity.getOwnerId(), entity.getName(),
                entity.getPhone(), entity.getStatus(),
                new Address(entity.getAddress().getAddressLine(), entity.getAddress().getWard().getCode()),
                entity.getLatitude(), entity.getLongitude(), entity.getApprovalStatus(), entity.getSubmittedAt(),
                entity.getReviewedBy(), entity.getReviewedAt(), entity.getRejectionReason(), entity.getPauseReason());
    }

    @Mapping(target = "id", expression = "java(restaurant.getId().value())")
    @Mapping(target = "address", ignore = true)
    RestaurantJpaEntity toEntity(Restaurant restaurant, @Context WardJpaEntity ward);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "address", ignore = true)
    void updateState(Restaurant restaurant, @MappingTarget RestaurantJpaEntity entity, @Context WardJpaEntity ward);

    @AfterMapping
    default void address(Restaurant restaurant, @MappingTarget RestaurantJpaEntity entity,
                         @Context WardJpaEntity ward) {
        AddressEmbeddable address = new AddressEmbeddable();
        address.setAddressLine(restaurant.getAddress().addressLine());
        address.setWard(ward);
        entity.setAddress(address);
    }
}
