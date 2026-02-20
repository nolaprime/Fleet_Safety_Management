package com.fleet.scoring.controller;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(ConstraintViolationException exception) {
        log.warn("Validation failed for request: {}", exception.getMessage());

        Map<String, Object> response = Map.of(
                "status", "REJECTED",
                "message", exception.getMessage()
        );

        return ResponseEntity.status(400).body(response);
    }
}
