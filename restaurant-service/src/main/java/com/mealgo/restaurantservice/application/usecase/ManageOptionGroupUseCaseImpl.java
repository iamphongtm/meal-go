package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.optiongroup.ManageOptionGroupUseCase;
import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupCreationCommand;
import com.mealgo.restaurantservice.application.port.out.OptionGroupRepository;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroup;
import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroupId;
import com.mealgo.restaurantservice.domain.model.restaurant.RestaurantId;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.OPTION_GROUP_CONFLICT;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.RESTAURANT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ManageOptionGroupUseCaseImpl implements ManageOptionGroupUseCase {
    private final RestaurantRepository restaurantRepository;
    private final OptionGroupRepository optionGroupRepository;

    @Override
    @Transactional
    public OptionGroupId createOptionGroup(OptionGroupCreationCommand command) {
        if (!restaurantRepository.existsByIdAndOwnerId(command.restaurantId(), command.ownerId())) {
            throw new BusinessException(RESTAURANT_NOT_FOUND, command.restaurantId());
        }

        if (optionGroupRepository.existsByRestaurantIdAndName(command.restaurantId(), command.name())) {
            throw new BusinessException(OPTION_GROUP_CONFLICT, command.name());
        }

        var optionNames = command.options().stream()
                .map(OptionGroupCreationCommand.OptionCreationCommand::name)
                .toList();
        var optionGroup = OptionGroup.create(
                RestaurantId.of(command.restaurantId()), command.name(), optionNames);

        optionGroupRepository.save(optionGroup);
        return optionGroup.getId();
    }
}
