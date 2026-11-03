package com.mealgo.restaurantservice.domain.shared;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Object[] messageArgs;

    public BusinessException(ErrorCode errorCode, Object... messageArgs) {
        super();
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, (Object[]) null);
    }

}
