package com.mealgo.restaurantservice.application.port.in.optiongroup;

import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroupId;

public interface ManageOptionGroupUseCase {
    OptionGroupId createOptionGroup(OptionGroupCreationCommand command);
}
