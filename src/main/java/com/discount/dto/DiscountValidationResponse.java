package com.discount.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for discount validation
 * Contains the discount code, its validity status, and an informational message.
 */
@Data
@Builder
public class DiscountValidationResponse {

    /**
     * The discount code that was validated.
     */
    private String code;


    /**
     * Indicates whether the discount code is valid and applicable.
     */
    private boolean valid;

    /**
     * A message providing additional information about the validation result.
     */
    private String message;
}