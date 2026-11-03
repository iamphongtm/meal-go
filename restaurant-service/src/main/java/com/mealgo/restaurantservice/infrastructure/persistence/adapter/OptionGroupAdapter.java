package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.out.OptionGroupRepository;
import com.mealgo.restaurantservice.domain.model.optiongroup.OptionGroup;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.OptionGroupPersistenceMapper;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.OptionGroupJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OptionGroupAdapter implements OptionGroupRepository {
    private final OptionGroupJpaRepository optionGroupJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;

    @Override
    public Optional<OptionGroup> findByIdAndRestaurantId(UUID id, UUID restaurantId) {
        return optionGroupJpaRepository.findByIdAndRestaurantId(id, restaurantId)
                .map(OptionGroupPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByRestaurantIdAndName(UUID restaurantId, String name) {
        return optionGroupJpaRepository.existsByRestaurantIdAndName(restaurantId, name);
    }

    @Override
    public void save(OptionGroup optionGroup) {
        var restaurantReference = restaurantJpaRepository.getReferenceById(optionGroup.getRestaurantId().value());
        var entity = OptionGroupPersistenceMapper.toNewEntity(optionGroup, restaurantReference);
        optionGroupJpaRepository.save(entity);
    }
}
