package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.event.RestaurantCreatedEvent;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantCommand;
import com.mealgo.restaurantservice.application.port.in.restaurant.RegisterRestaurantUseCase;
import com.mealgo.restaurantservice.application.port.out.OutboxEventStore;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.application.port.out.WardRepository;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.model.geography.Ward;
import com.mealgo.restaurantservice.domain.model.restaurant.Address;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.mealgo.restaurantservice.application.event.EventType.RESTAURANT_CREATED;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RESTAURANT-SERVICE")
public class RegisterRestaurantUseCaseImpl implements RegisterRestaurantUseCase {
    private final RestaurantRepository restaurantRepository;
    private final WardRepository wardRepository;
    private final OutboxEventStore outboxEventStore;

    @Override
    @Transactional
    public RestaurantId registerRestaurant(RegisterRestaurantCommand command) {
        if(restaurantRepository.existByOwnerId(command.ownerId())){
            throw new BusinessException(RESTAURANT_CONFLICT, command.ownerId());
        }

        Ward ward = wardRepository.findByCode(command.wardCode()).orElseThrow(() -> new  BusinessException(WARD_NOT_FOUND, command.wardCode()));

        ward.validateBelongsTo(command.provinceCode());

        Restaurant restaurant = Restaurant.register(
                command.ownerId(),
                command.name(),
                command.phone(),
                RestaurantStatus.CLOSED,
                new Address(command.addressLine(), command.wardCode()),
                command.latitude(),
                command.longitude()
        );

        restaurantRepository.save(restaurant);

        outboxEventStore.append(RestaurantCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(RESTAURANT_CREATED.value())
                .occurredAt(Instant.now())
                .restaurantId(restaurant.getId().value())
                .name(restaurant.getName())
                .status(restaurant.getStatus().name())
                .addressLine(restaurant.getAddress().addressLine())
                .wardCode(restaurant.getAddress().wardCode())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .build());

        return restaurant.getId();
    }
}
