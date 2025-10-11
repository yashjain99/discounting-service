package com.discount.service.impl;

import com.discount.exception.DiscountCalculationException;
import com.discount.exception.DiscountValidationException;
import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.Discount;
import com.discount.model.DiscountedPrice;
import com.discount.model.PaymentInfo;
import com.discount.model.Product;
import com.discount.repository.DiscountRepository;
import com.discount.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public DiscountedPrice calculateCartDiscounts(
            List<CartItem> cartItems,
            CustomerProfile customer,
            PaymentInfo paymentInfo
    ) throws DiscountCalculationException {

        try {
            validateInputs(cartItems, customer);

            BigDecimal originalPrice = calculateOriginalPrice(cartItems);
            Map<String, BigDecimal> appliedDiscounts = new LinkedHashMap<>();

            // Step 1: Apply brand and category discounts
            BigDecimal priceAfterBrandCategory = applyBrandAndCategoryDiscounts(
                    cartItems, appliedDiscounts
            );

            // Step 2: Apply voucher codes (if any stored in session/context)
            BigDecimal priceAfterVoucher = applyVoucherDiscounts(
                    cartItems, customer, priceAfterBrandCategory, appliedDiscounts
            );

            // Step 3: Apply bank offers
            BigDecimal finalPrice = applyBankOffers(
                    paymentInfo, priceAfterVoucher, appliedDiscounts
            );

            String message = buildDiscountMessage(appliedDiscounts);

            return DiscountedPrice.builder()
                    .originalPrice(originalPrice)
                    .finalPrice(finalPrice)
                    .appliedDiscounts(appliedDiscounts)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("Error calculating discounts", e);
            throw new DiscountCalculationException(
                    "Failed to calculate discounts: " + e.getMessage(), e
            );
        }
    }

    @Override
    public boolean validateDiscountCode(
            String code,
            List<CartItem> cartItems,
            CustomerProfile customer
    ) throws DiscountValidationException {

        try {
            Discount discount = discountRepository.findByCode(code)
                    .orElseThrow(() -> new DiscountValidationException(
                            "Discount code not found: " + code
                    ));

            // Check customer tier requirements
            if (discount.getRequiredCustomerTiers() != null
                && !discount.getRequiredCustomerTiers().isEmpty()) {
                if (!discount.getRequiredCustomerTiers()
                        .contains(customer.getTier())) {
                    return false;
                }
            }

            // Check if any cart item matches discount criteria
            return cartItems.stream()
                    .anyMatch(item -> isDiscountApplicable(discount, item));

        } catch (DiscountValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating discount code", e);
            throw new DiscountValidationException(
                    "Failed to validate discount code: " + e.getMessage(), e
            );
        }
    }

    private void validateInputs(List<CartItem> cartItems, CustomerProfile customer) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new DiscountValidationException("Cart items cannot be empty");
        }
        if (customer == null) {
            throw new DiscountValidationException("Customer profile is required");
        }
    }

    private BigDecimal calculateOriginalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getBasePrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal applyBrandAndCategoryDiscounts(
            List<CartItem> cartItems,
            Map<String, BigDecimal> appliedDiscounts
    ) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalBrandDiscount = BigDecimal.ZERO;
        BigDecimal totalCategoryDiscount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            BigDecimal itemPrice = product.getBasePrice();

            // Apply brand discount
            Optional<Discount> brandDiscount = discountRepository
                    .findBrandDiscount(product.getBrand());
            if (brandDiscount.isPresent()) {
                BigDecimal discount = calculateDiscount(
                        itemPrice, brandDiscount.get()
                );
                itemPrice = itemPrice.subtract(discount);
                totalBrandDiscount = totalBrandDiscount.add(
                        discount.multiply(BigDecimal.valueOf(item.getQuantity()))
                );
            }

            // Apply category discount
            Optional<Discount> categoryDiscount = discountRepository
                    .findCategoryDiscount(product.getCategory());
            if (categoryDiscount.isPresent()) {
                BigDecimal discount = calculateDiscount(
                        itemPrice, categoryDiscount.get()
                );
                itemPrice = itemPrice.subtract(discount);
                totalCategoryDiscount = totalCategoryDiscount.add(
                        discount.multiply(BigDecimal.valueOf(item.getQuantity()))
                );
            }

            // Update product current price
            product.setCurrentPrice(itemPrice);
            totalPrice = totalPrice.add(
                    itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }

        if (totalBrandDiscount.compareTo(BigDecimal.ZERO) > 0) {
            appliedDiscounts.put("Brand Discount", totalBrandDiscount);
        }
        if (totalCategoryDiscount.compareTo(BigDecimal.ZERO) > 0) {
            appliedDiscounts.put("Category Discount", totalCategoryDiscount);
        }

        return totalPrice;
    }

    private BigDecimal applyVoucherDiscounts(
            List<CartItem> cartItems,
            CustomerProfile customer,
            BigDecimal currentPrice,
            Map<String, BigDecimal> appliedDiscounts
    ) {
        // For this implementation, vouchers would be passed separately
        // This is a placeholder for voucher logic
        return currentPrice;
    }

    private BigDecimal applyBankOffers(
            // Removed Optional as a parameter: it can cause issues with Jackson, JPA and it is discouraged.
            PaymentInfo paymentInfo,
            BigDecimal currentPrice,
            Map<String, BigDecimal> appliedDiscounts
    ) {

        // Handle nullable paymentInfo
        Optional<PaymentInfo> optionalPayment = Optional.ofNullable(paymentInfo);

        if (optionalPayment.isEmpty() || optionalPayment.get().getBankName() == null) {
            return currentPrice;
        }

        PaymentInfo payment = optionalPayment.get();
        Optional<Discount> bankOffer = discountRepository
                .findBankOffer(payment.getBankName(), payment.getCardType());

        if (bankOffer.isPresent()) {
            BigDecimal discount = calculateDiscount(currentPrice, bankOffer.get());
            appliedDiscounts.put("Bank Offer - " + payment.getBankName(), discount);
            return currentPrice.subtract(discount);
        }

        return currentPrice;
    }

    private BigDecimal calculateDiscount(BigDecimal price, Discount discount) {
        if (discount.isPercentage()) {
            return price.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return discount.getValue();
    }

    private boolean isDiscountApplicable(Discount discount, CartItem item) {
        Product product = item.getProduct();

        // Check brand exclusions
        if (discount.getExcludedBrands() != null
            && discount.getExcludedBrands().contains(product.getBrand())) {
            return false;
        }

        // Check category exclusions
        if (discount.getExcludedCategories() != null
            && discount.getExcludedCategories()
                    .contains(product.getCategory())) {
            return false;
        }

        // Check applicable brands
        if (discount.getApplicableBrands() != null
            && !discount.getApplicableBrands().isEmpty()) {
            return discount.getApplicableBrands().contains(product.getBrand());
        }

        // Check applicable categories
        if (discount.getApplicableCategories() != null
            && !discount.getApplicableCategories().isEmpty()) {
            return discount.getApplicableCategories()
                    .contains(product.getCategory());
        }

        return true;
    }

    private String buildDiscountMessage(Map<String, BigDecimal> appliedDiscounts) {
        if (appliedDiscounts.isEmpty()) {
            return "No discounts applied";
        }

        StringBuilder message = new StringBuilder("Applied discounts: ");
        appliedDiscounts.forEach((key, value) ->
                message.append(key).append(" (₹")
                        .append(value.setScale(2, RoundingMode.HALF_UP))
                        .append("), ")
        );

        return message.substring(0, message.length() - 2);
    }
}