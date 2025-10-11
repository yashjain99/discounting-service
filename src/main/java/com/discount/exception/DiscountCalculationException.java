package com.discount.exception;

/**
 * Exception thrown when an error occurs during discount calculation.
 * Extends RuntimeException to indicate an unchecked exception.
 */
public class DiscountCalculationException extends RuntimeException {

    /**
     * Constructs a new DiscountCalculationException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public DiscountCalculationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DiscountCalculationException with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of the exception
     */
    public DiscountCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}