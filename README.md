# E-Commerce Discount Service

## Overview
A Spring Boot service for calculating e-commerce discounts with support for brand-specific, category-specific, voucher, and bank card offers.

## Discount Application Order
1. **Brand & Category Discounts** - Applied first to product base price
2. **Voucher Codes** - Applied to discounted price
3. **Bank Offers** - Applied last to final cart total

## Architecture

### Project Structure
```
src/
├── main/java/com/com.discount/
│   ├── model/           # Domain models
│   ├── service/         # Business logic
│   ├── repository/      # Data access layer
│   ├── exception/       # Custom exceptions
│   └── config/          # Configuration classes
└── test/                # Unit and integration tests
```

## Technical Decisions

### 1. Discount Application Strategy
- Brand and category discounts update the product's \`currentPrice\`
- Subsequent discounts apply to the updated price
- Each com.discount type is applied sequentially to maintain order

### 2. Repository Pattern
- Used in-memory repository for simplicity
- Can be easily swapped with JPA/database implementation
- Interface allows for easy testing with mocks

### 3. BigDecimal for Monetary Values
- Ensures precision in financial calculations
- Uses HALF_UP rounding mode for standard rounding

### 4. Exception Handling
- Custom exceptions for domain-specific errors
- Clear error messages for debugging

## Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build

```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

## Test Scenario
The test data includes a PUMA T-shirt (₹1000) with:
- 40% brand com.discount (PUMA)
- 10% category com.discount (T-Shirts)
- 10% ICICI bank offer

**Expected Calculation:**
- Base Price: ₹1000
- After Brand Discount (40%): ₹600
- After Category Discount (10% of ₹600): ₹540
- After Bank Offer (10% of ₹540): ₹486
- **Final Price: ₹486**

## Assumptions

1. **Discount Stacking**: All applicable discounts stack multiplicatively
2. **Brand/Category Order**: Brand discounts apply before category discounts
3. **Voucher Codes**: Not fully implemented in initial version (placeholder exists)
4. **Customer Tiers**: Used for validation but not for com.discount calculation
5. **Thread Safety**: Current implementation is not thread-safe
6. **Data Persistence**: Using in-memory storage for simplicity

## API Usage

### Calculate Cart Discounts
```java
List<CartItem> cartItems = createCartItems();
CustomerProfile customer = createCustomer();
Optional<PaymentInfo> payment = Optional.of(createPaymentInfo());

DiscountedPrice result = discountService.calculateCartDiscounts(
    cartItems,
    customer,
    payment
);
```

### Validate Discount Code
```java
boolean isValid = discountService.validateDiscountCode(
    "PUMA40",
    cartItems,
    customer
);
```