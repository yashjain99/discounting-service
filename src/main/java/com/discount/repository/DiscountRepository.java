package com.discount.repository;

import com.discount.model.Discount;

import java.util.Optional;

/**
 * Repository interface for discount data access.
 */
public interface DiscountRepository {

    /**
     * Finds a discount by its unique code.
     *
     * @param code the discount code to search for
     * @return an Optional containing the Discount if found, otherwise empty
     */
    Optional<Discount> findByCode(String code);

    /**
     * Finds a discount applicable to a specific brand.
     *
     * @param brand the brand name to search discounts for
     * @return an Optional containing the Discount if found, otherwise empty
     */
    Optional<Discount> findBrandDiscount(String brand);

    /**
     * Finds a discount applicable to a specific product category.
     *
     * @param category the category name to search discounts for
     * @return an Optional containing the Discount if found, otherwise empty
     */
    Optional<Discount> findCategoryDiscount(String category);

    /**
     * Finds a discount applicable for a specific bank and card type combination.
     *
     * @param bankName the name of the bank
     * @param cardType the type of card (e.g., credit, debit)
     * @return an Optional containing the Discount if found, otherwise empty
     */
    Optional<Discount> findBankOffer(String bankName, String cardType);
}