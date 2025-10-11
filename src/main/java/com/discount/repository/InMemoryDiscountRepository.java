package com.discount.repository;

import com.discount.model.Discount;
import com.discount.model.enums.DiscountType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * In-memory implementation of DiscountRepository.
 * Stores and retrieves discount data using internal maps for codes, brands, categories, and bank offers.
 */
@Repository
public class InMemoryDiscountRepository implements DiscountRepository {

    private final Map<String, Discount> discountsByCode = new HashMap<>();
    private final Map<String, Discount> brandDiscounts = new HashMap<>();
    private final Map<String, Discount> categoryDiscounts = new HashMap<>();
    private final Map<String, Discount> bankOffers = new HashMap<>();

    /**
     * Initializes the repository with sample discount data.
     */
    public InMemoryDiscountRepository() {
        initializeDiscounts();
    }

    /**
     * Populates the in-memory maps with predefined discounts for brands, categories, banks, and vouchers.
     */
    private void initializeDiscounts() {
        // Brand discount: PUMA 40% off
        Discount pumaDiscount = Discount.builder()
                .id("BRAND_PUMA")
                .code("PUMA40")
                .type(DiscountType.BRAND)
                .value(BigDecimal.valueOf(40))
                .isPercentage(true)
                .applicableBrands(Set.of("PUMA"))
                .build();
        brandDiscounts.put("PUMA", pumaDiscount);
        discountsByCode.put("PUMA40", pumaDiscount);

        // Category discount: T-shirts 10% off
        Discount tshirtDiscount = Discount.builder()
                .id("CAT_TSHIRT")
                .code("TSHIRT10")
                .type(DiscountType.CATEGORY)
                .value(BigDecimal.valueOf(10))
                .isPercentage(true)
                .applicableCategories(Set.of("T-Shirts"))
                .build();
        categoryDiscounts.put("T-Shirts", tshirtDiscount);
        discountsByCode.put("TSHIRT10", tshirtDiscount);

        // Bank offer: ICICI 10% instant discount
        Discount iciciOffer = Discount.builder()
                .id("BANK_ICICI")
                .code("ICICI10")
                .type(DiscountType.BANK_OFFER)
                .value(BigDecimal.valueOf(10))
                .isPercentage(true)
                .bankName("ICICI")
                .build();
        bankOffers.put("ICICI", iciciOffer);
        discountsByCode.put("ICICI10", iciciOffer);

        // Voucher: SUPER69
        Discount superVoucher = Discount.builder()
                .id("VOUCHER_SUPER69")
                .code("SUPER69")
                .type(DiscountType.VOUCHER)
                .value(BigDecimal.valueOf(69))
                .isPercentage(true)
                .build();
        discountsByCode.put("SUPER69", superVoucher);
    }

    @Override
    public Optional<Discount> findByCode(String code) {
        return Optional.ofNullable(discountsByCode.get(code));
    }

    @Override
    public Optional<Discount> findBrandDiscount(String brand) {
        return Optional.ofNullable(brandDiscounts.get(brand));
    }

    @Override
    public Optional<Discount> findCategoryDiscount(String category) {
        return Optional.ofNullable(categoryDiscounts.get(category));
    }

    @Override
    public Optional<Discount> findBankOffer(String bankName, String cardType) {
        return Optional.ofNullable(bankOffers.get(bankName));
    }
}