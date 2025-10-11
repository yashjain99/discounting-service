package com.discount.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Standard error response format
 */
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}