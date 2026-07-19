package com.mapnaom.ticketingplatform.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        logException(ex, request, "Resource not found");
        return response(HttpStatus.NOT_FOUND, "Resource Not Found", userMessage(ex), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        logException(ex, request, "Request validation failed");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fieldError
                    ? fieldError.getField()
                    : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        return response(HttpStatus.BAD_REQUEST, "Validation Failed", errors.toString(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        logException(ex, request, "Invalid request argument");
        return response(HttpStatus.BAD_REQUEST, "Invalid Argument", userMessage(ex), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        logException(ex, request, "Authentication failed");
        return response(HttpStatus.UNAUTHORIZED, "Unauthorized", userMessage(ex), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        logException(ex, request, "Access denied");
        return response(HttpStatus.FORBIDDEN, "Forbidden", userMessage(ex), request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MissingPathVariableException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception ex, HttpServletRequest request) {
        logException(ex, request, "Malformed or incomplete request");
        return response(HttpStatus.BAD_REQUEST, "Bad Request", userMessage(ex), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logException(ex, request, "Uploaded file is too large");
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                "Uploaded file is too large", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logException(ex, request, "Database constraint violation");
        return response(HttpStatus.CONFLICT, "Conflict",
                "The request conflicts with existing data", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logException(ex, request, "HTTP method not supported");
        return response(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "HTTP method is not supported for this endpoint", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(
            Exception ex, HttpServletRequest request) {
        logException(ex, request, "Unhandled exception");
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected server error occurred", request);
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(), status.value(), error, message, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, status);
    }

    private String userMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "The request is invalid"
                : ex.getMessage();
    }

    private void logException(Exception ex, HttpServletRequest request, String category) {
        log.error("API exception: category={}, method={}, uri={}, message={}",
                category, request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
    }

    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {}
}
