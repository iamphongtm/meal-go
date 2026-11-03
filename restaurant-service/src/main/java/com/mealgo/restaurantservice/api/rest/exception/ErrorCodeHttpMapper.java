package com.mealgo.restaurantservice.api.rest.exception;

import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import org.springframework.http.HttpStatus;

final class ErrorCodeHttpMapper {
    private ErrorCodeHttpMapper() {
    }

    static HttpStatus statusOf(ErrorCode code) {
        return switch (code) {
            case RESTAURANT_NOT_FOUND, WARD_NOT_FOUND, PROVINCE_NOT_FOUND,
                 MENU_CATEGORY_NOT_FOUND, MENU_ITEM_NOT_FOUND, OPTION_GROUP_NOT_FOUND,
                 OPTION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RESTAURANT_CONFLICT, WARD_CONFLICT, PROVINCE_CONFLICT,
                 MENU_CATEGORY_CONFLICT, MENU_ITEM_CONFLICT, OPTION_GROUP_CONFLICT,
                 OPTION_CONFLICT -> HttpStatus.CONFLICT;
            case WARD_PROVINCE_MISMATCH, OPTION_GROUP_SELECTION_RANGE_INVALID, OPTION_GROUP_DUPLICATE,
                 OPTION_GROUP_CONFIGURATION_INVALID, UPLOAD_REQUEST_REQUIRED,
                 UPLOAD_FILE_NAME_INVALID, UPLOAD_CONTENT_TYPE_INVALID,
                 UPLOAD_FILE_SIZE_INVALID -> HttpStatus.BAD_REQUEST;
        };
    }
}
