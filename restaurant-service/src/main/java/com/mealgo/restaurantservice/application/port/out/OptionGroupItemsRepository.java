package com.mealgo.restaurantservice.application.port.out;

import com.mealgo.restaurantservice.application.port.in.optiongroup.OptionGroupItems;
import java.util.List;
import java.util.UUID;

public interface OptionGroupItemsRepository {
    List<OptionGroupItems> findAll(UUID restaurantId);
    void replaceItems(UUID restaurantId, UUID groupId, List<OptionGroupItems.ItemConfiguration> items);
}
