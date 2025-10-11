package com.discount.exception;

/**
 * Exception thrown when a discount code validation fails or is invalid.
 * Extends RuntimeException to indicate an unchecked exception.
 */
public class DiscountValidationException extends RuntimeException {

    /**
     * Constructs a new DiscountValidationException with the specified detail message.
     *
     * @param message the detail message describing the reason for the validation failure
     */
    public DiscountValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DiscountValidationException with the specified detail message and cause.
     *
     * @param message the detail message describing the reason for the validation failure
     * @param cause   the underlying cause of the exception
     */
    public DiscountValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}