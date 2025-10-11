# E-Commerce Discount Service

## Overview

A Spring Boot service for calculating e-commerce discounts with support for brand-specific, category-specific, voucher,
and bank card offers.

## Discount Application Order

1. **Brand & Category Discounts** - Applied first to product base price
2. **Voucher Codes** - Applied to discounted price
3. **Bank Offers** - Applied last to final cart total

## Architecture

### Project Structure

```
src/
├── main/java/com/discount/
│   ├── controllers/     # REST controllers handling HTTP requests
│   ├── dto/             # Data Transfer Objects for request and response payloads
│   ├── model/           # Domain models representing entities and business objects
│   ├── service/         # Business logic and service layer implementations
│   ├── repository/      # Data access layer, including interfaces and in-memory/mock implementations
│   ├── exception/       # Custom exceptions and global exception handling
│   ├── utils/           # Utility and helper classes
├── test/java/com/discount/
│   ├── config/          # Test configuration and test data setup
│   └── service/         # Unit and integration tests for service layer
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

### Run Application Locally

```bash
mvn spring-boot:run
```

### Run Application via Docker

- Build the docker Image:
```bash
docker build -t discount-service .
```
- Run the container
```bash
docker run -p 8080:8080 discount-service
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Calculate Cart Discounts

**Endpoint**: `POST /api/v1/discounts/calculate`

**Description**: Calculate final price after applying all applicable discounts

**Request Body**:

```json
{
  "cartItems": [
    {
      "product": {
        "id": "PROD_001",
        "brand": "PUMA",
        "brandTier": "PREMIUM",
        "category": "T-Shirts",
        "basePrice": 1000
      },
      "quantity": 1,
      "size": "M"
    }
  ],
  "customer": {
    "id": "CUST_001",
    "tier": "GOLD"
  },
  "paymentInfo": {
    "method": "CARD",
    "bankName": "ICICI",
    "cardType": "CREDIT"
  },
  "voucherCode": "SUPER69"
}
```

**Response**:

```json
{
  "originalPrice": 1000.00,
  "finalPrice": 150.66,
  "appliedDiscounts": {
    "Brand Discount": 400.00,
    "Category Discount": 60.00,
    "Voucher - SUPER69": 372.60,
    "Bank Offer - ICICI": 16.74
  },
  "message": "Applied discounts: Brand Discount (₹400.00), Category Discount (₹60.00), Voucher - SUPER69 (₹372.60), Bank Offer - ICICI (₹16.74)"
}
```

### 2. Validate Discount Code

**Endpoint**: `POST /api/v1/discounts/validate`

**Description**: Check if a discount code can be applied to the cart

**Request Body**:

```json
{
  "code": "PUMA40",
  "cartItems": [
    {
      "product": {
        "id": "PROD_001",
        "brand": "PUMA",
        "category": "T-Shirts",
        "basePrice": 1000
      },
      "quantity": 1,
      "size": "M"
    }
  ],
  "customer": {
    "id": "CUST_001",
    "tier": "GOLD"
  }
}
```

**Response**:

```json
{
  "code": "PUMA40",
  "valid": true,
  "message": "Discount code is valid"
}
```

### Error Responses

**400 Bad Request** (Validation/Calculation Error):

```json
{
  "timestamp": "2025-10-11T10:30:00",
  "status": 400,
  "error": "Discount Calculation Error",
  "message": "Cart items cannot be empty"
}
```

**500 Internal Server Error**:

