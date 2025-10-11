package com.discount.controllers;

import com.discount.dto.CalculateDiscountRequest;
import com.discount.dto.DiscountValidationResponse;
import com.discount.dto.ValidateDiscountRequest;
import com.discount.model.DiscountedPrice;
import com.discount.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for discount operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    /**
     * Calculate discounts for a cart
     * POST /api/v1/discounts/calculate
     */
    @PostMapping("/calculate")
    public ResponseEntity<DiscountedPrice> calculateDiscounts(
            @RequestBody CalculateDiscountRequest request
    ) {
        log.info("Calculating discounts for customer: {}", request.getCustomer().getId());

        DiscountedPrice result = discountService.calculateCartDiscounts(
                request.getCartItems(),
                request.getCustomer(),
                request.getPaymentInfo(),
                request.getVoucherCode()
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Validate a discount code
     * POST /api/v1/discounts/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<DiscountValidationResponse> validateDiscountCode(
            @RequestBody ValidateDiscountRequest request
    ) {
        log.info("Validating discount code: {}", request.getCode());

        boolean isValid = discountService.validateDiscountCode(
                request.getCode(),
                request.getCartItems(),
                request.getCustomer()
        );

        DiscountValidationResponse response = DiscountValidationResponse.builder()
                .code(request.getCode())
                .valid(isValid)
                .message(isValid ? "Discount code is valid" : "Discount code is invalid or not applicable")
                .build();

        return ResponseEntity.ok(response);
    }
}