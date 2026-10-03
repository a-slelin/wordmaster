package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.error.ErrorResponse;
import a.slelin.work.word.master.dto.error.ValidationErrorResponse;
import a.slelin.work.word.master.exception.AccessForbiddenException;
import a.slelin.work.word.master.exception.BusinessFault;
import a.slelin.work.word.master.exception.DuplicateResourceException;
import a.slelin.work.word.master.exception.EntityNotFoundByPropertyException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts exceptions to {@link ErrorResponse} with a proper HTTP status.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundByPropertyException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundByPropertyException e, ServletWebRequest request) {
        return build(HttpStatus.NOT_FOUND, e, request, Map.of(
                "entity", e.getEntity().getSimpleName(),
                "property", e.getProperty(),
                "value", String.valueOf(e.getInvalidProperty())));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException e, ServletWebRequest request) {
        return build(HttpStatus.CONFLICT, e, request, Map.of("field", e.getField(), "value", e.getValue()));
    }

    @ExceptionHandler(BusinessFault.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessFault e, ServletWebRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, e, request, null);
    }

    @ExceptionHandler({AccessForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException e, ServletWebRequest request) {
        return build(HttpStatus.FORBIDDEN, e, request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, ServletWebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, e, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException e, ServletWebRequest request) {
        List<Map<String, Object>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("field", error.getField());
                    item.put("message", error.getDefaultMessage());
                    item.put("value", error.getRejectedValue());
                    return item;
                })
                .toList();
        return build(HttpStatus.BAD_REQUEST, e, request, "Request validation failed.", Map.of("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e,
                                                                   ServletWebRequest request) {
        List<ValidationErrorResponse> errors = ValidationErrorResponse.fromException(e);
        return build(HttpStatus.BAD_REQUEST, e, request, "Validation failed.", Map.of("errors", errors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(HandlerMethodValidationException e,
                                                                ServletWebRequest request) {
        return build(HttpStatus.BAD_REQUEST, e, request, "Request parameters validation failed.", null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e, ServletWebRequest request) {
        return build(HttpStatus.BAD_REQUEST, e, request, "Malformed request.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                                  ServletWebRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, e, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e, ServletWebRequest request) {
        return build(HttpStatus.NOT_FOUND, e, request, "Resource not found.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e, ServletWebRequest request) {
        log.error("Unexpected error on {} {}", request.getHttpMethod(), request.getRequest().getRequestURI(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e, request, "Unexpected server error.", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception e, ServletWebRequest request,
                                                Map<String, Object> details) {
        return build(status, e, request, e.getMessage(), details);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception e, ServletWebRequest request,
                                                String message, Map<String, Object> details) {
        ErrorResponse body = ErrorResponse.buildDefault(e, request)
                .path(request.getRequest().getRequestURI())
                .httpStatus(status)
                .message(message == null ? status.getReasonPhrase() : message)
                .debugMessage(status.is5xxServerError() ? null : e.getMessage())
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
