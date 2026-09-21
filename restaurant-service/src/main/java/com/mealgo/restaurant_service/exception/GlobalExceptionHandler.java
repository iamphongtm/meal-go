package com.mealgo.restaurant_service.exception;

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
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @Value("${spring.application.name}")
    private String servicename;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        //urn:mealgo:restaurant-service:validation-error
        String urn = "urn:mealgo:" + servicename + ":validation-error";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Input validation failed."
        );
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create(urn));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        List<Map<String, String>> invalidParams = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("field", fieldError.getField());
            errorMap.put("message", fieldError.getDefaultMessage());
            invalidParams.add(errorMap);
        }
        problemDetail.setProperty("invalid_params", invalidParams);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or contains an invalid value."
        );

        problem.setTitle("Invalid Request Body");
        problem.setType(URI.create(
                "urn:mealgo:" + servicename + ":invalid-request-body"
        ));
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        ErrorCode errorCode = ex.getErrorCode();
        String urn = "urn:mealgo:" + servicename + ":" + errorCode.getTypeSuffix();

        String detailKey = errorCode.getTypeSuffix() + ".detail";
        String titleKey = errorCode.getTypeSuffix() + ".title";

        String detail = messageSource.getMessage(detailKey, ex.getMessageArgs(), null, locale);
        String translatedTitle = messageSource.getMessage(titleKey, null, null, locale);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
        problemDetail.setTitle(translatedTitle);
        problemDetail.setType(URI.create(urn));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return new ResponseEntity<>(problemDetail, errorCode.getStatus());
    }
}
