package com.mealgo.identify_service.exception;

import io.apikit.apicommon.exception.ApiException;

/**
 * Base for identify-service business errors.
 * Pass only {@link IdentifyErrorCode} (+ optional message args).
 */
public class BusinessException extends ApiException {

	public BusinessException(IdentifyErrorCode errorCode, Object... messageArgs) {
		super(errorCode, errorCode.message(messageArgs));
	}
}
