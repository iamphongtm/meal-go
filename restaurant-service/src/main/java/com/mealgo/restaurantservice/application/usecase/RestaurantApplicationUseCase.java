package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.event.RestaurantApprovedEvent;
import com.mealgo.restaurantservice.application.event.RestaurantCreatedEvent;
import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import com.mealgo.restaurantservice.application.port.out.*;
import com.mealgo.restaurantservice.domain.enums.ApprovalStatus;
import com.mealgo.restaurantservice.domain.model.restaurant.Address;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.model.geography.Ward;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantApplicationUseCase {
    private final RestaurantRepository restaurants;
    private final ApplicationQueryPort applications;
    private final WardRepository wards;
    private final OutboxEventStore outbox;

    public ApplicationPageResult list(ListApplicationsQuery query) {
        return applications.list(query);
    }

    public ApplicationResult get(UUID restaurantId) {
        return applications.findById(restaurantId)
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, restaurantId));
    }

    public ApplicationResult getMine(UUID ownerId) {
        return applications.findByOwnerId(ownerId)
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND));
    }

    @Transactional
    public ApplicationResult review(ReviewApplicationCommand command) {
        Restaurant restaurant = restaurants.findByIdForUpdate(command.restaurantId())
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId()));
        Instant now = Instant.now();
        restaurant.review(command.decision(), command.adminId(), command.reason(), now);
        restaurants.updateState(restaurant);
        if (command.decision() == ApprovalStatus.APPROVED) {
            outbox.append(new RestaurantApprovedEvent(UUID.randomUUID(), "RestaurantApproved", now,
                    command.restaurantId(), restaurant.getOwnerId(), command.adminId()));
            outbox.append(RestaurantCreatedEvent.builder()
                    .eventId(UUID.randomUUID()).eventType("RestaurantCreated").occurredAt(now)
                    .restaurantId(command.restaurantId()).name(restaurant.getName())
                    .status(restaurant.getStatus().name()).addressLine(restaurant.getAddress().addressLine())
                    .wardCode(restaurant.getAddress().wardCode()).latitude(restaurant.getLatitude())
                    .longitude(restaurant.getLongitude()).build());
        }
        return get(command.restaurantId());
    }

    @Transactional
    public ApplicationResult resubmit(ResubmitApplicationCommand command) {
        Restaurant restaurant = restaurants.findByIdForUpdate(command.restaurantId())
                .filter(value -> value.getOwnerId().equals(command.ownerId()))
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId()));
        Ward ward = wards.findByCode(command.wardCode())
                .orElseThrow(() -> new BusinessException(WARD_NOT_FOUND, command.wardCode()));
        ward.validateBelongsTo(command.provinceCode());
        restaurant.resubmit(command.name(), command.phone(), new Address(command.addressLine(), command.wardCode()),
                command.latitude(), command.longitude(), Instant.now());
        restaurants.updateState(restaurant);
        return get(command.restaurantId());
    }
}