```json
{
  "timestamp": "2025-10-11T10:30:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Example cURL Commands

### Calculate Discounts

```bash
curl -X POST http://localhost:8080/api/v1/discounts/calculate \\
-H "Content-Type: application/json" \\
-d '{
  "cartItems": [
    {
      "product": {
        "id": "PROD_001",
        "brand": "PUMA",
        "brandTier": "PREMIUM",
        "category": "T-Shirts",
        "basePrice": 1000
      },
      "quantity": 1,
      "size": "M"
    }
  ],
  "customer": {
    "id": "CUST_001",
    "tier": "GOLD"
  },
  "paymentInfo": {
    "method": "CARD",
    "bankName": "ICICI",
    "cardType": "CREDIT"
  },
  "voucherCode": "SUPER69"
}'
```

### Validate Discount Code

```bash
curl -X POST http://localhost:8080/api/v1/discounts/validate \\
-H "Content-Type: application/json" \\
-d '{
  "code": "PUMA40",
  "cartItems": [
    {
      "product": {
        "id": "PROD_001",
        "brand": "PUMA",
        "category": "T-Shirts",
        "basePrice": 1000
      },
      "quantity": 1,
      "size": "M"
    }
  ],
  "customer": {
    "id": "CUST_001",
    "tier": "GOLD"
  }
}'
```

## Test Scenario

The test data includes a PUMA T-shirt (₹1000) with:

- 40% brand discount (PUMA)
- 10% category discount (T-Shirts)
- 10% ICICI bank offer

**Expected Calculation:**

- Base Price: ₹1000
- After Brand Discount (40%): ₹600
- After Category Discount (10% of ₹600): ₹540
- After Bank Offer (10% of ₹540): ₹486
- **Final Price: ₹486**

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

### Discount Logic

1. **Discount Stacking**: All applicable discounts stack multiplicatively (not additively)
    - Example: 40% + 10% = 46% total discount, not 50%
    - Calculation: ₹1000 → ₹600 (40% off) → ₹540 (10% of ₹600), not ₹500

2. **Discount Application Order**: Fixed sequence as per requirements
    - First: Brand discounts
    - Second: Category discounts
    - Third: Voucher codes
    - Fourth: Bank offers
    - Each discount applies to the already-discounted price from previous step

3. **Product-Level vs Cart-Level Discounts**:
    - Brand and category discounts: Applied at product level (per item)
    - Voucher codes: Applied at cart level (total after brand/category discounts)
    - Bank offers: Applied at cart level (final total)

### Voucher Codes

1. **Voucher Code Input**: Voucher code is passed as a separate string parameter
    - Applied at checkout time
    - Can be null (no voucher applied)

2. **Voucher Validation**: Voucher codes must be validated before application
    - Check if code exists in repository
    - Check brand exclusions
    - Check category restrictions
    - Check customer tier requirements

### Payment & Bank Offers

1. **Payment Info Optional**: Payment information is optional
    - If not provided, no bank offers applied
    - Bank offers require both bankName and cardType

### Product & Pricing

1. **Product Immutability**: Product objects should not be mutated during discount calculation
    - `currentPrice` field is removed
    - All calculations done on-the-fly without storing intermediate state

2. **Price Precision**: All monetary calculations use BigDecimal
    - Rounding mode: HALF_UP (standard rounding)
    - Scale: 2 decimal places for final prices

3. **Base Price**: Product.basePrice is the original price before any discounts
    - Never changes during calculation
    - Used as starting point for all discount calculations

### Data & Storage

1. **In-Memory Repository**: Test data stored in memory
    - No database persistence
    - Data lost on application restart

### Error Handling

1. **Exception Strategy**:
    - `DiscountCalculationException`: For calculation errors (empty cart, null customer, etc.)
    - `DiscountValidationException`: For invalid/inapplicable voucher codes
    - Both are RuntimeExceptions (unchecked)

2. **Validation Rules**:
    - Empty cart throws exception
    - Null customer throws exception
    - Invalid voucher code throws exception during calculation
    - Missing payment info silently skips bank offers (no exception)

### Testing

1. **Test Scenario**: Based on assignment requirements
    - PUMA T-shirt: ₹1000 base price
    - 40% PUMA brand discount
    - 10% T-Shirts category discount
    - 69% SUPER69 voucher
    - 10% ICICI bank offer
    - Expected final price: ₹150.66

2. **Test Coverage**: Unit tests use mocks, integration tests use real beans
    - Unit tests: Test service logic with mocked repository
    - Integration tests: Test complete Spring Boot context with test data