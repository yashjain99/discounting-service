package com.discount.utils;

import com.discount.exception.DiscountValidationException;
import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.Discount;
import com.discount.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for validating inputs and discount applicability.
 * Provides methods to validate cart and customer data and to check if a discount applies to a specific cart item.
 */
@Component
public class ValidationUtils {

    /**
     * Validates the provided cart items and customer profile.
     * Throws a DiscountValidationException if the cart is empty or the customer profile is null.
     *
     * @param cartItems the list of cart items to validate
     * @param customer  the customer profile to validate
     * @throws DiscountValidationException if cartItems is null/empty or customer is null
     */
    public void validateInputs(List<CartItem> cartItems, CustomerProfile customer) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new DiscountValidationException("Cart items cannot be empty");
        }
        if (customer == null) {
            throw new DiscountValidationException("Customer profile is required");
        }
    }

    /**
     * Checks whether a discount is applicable to a given cart item based on product brand and category.
     * Considers both exclusions and explicit applicability rules in the discount.
     *
     * @param discount the discount to check
     * @param item     the cart item to validate against the discount
     * @return true if the discount can be applied to the cart item, false otherwise
     */
    public boolean isDiscountApplicable(Discount discount, CartItem item) {
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
}
