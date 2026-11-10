package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.application.port.in.restaurant.*;
import com.mealgo.restaurantservice.application.port.out.ApplicationQueryPort;
import com.mealgo.restaurantservice.infrastructure.persistence.mapper.ApplicationResultMapper;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.RestaurantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.RestaurantJpaEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationQueryAdapter implements ApplicationQueryPort {
    private final RestaurantJpaRepository restaurants;
    private final ApplicationResultMapper mapper;

    public ApplicationPageResult list(ListApplicationsQuery query) {
        Page<RestaurantJpaEntity> page = restaurants.findByApprovalStatus(query.status(),
                PageRequest.of(query.page(), query.size(), Sort.by("submittedAt", "id")));
        return new ApplicationPageResult(page.stream().map(mapper::toResult).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    public Optional<ApplicationResult> findById(UUID id) {
        return restaurants.findById(id).map(mapper::toResult);
    }

    public Optional<ApplicationResult> findByOwnerId(UUID ownerId) {
        return restaurants.findByOwnerId(ownerId).map(mapper::toResult);
    }
}
