package com.discount.model;

import com.discount.model.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class Discount {
    private String id;
    private String code;
    private DiscountType type;
    private BigDecimal value;
    private boolean isPercentage;
    private Set<String> applicableBrands;
    private Set<String> applicableCategories;
    private Set<String> excludedBrands;
    private Set<String> excludedCategories;
    private Set<String> requiredCustomerTiers;
    private String bankName;
    private String cardType;
}