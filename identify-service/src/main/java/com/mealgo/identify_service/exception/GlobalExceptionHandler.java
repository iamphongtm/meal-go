package com.mealgo.identify_service.exception;

import com.mealgo.identify_service.problem.ProblemProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles application and Spring MVC exceptions as RFC 9457 responses.
 *
 * @author phongtm
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ProblemProperties problemProperties;

    public GlobalExceptionHandler(ProblemProperties problemProperties) {
        this.problemProperties = problemProperties;
    }

    private static void addError(
            Map<String, List<String>> errors,
            String field,
            String message) {
        errors.computeIfAbsent(field, ignored -> new ArrayList<>()).add(message);
    }

    @Nullable
    private static HttpServletRequest servletRequest(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest();
        }
        return null;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            addError(errors, fieldError.getField(), fieldError.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                addError(errors, error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail body = validationProblem(
                CommonErrorCode.VALIDATION_FAILED,
                "One or more fields are invalid",
                servletRequest(request),
                errors);
        return ResponseEntity.status(status).headers(headers).body(body);
    }

    /**
     * Converts method and path parameter validation failures to a Problem Detail response.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(violation ->
                addError(errors, violation.getPropertyPath().toString(), violation.getMessage()));

        return response(HttpStatus.BAD_REQUEST, validationProblem(
                CommonErrorCode.VALIDATION_FAILED,
                "One or more constraints were violated",
                request,
                errors));
    }

    /**
     * Converts an application-defined API exception while preserving its status and headers.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ProblemDetail> handleApiException(
            ApiException ex,
            HttpServletRequest request) {
        ProblemDetail body = ex.toProblemDetail(problemProperties, request);
        return ResponseEntity.status(ex.getStatusCode()).headers(ex.getHeaders()).body(body);
    }

    /**
     * Returns a safe response for unexpected exceptions and logs the cause server-side.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandled(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception", ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                Problems.of(
                        CommonErrorCode.INTERNAL,
                        "An unexpected error occurred",
                        problemProperties,
                        request));
    }

    private ProblemDetail validationProblem(
            CommonErrorCode errorCode,
            String detail,
            HttpServletRequest request,
            Map<String, List<String>> errors) {
        return Problems.of(errorCode, detail, problemProperties, request, Map.of("errors", errors));
    }

    private ResponseEntity<ProblemDetail> response(
            HttpStatusCode status,
            ProblemDetail body) {
        return ResponseEntity.status(status).body(body);
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
}
