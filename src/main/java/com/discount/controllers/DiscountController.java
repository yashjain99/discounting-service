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
 * REST controller for handling discount-related operations.
 * Provides endpoints for calculating cart discounts and validating discount codes.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    /**
     * Service for performing discount calculations and validations.
     */
    private final DiscountService discountService;

    /**
     * Calculates the discounted price for a list of cart items for a given customer and optional payment info or voucher code.
     *
     * @param request the request containing cart items, customer information, payment info, and optional voucher code
     * @return the discounted price for the cart wrapped in a ResponseEntity
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
     * Validates whether a discount code is applicable for the given cart items and customer.
     *
     * @param request the request containing the discount code, cart items, and customer information
     * @return a response indicating whether the discount code is valid and a descriptive message
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