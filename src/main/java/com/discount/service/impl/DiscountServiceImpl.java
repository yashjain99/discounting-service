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
import com.discount.utils.ValidationUtils;
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
            new ValidationUtils().validateInputs(cartItems, customer);

            BigDecimal originalPrice = calculateOriginalPrice(cartItems);
            Map<String, BigDecimal> appliedDiscounts = new LinkedHashMap<>();

            // Step 1: Apply brand and category discounts
            BigDecimal priceAfterBrandCategory = applyBrandAndCategoryDiscounts(
                    cartItems, appliedDiscounts
            );

            // Step 2: Apply voucher codes
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
                    .anyMatch(item -> new ValidationUtils().isDiscountApplicable(discount, item));

        } catch (DiscountValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating discount code", e);
            throw new DiscountValidationException(
                    "Failed to validate discount code: " + e.getMessage(), e
            );
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
            int quantity = item.getQuantity();

            // Start with base price
            BigDecimal currentPrice = product.getBasePrice();

            Discount brandDiscount = discountRepository.findBrandDiscount(product.getBrand()).isPresent()
                    ? discountRepository.findBrandDiscount(product.getBrand()).get()
                    : null;

            // Apply brand discount
            DiscountCalculationResult brandResult = applyDiscountIfPresent(
                    currentPrice,
                    brandDiscount
            );
            currentPrice = brandResult.discountedPrice();
            totalBrandDiscount = totalBrandDiscount.add(
                    brandResult.discountAmount().multiply(BigDecimal.valueOf(quantity))
            );

            Discount categoryDiscount = discountRepository.findCategoryDiscount(product.getCategory()).isPresent()
                    ? discountRepository.findCategoryDiscount(product.getCategory()).get()
                    : null;

            // Apply category discount
            DiscountCalculationResult categoryResult = applyDiscountIfPresent(
                    currentPrice,
                    categoryDiscount
            );
            currentPrice = categoryResult.discountedPrice();
            totalCategoryDiscount = totalCategoryDiscount.add(
                    categoryResult.discountAmount().multiply(BigDecimal.valueOf(quantity))
            );

            // Add to total
            totalPrice = totalPrice.add(
                    currentPrice.multiply(BigDecimal.valueOf(quantity))
            );
        }

        // Track applied discounts
        addDiscountIfNonZero(appliedDiscounts, "Brand Discount", totalBrandDiscount);
        addDiscountIfNonZero(appliedDiscounts, "Category Discount", totalCategoryDiscount);

        return totalPrice;
    }

    /**
     * Add discount to map only if amount is greater than zero
     */
    private void addDiscountIfNonZero(
            Map<String, BigDecimal> discounts,
            String name,
            BigDecimal amount
    ) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            discounts.put(name, amount);
        }
    }

    private DiscountCalculationResult applyDiscountIfPresent(
            BigDecimal currentPrice,
            Discount discount
    ) {

        // Handle nullable paymentInfo
        Optional<Discount> optionalDiscount = Optional.ofNullable(discount);

        if (optionalDiscount.isEmpty()) {
            return new DiscountCalculationResult(currentPrice, BigDecimal.ZERO);
        }

        BigDecimal discountAmount = calculateDiscount(currentPrice, optionalDiscount.get());
        BigDecimal newPrice = currentPrice.subtract(discountAmount);

        return new DiscountCalculationResult(newPrice, discountAmount);
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

    /**
     * Internal record to hold discount calculation results
     */
    private record DiscountCalculationResult(
            BigDecimal discountedPrice,
            BigDecimal discountAmount
    ) {
    }
}