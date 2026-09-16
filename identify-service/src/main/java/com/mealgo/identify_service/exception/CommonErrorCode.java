package com.mealgo.identify_service.exception;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

	BAD_REQUEST(HttpStatus.BAD_REQUEST),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
	FORBIDDEN(HttpStatus.FORBIDDEN),
	NOT_FOUND(HttpStatus.NOT_FOUND),
	CONFLICT(HttpStatus.CONFLICT),
	INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR),
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation Failed");

	private final HttpStatus status;
	private final String titleOverride;

	CommonErrorCode(HttpStatus status) {
		this(status, null);
	}

	CommonErrorCode(HttpStatus status, String titleOverride) {
		this.status = status;
		this.titleOverride = titleOverride;
	}

	@Override
	public String code() {
		return name();
	}

	@Override
	public HttpStatus status() {
		return status;
	}

	@Override
	public String title() {
		return titleOverride != null ? titleOverride : status.getReasonPhrase();
	}
}
