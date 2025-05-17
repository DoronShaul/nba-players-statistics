package com.doron.shaul.nba.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getAllValidationResults().forEach(result -> {
            result.getResolvableErrors().forEach(error -> {
                String fieldName = error.getDefaultMessage().contains(":")
                        ? error.getDefaultMessage().split(":")[0]
                        : "error";
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        });

        if (errors.isEmpty()) {
            errors.put("message", "Validation failed");
        }

        return errors;
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEmptyResultDataAccessException() {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Resource not found");
        return error;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllServerExceptions(Exception ex) {
        String errorReference = UUID.randomUUID().toString();

        log.error("Server error (Ref: {}, Type: {}): {}",
                errorReference,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);

        Map<String, String> error = new HashMap<>();
        error.put("message", "An unexpected server error occurred");
        error.put("errorReference", errorReference);
        return error;
    }
}