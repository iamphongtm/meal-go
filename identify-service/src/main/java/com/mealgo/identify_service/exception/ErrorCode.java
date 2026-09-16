package com.mealgo.identify_service.exception;

import org.springframework.http.HttpStatus;

import java.util.Locale;

public interface ErrorCode {

	String code();

	HttpStatus status();

	default String typeSuffix() {
		return code().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	default String title() {
		return status().getReasonPhrase();
	}
}
