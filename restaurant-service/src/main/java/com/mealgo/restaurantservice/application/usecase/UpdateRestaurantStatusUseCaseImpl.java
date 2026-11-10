package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.enums.RestaurantStatus;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateRestaurantStatusUseCaseImpl implements UpdateRestaurantStatusUseCase {
    private static final Set<String> REASONS = Set.of("overload", "out_of_stock", "shift_break", "other");
    private final RestaurantRepository restaurantRepository;

    @Override
    public RestaurantStatusResult updateStatus(UpdateRestaurantStatusCommand command) {
        if (command == null || command.status() == null) {
            throw new BusinessException(RESTAURANT_STATUS_INVALID);
        }
        String reason = null;
        if (command.status() == RestaurantStatus.CLOSED) {
            reason = command.pauseReason();
        }
        if (command.status() == RestaurantStatus.CLOSED && (reason == null || !REASONS.contains(reason))) {
            throw new BusinessException(RESTAURANT_PAUSE_REASON_INVALID);
        }
        Restaurant restaurant = restaurantRepository.findByIdForUpdate(command.restaurantId())
                .filter(value -> value.getOwnerId().equals(command.ownerId()))
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId()));
        restaurant.changeOperatingStatus(command.status(), reason);
        restaurantRepository.updateState(restaurant);
        return new RestaurantStatusResult(command.restaurantId(), command.status(), reason);
    }
}
