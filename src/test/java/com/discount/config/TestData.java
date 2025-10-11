package com.discount.config;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.PaymentInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents test data for discount calculations.
 * Contains a list of cart items, customer profile, payment information, and an optional voucher code.
 * Can be used to simulate requests in tests or sample scenarios.
 */
@Data
@Builder
public class TestData {
    private List<CartItem> cartItems;
    private CustomerProfile customer;
    private PaymentInfo paymentInfo;
    private String voucherCode;
}