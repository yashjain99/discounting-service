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

/**
 * Service implementation for handling discount-related operations.
 * Provides methods to calculate cart discounts and validate discount codes.
 * Uses an underlying DiscountRepository to retrieve discount data for brands, categories, vouchers, and bank offers.
 * Tracks and applies multiple types of discounts to calculate final prices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public DiscountedPrice calculateCartDiscounts(
            List<CartItem> cartItems,
            CustomerProfile customer,
            PaymentInfo paymentInfo,
            String voucherCode
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
                    voucherCode, cartItems, customer, priceAfterBrandCategory, appliedDiscounts
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

    /**
     * Calculates the total original price of all items in the cart before any discounts.
     *
     * @param cartItems the list of cart items to calculate the total price for
     * @return the total original price as a BigDecimal
     */
    private BigDecimal calculateOriginalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getBasePrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Applies brand and category discounts to a list of cart items and calculates the total price after discounts.
     * Tracks the total brand and category discounts applied in the provided map.
     *
     * @param cartItems        the list of cart items to apply discounts to
     * @param appliedDiscounts a map to track the total discounts applied, keyed by discount type
     * @return the total price of the cart items after applying brand and category discounts
     */
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

    /**
     * Applies a discount to the given price if the discount is present.
     * If the discount is null, returns the original price with zero discount.
     *
     * @param currentPrice the current price to apply the discount to
     * @param discount     the discount to apply, may be null
     * @return a DiscountCalculationResult containing the new price after discount and the discount amount applied
     */
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

    /**
     * Apply voucher discount if code is provided and valid
     */
    private BigDecimal applyVoucherDiscounts(
            String code,
            List<CartItem> cartItems,
            CustomerProfile customer,
            BigDecimal currentPrice,
            Map<String, BigDecimal> appliedDiscounts
    ) {
        if (code == null || code.isBlank()) {
            return currentPrice;
        }

        // Validate the voucher code
        if (!validateDiscountCode(code, cartItems, customer)) {
            log.warn("Invalid voucher code: {}", code);
            throw new DiscountValidationException("Invalid or inapplicable voucher code: " + code);
        }

        // Get voucher discount details
        Optional<Discount> voucher = discountRepository.findByCode(code);
        if (voucher.isEmpty()) {
            return currentPrice;
        }

        Discount voucherDiscount = voucher.get();

        // Check if voucher is applicable to cart items
        boolean applicable = cartItems.stream()
                .anyMatch(item -> new ValidationUtils().isDiscountApplicable(voucherDiscount, item));

        if (!applicable) {
            throw new DiscountValidationException(
                    "Voucher code '" + code + "' is not applicable to items in cart"
            );
        }

        // Calculate voucher discount
        BigDecimal discountAmount = calculateDiscount(currentPrice, voucherDiscount);
        appliedDiscounts.put("Voucher - " + code, discountAmount);

        return currentPrice.subtract(discountAmount);
    }

    /**
     * Applies a bank-specific offer to the current price if the payment information and offer are available.
     * Tracks the applied bank discount in the provided map.
     *
     * @param paymentInfo      the payment information containing bank and card details, may be null
     * @param currentPrice     the current price before applying the bank offer
     * @param appliedDiscounts a map to track the total discounts applied, keyed by discount type
     * @return the price after applying the bank offer, or the original price if no offer is applicable
     */
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

    /**
     * Calculates the discount amount for a given price based on the discount details.
     *
     * @param price    the original price to calculate the discount on
     * @param discount the discount to apply
     * @return the discount amount as a BigDecimal
     */
    private BigDecimal calculateDiscount(BigDecimal price, Discount discount) {
        if (discount.isPercentage()) {
            return price.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return discount.getValue();
    }

    /**
     * Builds a descriptive message summarizing the discounts applied to a cart.
     *
     * @param appliedDiscounts a map of discount types and their corresponding amounts
     * @return a formatted string describing the applied discounts, or a message indicating no discounts were applied
     */
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