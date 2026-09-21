package com.mealgo.restaurant_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    //Error Ward
    WARD_NOT_FOUND(HttpStatus.NOT_FOUND, "ward.not-found"),
    //Error Province
    PROVINCE_NOT_FOUND(HttpStatus.NOT_FOUND, "province.not-found"),
    ;
    private final HttpStatus status;
    private final String typeSuffix;

    ErrorCode(HttpStatus status, String typeSuffix) {
        this.status = status;
        this.typeSuffix = typeSuffix;
    }
}
