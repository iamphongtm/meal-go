package com.mealgo.searchservice.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(ApplicationException exception) {
        return error(exception.getErrorCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleInvalidArgument(IllegalArgumentException exception) {
        return error(ErrorCode.INVALID_ARGUMENT);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ErrorCode code = status.is5xxServerError()
                ? ErrorCode.INTERNAL_ERROR : ErrorCode.INVALID_REQUEST;
        // Preserve Spring MVC's status and headers (e.g. Allow for HTTP 405).
        return super.handleExceptionInternal(
                exception, problem(status, code), headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception exception) {
        // Never expose exception messages or provider URLs containing credentials.
        return error(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<Object> error(ErrorCode code) {
        HttpStatusCode status = HttpStatusCode.valueOf(code.getStatus());
        return ResponseEntity.status(status).body(problem(status, code));
    }

    private ProblemDetail problem(HttpStatusCode status, ErrorCode code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, code.getMessage());
        problem.setProperty("code", code.name());
        return problem;
    }
}
