package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.event.RestaurantAddressUpdatedEvent;
import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantAddressResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressCommand;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateRestaurantAddressUseCase;
import com.mealgo.restaurantservice.application.port.out.OutboxEventStore;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.application.port.out.WardRepository;
import com.mealgo.restaurantservice.domain.model.geography.Ward;
import com.mealgo.restaurantservice.domain.model.restaurant.Address;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UpdateRestaurantAddressUseCaseImpl implements UpdateRestaurantAddressUseCase {
    private final RestaurantRepository restaurantRepository;
    private final WardRepository wardRepository;
    private final OutboxEventStore outboxEventStore;

    @Override
    @Transactional
    public RestaurantAddressResult updateAddress(UpdateRestaurantAddressCommand command) {
        validateCommand(command);

        Restaurant restaurant = restaurantRepository.findByIdForUpdate(command.restaurantId())
                .filter(value -> value.getOwnerId().equals(command.ownerId()))
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId()));

        Ward ward = wardRepository.findByCode(command.wardCode())
                .orElseThrow(() -> new BusinessException(WARD_NOT_FOUND, command.wardCode()));

        ward.validateBelongsTo(command.provinceCode());

        Address address = new Address(command.addressLine().trim(), command.wardCode());
        boolean addressChanged = restaurant.updateAddress(address, command.latitude(), command.longitude());

        if (addressChanged) {
            restaurantRepository.updateState(restaurant);

            RestaurantAddressUpdatedEvent event = new RestaurantAddressUpdatedEvent(
                    UUID.randomUUID(),
                    "RestaurantAddressUpdated",
                    Instant.now(),
                    command.restaurantId(),
                    restaurant.getName(),
                    restaurant.getStatus().name(),
                    restaurant.getAddress().addressLine(),
                    restaurant.getAddress().wardCode(),
                    restaurant.getLatitude(),
                    restaurant.getLongitude()
            );

            outboxEventStore.append(event);
        }

        return new RestaurantAddressResult(
                command.restaurantId(),
                command.provinceCode(),
                restaurant.getAddress().wardCode(),
                restaurant.getAddress().addressLine(),
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );
    }

    private void validateCommand(UpdateRestaurantAddressCommand command) {
        if (command == null || command.restaurantId() == null || command.ownerId() == null
                || command.provinceCode() == null || command.provinceCode() <= 0
                || command.wardCode() == null || command.wardCode() <= 0
                || command.addressLine() == null || command.addressLine().isBlank()
                || command.addressLine().length() > 255
                || command.latitude() == null || !Double.isFinite(command.latitude())
                || command.latitude() < -90 || command.latitude() > 90
                || command.longitude() == null || !Double.isFinite(command.longitude())
                || command.longitude() < -180 || command.longitude() > 180) {
            throw new BusinessException(RESTAURANT_ADDRESS_INVALID);
        }
    }
}
