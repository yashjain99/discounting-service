package com.discount.config;

import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.PaymentInfo;
import com.discount.model.Product;
import com.discount.model.enums.BrandTier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring configuration class for creating test data beans.
 * Provides pre-configured TestData instances for testing discount scenarios.
 */
@Configuration
public class TestDataConfig {

    /**
     * Creates a TestData bean representing a sample cart scenario.
     * Includes a PUMA T-shirt, a GOLD-tier customer, ICICI card payment, and a sample voucher code.
     *
     * @return a TestData object with pre-configured cart items, customer, payment info, and voucher code
     */
    @Bean
    public TestData testData() {
        // PUMA T-shirt with multiple discounts scenario
        Product pumaTshirt = Product.builder()
                .id("PROD_001")
                .brand("PUMA")
                .brandTier(BrandTier.PREMIUM)
                .category("T-Shirts")
                .basePrice(BigDecimal.valueOf(1000))
                .build();

        CartItem cartItem = CartItem.builder()
                .product(pumaTshirt)
                .quantity(1)
                .size("M")
                .build();

        CustomerProfile customer = CustomerProfile.builder()
                .id("CUST_001")
                .tier("GOLD")
                .build();

        PaymentInfo paymentInfo = PaymentInfo.builder()
                .method("CARD")
                .bankName("ICICI")
                .cardType("CREDIT")
                .build();

        return TestData.builder()
                .cartItems(List.of(cartItem))
                .customer(customer)
                .paymentInfo(paymentInfo)
                .voucherCode("SUPER69")
                .build();
    }
}