package com.mealgo.identify_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain error catalog for identify-service.
 * Messages are centralized here; callers only pass the code (+ format args).
 */
public enum IdentifyErrorCode implements ErrorCode {

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User %s was not found"),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email %s is already registered");

	private final HttpStatus status;
	private final String messageTemplate;
	private final String titleOverride;

	IdentifyErrorCode(HttpStatus status, String messageTemplate) {
		this(status, messageTemplate, null);
	}

	IdentifyErrorCode(HttpStatus status, String messageTemplate, String titleOverride) {
		this.status = status;
		this.messageTemplate = messageTemplate;
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

	/**
	 * Builds {@code detail} from the centralized template.
	 */
	public String message(Object... args) {
		if (args == null || args.length == 0) {
			return messageTemplate;
		}
		return String.format(messageTemplate, args);
	}
}
