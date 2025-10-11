package com.discount.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerProfile {
    private String id;
    private String tier;
}