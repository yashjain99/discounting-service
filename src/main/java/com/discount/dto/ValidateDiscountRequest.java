package com.discount.dto;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for validate discount endpoint
 * Contains the discount code to validate along with the customer's cart and profile information.
 */
@Data
@Builder
public class ValidateDiscountRequest {

    /**
     * The discount code to be validated.
     */
    private String code;

    /**
     * The list of items in the customer's cart for which the discount code is being validated.
     */
    private List<CartItem> cartItems;


    /**
     * The profile information of the customer validating the discount code.
     */
    private CustomerProfile customer;
}