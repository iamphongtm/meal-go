package com.mealgo.restaurantservice.api.rest.exception;

import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @Value("${spring.application.name}")
    private String servicename;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String urn = "urn:mealgo:" + servicename + ":validation-error";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Input validation failed."
        );
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create(urn));
        problemDetail.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));

        List<Map<String, String>> invalidParams = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("field", fieldError.getField());
            errorMap.put("message", fieldError.getDefaultMessage());
            invalidParams.add(errorMap);
        }
        problemDetail.setProperty("invalid_params", invalidParams);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or contains an invalid value."
        );

        problem.setTitle("Invalid Request Body");
        problem.setType(URI.create(
                "urn:mealgo:" + servicename + ":invalid-request-body"
        ));
        problem.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));

        return handleExceptionInternal(ex, problem, headers, status, request);
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

        HttpStatus status = ErrorCodeHttpMapper.statusOf(errorCode);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(translatedTitle);
        problemDetail.setType(URI.create(urn));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return new ResponseEntity<>(problemDetail, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception for request " + request.getRequestURI(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred."
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create(
                "urn:mealgo:" + servicename + ":internal-server-error"
        ));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.internalServerError().body(problemDetail);
    }
}
