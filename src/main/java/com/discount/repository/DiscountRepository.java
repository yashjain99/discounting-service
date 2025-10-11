package com.discount.repository;

import com.discount.model.Discount;

import java.util.Optional;

/**
 * Repository interface for discount data access.
 */
public interface DiscountRepository {

    Optional<Discount> findByCode(String code);

    Optional<Discount> findBrandDiscount(String brand);

    Optional<Discount> findCategoryDiscount(String category);

    Optional<Discount> findBankOffer(String bankName, String cardType);
}