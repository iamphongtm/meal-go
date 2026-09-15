package io.apikit.apicommon.exception;

import io.apikit.apicommon.problem.ProblemProperties;
import io.apikit.apicommon.problem.Problems;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Base exception for domain and HTTP errors exposed by an API.
 *
 * <p>Each instance carries an {@link ErrorCode} and a client-facing detail.
 * A consuming service can call {@link #toProblemDetail(ProblemProperties,
 * HttpServletRequest)} from its exception handler to create a response using
 * that application's problem URI configuration.</p>
 *
 * @author phongtm
 */
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

	/**
	 * Builds the response body with the consuming application's configuration.
	 */
	public ProblemDetail toProblemDetail(ProblemProperties problemProperties, HttpServletRequest request) {
		return Problems.of(errorCode, detail, problemProperties, request);
	}
}
