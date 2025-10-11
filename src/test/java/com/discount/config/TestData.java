package com.discount.config;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.PaymentInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TestData {
    private List<CartItem> cartItems;
    private CustomerProfile customer;
    private PaymentInfo paymentInfo;
    private String voucherCode;
}