package com.discount.service;

import com.discount.config.TestData;
import com.discount.config.TestDataConfig;
import com.discount.model.DiscountedPrice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(TestDataConfig.class)
class DiscountServiceIntegrationTest {

    @Autowired
    private DiscountService discountService;

    @Autowired
    private TestData testData;

    @Test
    void testCompleteDiscountFlow_PumaTshirtScenario() {
        // Arrange - Using test data from config

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                testData.getCartItems(),
                testData.getCustomer(),
                testData.getPaymentInfo(),
                testData.getVoucherCode()
        );

        // Assert
        assertEquals(BigDecimal.valueOf(1000), result.getOriginalPrice());

        // Verify all three discounts are applied
        assertTrue(result.getAppliedDiscounts().containsKey("Brand Discount"));
        assertTrue(result.getAppliedDiscounts().containsKey("Category Discount"));
        assertTrue(result.getAppliedDiscounts().containsKey("Bank Offer - ICICI"));
        assertTrue(result.getAppliedDiscounts().containsKey("Voucher - SUPER69"));

        // Expected: 1000 -> 600 (40% off) -> 540 (10% off) -> 486 (10% off) -> 150.66 (69% off)
        assertEquals(0, result.getFinalPrice().compareTo(BigDecimal.valueOf(150.66)));

        // Verify discount message is generated
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Brand Discount"));
    }

    @Test
    void testDiscountCalculation_WithoutBankOffer() {
        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                testData.getCartItems(),
                testData.getCustomer(),
                null,
                testData.getVoucherCode()
        );

        // Assert
        // Expected: 1000 -> 600 (40% off) -> 540 (10% off) -> 167 (69% off)
        assertEquals(0, result.getFinalPrice().compareTo(BigDecimal.valueOf(167.40)));
        assertFalse(result.getAppliedDiscounts().containsKey("Bank Offer - ICICI"));
    }

    @Test
    void testDiscountCalculation_WithoutVoucherCode() {
        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                testData.getCartItems(),
                testData.getCustomer(),
                testData.getPaymentInfo(),
                null
        );

        // Assert
        // Expected: 1000 -> 600 (40% off) -> 540 (10% off)
        assertEquals(0, result.getFinalPrice().compareTo(BigDecimal.valueOf(486.00)));
        assertFalse(result.getAppliedDiscounts().containsKey("Voucher - SUPER69"));
    }

    @Test
    void testValidateDiscountCode_BrandDiscount() {
        // Act
        boolean isValid = discountService.validateDiscountCode(
                "PUMA40",
                testData.getCartItems(),
                testData.getCustomer()
        );

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateDiscountCode_CategoryDiscount() {
        // Act
        boolean isValid = discountService.validateDiscountCode(
                "TSHIRT10",
                testData.getCartItems(),
                testData.getCustomer()
        );

        // Assert
        assertTrue(isValid);
    }
}