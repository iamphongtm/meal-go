package com.mealgo.identify_service.exception;

import com.mealgo.identify_service.problem.ProblemProperties;
import com.mealgo.identify_service.problem.Problems;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import jakarta.servlet.http.HttpServletRequest;

public class ApiException extends ErrorResponseException {

	private final ErrorCode errorCode;
	private final String detail;

	public ApiException(ErrorCode errorCode, String detail) {
		this(errorCode, detail, new ProblemProperties());
	}

	public ApiException(ErrorCode errorCode, String detail, ProblemProperties problemProperties) {
		this(errorCode, detail, problemProperties, null);
	}

	public ApiException(ErrorCode errorCode, String detail, Throwable cause) {
		this(errorCode, detail, new ProblemProperties(), cause);
	}

	public ApiException(ErrorCode errorCode, String detail, ProblemProperties problemProperties, Throwable cause) {
		super(errorCode.status(), Problems.of(errorCode, detail, problemProperties, null), cause);
		this.errorCode = errorCode;
		this.detail = detail;
	}

	public static ApiException of(ErrorCode errorCode, String detail) {
		return new ApiException(errorCode, detail);
	}

	public static ApiException badRequest(String detail) {
		return of(CommonErrorCode.BAD_REQUEST, detail);
	}

	public static ApiException notFound(String detail) {
		return of(CommonErrorCode.NOT_FOUND, detail);
	}

	public static ApiException conflict(String detail) {
		return of(CommonErrorCode.CONFLICT, detail);
	}

	public static ApiException unauthorized(String detail) {
		return of(CommonErrorCode.UNAUTHORIZED, detail);
	}

	public static ApiException forbidden(String detail) {
		return of(CommonErrorCode.FORBIDDEN, detail);
	}

	public ProblemDetail body() {
		return getBody();
	}

	public ErrorCode errorCode() {
		return errorCode;
	}

	public ProblemDetail toProblemDetail(ProblemProperties problemProperties, HttpServletRequest request) {
		return Problems.of(errorCode, detail, problemProperties, request);
	}
}
