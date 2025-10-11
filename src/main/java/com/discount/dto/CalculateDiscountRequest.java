package com.discount.dto;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.PaymentInfo;
import lombok.Data;
import lombok.Builder;

import java.util.List;

/**
 * Request DTO for calculate discount endpoint
 */
@Data
@Builder
public class CalculateDiscountRequest {
    private List<CartItem> cartItems;
    private CustomerProfile customer;
    private PaymentInfo paymentInfo;
    private String voucherCode;
}