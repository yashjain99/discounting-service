package com.discount.dto;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.PaymentInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for discount calculation.
 * Contains the information required to calculate the discounted price of a customer's cart.
 */
@Data
@Builder
public class CalculateDiscountRequest {

    /**
     * The list of items in the customer's cart.
     */
    private List<CartItem> cartItems;

    /**
     * The profile information of the customer for whom the discount is being calculated.
     */
    private CustomerProfile customer;

    /**
     * Optional payment information used to determine applicable discounts.
     */
    private PaymentInfo paymentInfo;

    /**
     * Optional voucher code to apply additional discounts.
     */
    private String voucherCode;
}