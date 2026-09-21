package com.mealgo.restaurant_service.service.impl;

import com.mealgo.restaurant_service.domain.enums.RestaurantStatus;
import com.mealgo.restaurant_service.domain.model.Address;
import com.mealgo.restaurant_service.domain.model.Province;
import com.mealgo.restaurant_service.domain.model.Restaurant;
import com.mealgo.restaurant_service.domain.model.Ward;
import com.mealgo.restaurant_service.domain.repository.ProvinceRepository;
import com.mealgo.restaurant_service.domain.repository.RestaurantRepository;
import com.mealgo.restaurant_service.domain.repository.WardRepository;
import com.mealgo.restaurant_service.dto.request.RestaurantCreationRequest;
import com.mealgo.restaurant_service.exception.BusinessException;
import com.mealgo.restaurant_service.exception.ErrorCode;
import com.mealgo.restaurant_service.service.RestaurantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "RESTAURANT-SERVICE")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RestaurantServiceImpl implements RestaurantService {
    RestaurantRepository restaurantRepo;
    WardRepository wardRepo;
    ProvinceRepository provinceRepo;

    @Override
    public void init(RestaurantCreationRequest request, String ownerId) {
        //check ward
        Ward ward = wardRepo.findByCode(request.wardCode()).orElse(null);

        if (ward == null) {
            throw new BusinessException(ErrorCode.WARD_NOT_FOUND, request.wardCode().toString());
        }

        //check province
        Province province = provinceRepo.findByCode(request.wardCode()).orElse(null);

        if (province == null) {
            throw new BusinessException(ErrorCode.WARD_NOT_FOUND, request.wardCode().toString());
        }

        Restaurant restaurant = Restaurant.builder()
                .ownerId(UUID.fromString(request.ownerId())) //TODO change ownerId
                .name(request.name())
                .phone(request.phone())
                .status(RestaurantStatus.OPENING)
                .address(Address.builder()
                        .addressLine(request.addressLine())
                        .ward(ward)
                        .build())
                .build();

        restaurantRepo.save(restaurant);
    }
}
