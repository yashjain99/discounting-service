package com.discount.dto;

import lombok.Data;
import lombok.Builder;

/**
 * Response DTO for discount validation
 */
@Data
@Builder
public class DiscountValidationResponse {
    private String code;
    private boolean valid;
    private String message;
}