package com.mealgo.identify_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user.not-found"),
    USER_CONFLICT(HttpStatus.CONFLICT, "user.conflict"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "auth.invalid-credentials"),
    ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "auth.account-inactive"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "auth.invalid-refresh-token"),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "auth.refresh-token-reused");

    private final HttpStatus status;
    private final String typeSuffix;

    ErrorCode(HttpStatus status, String typeSuffix) {
        this.status = status;
        this.typeSuffix = typeSuffix;
    }
}
