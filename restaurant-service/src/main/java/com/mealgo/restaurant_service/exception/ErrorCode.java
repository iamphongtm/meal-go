package com.mealgo.restaurant_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "restaurant.not-found"),
    RESTAURANT_CONFLICT(HttpStatus.CONFLICT, "restaurant.conflict"),

    WARD_NOT_FOUND(HttpStatus.NOT_FOUND, "ward.not-found"),
    WARD_CONFLICT(HttpStatus.CONFLICT, "ward.conflict"),

    PROVINCE_NOT_FOUND(HttpStatus.NOT_FOUND, "province.not-found"),
    PROVINCE_CONFLICT(HttpStatus.CONFLICT, "province.conflict"),

    MENU_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "menu-category.not-found"),
    MENU_CATEGORY_CONFLICT(HttpStatus.CONFLICT, "menu-category.conflict"),

    MENU_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "menu-item.not-found"),
    MENU_ITEM_CONFLICT(HttpStatus.CONFLICT, "menu-item.conflict"),

    OPTION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "option-group.not-found"),
    OPTION_GROUP_CONFLICT(HttpStatus.CONFLICT, "option-group.conflict"),
    OPTION_GROUP_SELECTION_RANGE_INVALID(HttpStatus.BAD_REQUEST, "option-group.selection-range.invalid"),

    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "option.not-found"),
    OPTION_CONFLICT(HttpStatus.CONFLICT, "option.conflict");

    private final HttpStatus status;
    private final String typeSuffix;

    ErrorCode(HttpStatus status, String typeSuffix) {
        this.status = status;
        this.typeSuffix = typeSuffix;
    }
}
