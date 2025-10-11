package com.discount.exception;

import com.discount.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST controllers.
 * Handles exceptions thrown in the application and maps them to structured error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * Handles exceptions of type DiscountCalculationException.
     * Returns a BAD_REQUEST (400) response with details about the discount calculation error.
     *
     * @param ex the DiscountCalculationException thrown
     * @return a ResponseEntity containing an ErrorResponse with error details
     */
    @ExceptionHandler(DiscountCalculationException.class)
    public ResponseEntity<ErrorResponse> handleDiscountCalculationException(
            DiscountCalculationException ex
    ) {
        log.error("Discount calculation error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Discount Calculation Error")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles exceptions of type DiscountValidationException.
     * Returns a BAD_REQUEST (400) response with details about the discount validation error.
     *
     * @param ex the DiscountValidationException thrown
     * @return a ResponseEntity containing an ErrorResponse with error details
     */
    @ExceptionHandler(DiscountValidationException.class)
    public ResponseEntity<ErrorResponse> handleDiscountValidationException(
            DiscountValidationException ex
    ) {
        log.error("Discount validation error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Discount Validation Error")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other exceptions not specifically handled by other methods.
     * Returns an INTERNAL_SERVER_ERROR (500) response with a generic error message.
     *
     * @param ex the Exception thrown
     * @return a ResponseEntity containing an ErrorResponse with generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}