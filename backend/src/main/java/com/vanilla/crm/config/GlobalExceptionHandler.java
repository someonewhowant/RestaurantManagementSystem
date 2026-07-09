package com.vanilla.crm.config;

import com.vanilla.crm.dto.common.ApiError;
import com.vanilla.crm.exception.BusinessRuleException;
import com.vanilla.crm.exception.DuplicateResourceException;
import com.vanilla.crm.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler that ensures the frontend always receives
 * a structured JSON error response, never a raw stack trace or Whitelabel page.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404: Resource not found ──────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 400: Business logic / illegal argument ──────────────────────
    @ExceptionHandler({BusinessRuleException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─── 400: Validation errors (@Valid) ─────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a
                ));

        log.warn("Validation failed: {}", fieldErrors);

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Один или несколько полей не прошли валидацию")
                .timestamp(LocalDateTime.now())
                .details(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // ─── 409: Conflict (e.g. duplicate email on register) ────────────
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleConflict(DuplicateResourceException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ─── 500: Unexpected errors ────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleRuntime(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера. Обратитесь к администратору.");
    }

    // ─── Helper ──────────────────────────────────────────────────────
    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
