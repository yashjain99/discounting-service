package com.discount.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DiscountedPrice {
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private Map<String, BigDecimal> appliedDiscounts;
    private String message;
}