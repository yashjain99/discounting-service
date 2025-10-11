package com.discount.service;

import com.discount.exception.DiscountCalculationException;
import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.Discount;
import com.discount.model.DiscountedPrice;
import com.discount.model.PaymentInfo;
import com.discount.model.Product;
import com.discount.model.enums.BrandTier;
import com.discount.model.enums.DiscountType;
import com.discount.repository.DiscountRepository;
import com.discount.service.impl.DiscountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountServiceImpl(discountRepository);
    }

    @Test
    void testCalculateCartDiscounts_WithAllDiscounts() {
        // Arrange
        Product product = createTestProduct();
        CartItem cartItem = CartItem.builder()
                .product(product)
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

        setupMockDiscounts();

        // Act
        DiscountedPrice result = discountService.calculateCartDiscounts(
                List.of(cartItem),
                customer,
                paymentInfo
        );

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000), result.getOriginalPrice());
        assertTrue(result.getFinalPrice().compareTo(
                result.getOriginalPrice()) < 0);
        assertFalse(result.getAppliedDiscounts().isEmpty());
    }

    @Test
    void testValidateDiscountCode_ValidCode() {
        // Arrange
        String code = "PUMA40";
        Product product = createTestProduct();
        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(1)
                .size("M")
                .build();

        CustomerProfile customer = CustomerProfile.builder()
                .id("CUST_001")
                .tier("GOLD")
                .build();

        Discount discount = Discount.builder()
                .id("BRAND_PUMA")
                .code(code)
                .type(DiscountType.BRAND)
                .value(BigDecimal.valueOf(40))
                .isPercentage(true)
                .applicableBrands(Set.of("PUMA"))
                .build();

        when(discountRepository.findByCode(code))
                .thenReturn(Optional.of(discount));

        // Act
        boolean result = discountService.validateDiscountCode(
                code,
                List.of(cartItem),
                customer
        );

        // Assert
        assertTrue(result);
    }

    @Test
    void testCalculateCartDiscounts_EmptyCart_ThrowsException() {
        // Arrange
        CustomerProfile customer = CustomerProfile.builder()
                .id("CUST_001")
                .tier("GOLD")
                .build();

        // Act & Assert
        assertThrows(DiscountCalculationException.class, () ->
                discountService.calculateCartDiscounts(
                        Collections.emptyList(),
                        customer,
                        null
                )
        );
    }

    private Product createTestProduct() {
        return Product.builder()
                .id("PROD_001")
                .brand("PUMA")
                .brandTier(BrandTier.PREMIUM)
                .category("T-Shirts")
                .basePrice(BigDecimal.valueOf(1000))
                .build();
    }

    private void setupMockDiscounts() {
        Discount brandDiscount = Discount.builder()
                .id("BRAND_PUMA")
                .type(DiscountType.BRAND)
                .value(BigDecimal.valueOf(40))
                .isPercentage(true)
                .build();

        Discount categoryDiscount = Discount.builder()
                .id("CAT_TSHIRT")
                .type(DiscountType.CATEGORY)
                .value(BigDecimal.valueOf(10))
                .isPercentage(true)
                .build();

        Discount bankOffer = Discount.builder()
                .id("BANK_ICICI")
                .type(DiscountType.BANK_OFFER)
                .value(BigDecimal.valueOf(10))
                .isPercentage(true)
                .build();

        when(discountRepository.findBrandDiscount("PUMA"))
                .thenReturn(Optional.of(brandDiscount));
        when(discountRepository.findCategoryDiscount("T-Shirts"))
                .thenReturn(Optional.of(categoryDiscount));
        when(discountRepository.findBankOffer("ICICI", "CREDIT"))
                .thenReturn(Optional.of(bankOffer));
    }
}