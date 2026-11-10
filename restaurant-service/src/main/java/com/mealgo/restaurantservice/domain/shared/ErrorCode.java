package com.mealgo.restaurantservice.domain.shared;

public enum ErrorCode {
    RESTAURANT_ADDRESS_INVALID("restaurant.address-invalid"),
    RESTAURANT_ADDRESS_REQUIRES_CLOSED("restaurant.address-requires-closed"),
    RESTAURANT_NOT_APPROVED("restaurant.not-approved"),
    RESTAURANT_REVIEW_CONFLICT("restaurant.review-conflict"),
    RESTAURANT_REVIEW_INVALID("restaurant.review-invalid"),
    RESTAURANT_REJECTION_REASON_INVALID("restaurant.rejection-reason-invalid"),
    RESTAURANT_STATUS_INVALID("restaurant.status-invalid"),
    RESTAURANT_PAUSE_REASON_INVALID("restaurant.pause-reason-invalid"),
    RESTAURANT_NOT_FOUND("restaurant.not-found"),
    RESTAURANT_CONFLICT("restaurant.conflict"),
    WARD_NOT_FOUND("ward.not-found"),
    WARD_CONFLICT("ward.conflict"),
    WARD_PROVINCE_MISMATCH("ward.province-mismatch"),
    PROVINCE_NOT_FOUND("province.not-found"),
    PROVINCE_CONFLICT("province.conflict"),
    MENU_CATEGORY_NOT_FOUND("menu-category.not-found"),
    MENU_CATEGORY_CONFLICT("menu-category.conflict"),
    MENU_ITEM_NOT_FOUND("menu-item.not-found"),
    MENU_ITEM_CONFLICT("menu-item.conflict"),
    OPTION_GROUP_NOT_FOUND("option-group.not-found"),
    OPTION_GROUP_CONFLICT("option-group.conflict"),
    OPTION_GROUP_SELECTION_RANGE_INVALID("option-group.selection-range.invalid"),
    OPTION_GROUP_DUPLICATE("option-group.duplicate"),
    OPTION_GROUP_CONFIGURATION_INVALID("option-group.configuration-invalid"),
    OPTION_NOT_FOUND("option.not-found"),
    OPTION_CONFLICT("option.conflict"),
    UPLOAD_REQUEST_REQUIRED("upload.request-required"),
    UPLOAD_FILE_NAME_INVALID("upload.file-name-invalid"),
    UPLOAD_CONTENT_TYPE_INVALID("upload.content-type-invalid"),
    UPLOAD_FILE_SIZE_INVALID("upload.file-size-invalid");

    private final String typeSuffix;

    ErrorCode(String typeSuffix) {
        this.typeSuffix = typeSuffix;
    }

    public String getTypeSuffix() {
        return typeSuffix;
    }
}
