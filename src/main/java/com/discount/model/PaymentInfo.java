package com.discount.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentInfo {
    private String method;
    private String bankName;
    private String cardType;
}