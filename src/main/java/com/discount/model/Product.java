package com.discount.model;

import com.discount.model.enums.BrandTier;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Product {
    private String id;
    private String brand;
    private BrandTier brandTier;
    private String category;
    private BigDecimal basePrice;
    private BigDecimal currentPrice;
}