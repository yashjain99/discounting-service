package com.discount.dto;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * Request DTO for validate discount endpoint
 */
@Data
@Builder
public class ValidateDiscountRequest {
    private String code;
    private List<CartItem> cartItems;
    private CustomerProfile customer;
}