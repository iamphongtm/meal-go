package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupItems;
import com.mealgo.restaurantservice.application.port.out.OptionGroupItemsRepository;
import com.mealgo.restaurantservice.application.port.out.RestaurantRepository;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import static com.mealgo.restaurantservice.domain.shared.ErrorCode.RESTAURANT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ManageOptionGroupItemsUseCase {
    private final RestaurantRepository restaurants;
    private final OptionGroupItemsRepository groups;

    @Transactional(readOnly = true)
    public List<OptionGroupItems> list(UUID restaurantId, UUID ownerId) {
        checkOwner(restaurantId, ownerId);
        return groups.findAll(restaurantId);
    }

    @Transactional
    public void replace(UUID restaurantId, UUID ownerId, UUID groupId,
                        List<OptionGroupItems.ItemConfiguration> items) {
        checkOwner(restaurantId, ownerId);
        groups.replaceItems(restaurantId, groupId, items);
    }

    private void checkOwner(UUID restaurantId, UUID ownerId) {
        if (!restaurants.existsByIdAndOwnerId(restaurantId, ownerId)) {
            throw new BusinessException(RESTAURANT_NOT_FOUND, restaurantId);
        }
    }
}
