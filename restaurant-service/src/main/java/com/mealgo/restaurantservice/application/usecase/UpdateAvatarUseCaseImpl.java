package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.restaurant.RestaurantAvatarResult;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateAvatarCommand;
import com.mealgo.restaurantservice.application.port.in.restaurant.UpdateAvatarUseCase;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.model.restaurant.Restaurant;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UpdateAvatarUseCaseImpl implements UpdateAvatarUseCase {
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public RestaurantAvatarResult updateAvatar(UpdateAvatarCommand command) {
        if (command == null || command.restaurantId() == null || command.ownerId() == null) {
            throw new BusinessException(RESTAURANT_AVATAR_INVALID);
        }
        Restaurant restaurant = restaurantRepository.findByIdForUpdate(command.restaurantId())
                .filter(value -> value.getOwnerId().equals(command.ownerId()))
                .orElseThrow(() -> new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId()));
        if (restaurant.updateAvatar(command.avatarUrl())) {
            restaurantRepository.updateState(restaurant);
        }
        return new RestaurantAvatarResult(command.restaurantId(), restaurant.getAvatarUrl());
    }
}
