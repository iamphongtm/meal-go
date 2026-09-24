package com.mealgo.identify_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @Value("${spring.application.name}")
    private String serviceName;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Input validation failed."
        );
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create("urn:mealgo:" + serviceName + ":validation-error"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, String>> invalidParams = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> error = new HashMap<>();
            error.put("field", fieldError.getField());
            error.put("message", fieldError.getDefaultMessage());
            invalidParams.add(error);
        }

        problemDetail.setProperty("invalid_params", invalidParams);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or contains an invalid value."
        );
        problemDetail.setTitle("Invalid Request Body");
        problemDetail.setType(URI.create("urn:mealgo:" + serviceName + ":invalid-request-body"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ErrorCode errorCode = ex.getErrorCode();
        String typeSuffix = errorCode.getTypeSuffix();
        String detail = messageSource.getMessage(
                typeSuffix + ".detail",
                ex.getMessageArgs(),
                locale
        );
        String title = messageSource.getMessage(typeSuffix + ".title", null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("urn:mealgo:" + serviceName + ":" + typeSuffix));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
    }
}
