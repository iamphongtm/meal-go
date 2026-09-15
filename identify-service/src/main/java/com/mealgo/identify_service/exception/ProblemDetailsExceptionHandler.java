package com.mealgo.identify_service.exception;

import io.apikit.apicommon.exception.ApiException;
import io.apikit.apicommon.exception.CommonErrorCode;
import io.apikit.apicommon.problem.ProblemProperties;
import io.apikit.apicommon.problem.Problems;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service-owned RFC 9457 advice — customize freely without fighting api-commons.
 */
@RestControllerAdvice
public class ProblemDetailsExceptionHandler extends ResponseEntityExceptionHandler {

	private final ProblemProperties problemProperties;

	public ProblemDetailsExceptionHandler(ProblemProperties problemProperties) {
		this.problemProperties = problemProperties;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		ex.getBindingResult().getGlobalErrors().forEach(error ->
				errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

		ProblemDetail body = Problems.of(
				CommonErrorCode.VALIDATION_FAILED,
				"One or more fields are invalid",
				problemProperties,
				servletRequest(request),
				Map.of("errors", errors));
		return ResponseEntity.badRequest().headers(headers).body(body);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getConstraintViolations().forEach(violation ->
				errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

		return Problems.of(
				CommonErrorCode.VALIDATION_FAILED,
				"One or more constraints were violated",
				problemProperties,
				request,
				Map.of("errors", errors));
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ProblemDetail> handleApiException(
			ApiException ex,
			HttpServletRequest request) {
		ProblemDetail body = ex.toProblemDetail(problemProperties, request);
		return ResponseEntity.status(ex.getStatusCode()).headers(ex.getHeaders()).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnhandled(Exception ex, HttpServletRequest request) {
		logger.error("Unhandled exception", ex);
		return Problems.of(
				CommonErrorCode.INTERNAL,
				"An unexpected error occurred",
				problemProperties,
				request);
	}

	@Override
	protected ResponseEntity<Object> createResponseEntity(
			@Nullable Object body,
			HttpHeaders headers,
			HttpStatusCode statusCode,
			WebRequest request) {

		if (body instanceof ProblemDetail problem) {
			HttpServletRequest servletRequest = servletRequest(request);

			if (problem.getInstance() == null && servletRequest != null) {
				problem.setInstance(URI.create(servletRequest.getRequestURI()));
			}
		}
		return super.createResponseEntity(body, headers, statusCode, request);
	}

	@Nullable
	private static HttpServletRequest servletRequest(WebRequest request) {
		if (request instanceof ServletWebRequest servletWebRequest) {
			return servletWebRequest.getRequest();
		}
		return null;
	}
}
