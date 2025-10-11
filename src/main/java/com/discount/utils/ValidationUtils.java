package com.discount.utils;

import com.discount.exception.DiscountValidationException;
import com.discount.model.CartItem;
import com.discount.model.CustomerProfile;
import com.discount.model.Discount;
import com.discount.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValidationUtils {

    public void validateInputs(List<CartItem> cartItems, CustomerProfile customer) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new DiscountValidationException("Cart items cannot be empty");
        }
        if (customer == null) {
            throw new DiscountValidationException("Customer profile is required");
        }
    }

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
