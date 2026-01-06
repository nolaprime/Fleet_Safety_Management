package com.fleet.telemetry.controller.exception_handler;
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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException exception) {

        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": must be between " + getRange(error))
                .collect(Collectors.toList());

        String primaryMessage = "Validation failed: " + (errors.isEmpty() ? "invalid input" : errors.get(0));

        // Log the failure for observability
        log.warn("Validation failed for request: {}", errors);

        Map<String, Object> response = Map.of(
                "status", "REJECTED",
                "message", primaryMessage,
                "errors", errors
        );

        return ResponseEntity.status(400).body(response);
    }

    private String getRange(org.springframework.validation.FieldError error) {
        // Simple helper to match your specific error string format
        return switch (error.getField()) {
            case "speed" -> "0 and 200";
            case "fuelLevel" -> "0 and 100";
            case "engineTemp" -> "0 and 150";
            case "tirePressure" -> "20 and 120";
            default -> "valid range";
        };
    }
}
