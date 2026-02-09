package com.example.genggamin.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests for PlafondRequest DTO
 *
 * <p>Best Practices yang diikuti:
 *
 * <ul>
 *   <li>Test DTO validation logic
 *   <li>AAA Pattern (Arrange, Act, Assert)
 *   <li>Satu skenario per metode @Test
 *   <li>Nested class untuk mengelompokkan test berdasarkan field
 *   <li>Tidak memerlukan Mock karena ini pure unit test
 * </ul>
 */
@DisplayName("PlafondRequest DTO Validation Tests")
class PlafondRequestTest {

  private PlafondRequest request;

  @BeforeEach
  void setUp() {
    // Arrange: Create a valid default request
    request =
        PlafondRequest.builder()
            .title("Gold Loan")
            .minIncome(new BigDecimal("5000000"))
            .maxAmount(new BigDecimal("50000000"))
            .tenorMonth(12L)
            .interestRate(new BigDecimal("1.5"))
            .isActive(true)
            .build();
  }

  // =========================================================================
  // Tests for title validation
  // =========================================================================
  @Nested
  @DisplayName("Title Validation")
  class TitleValidationTests {

    @Test
    @DisplayName("should pass validation when title is valid")
    void shouldPassValidation_whenTitleIsValid() {
      // Act & Assert - No exception should be thrown
      request.validate();
      assertThat(request.getTitle()).isEqualTo("Gold Loan");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when title is null")
    void shouldThrowException_whenTitleIsNull() {
      // Arrange
      request.setTitle(null);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Title cannot be empty");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when title is empty")
    void shouldThrowException_whenTitleIsEmpty() {
      // Arrange
      request.setTitle("");

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Title cannot be empty");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when title is whitespace only")
    void shouldThrowException_whenTitleIsWhitespaceOnly() {
      // Arrange
      request.setTitle("   ");

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Title cannot be empty");
    }
  }

  // =========================================================================
  // Tests for minIncome validation
  // =========================================================================
  @Nested
  @DisplayName("MinIncome Validation")
  class MinIncomeValidationTests {

    @Test
    @DisplayName("should pass validation when minIncome is positive")
    void shouldPassValidation_whenMinIncomeIsPositive() {
      // Arrange - already positive in setUp

      // Act & Assert - No exception should be thrown
      request.validate();
      assertThat(request.getMinIncome()).isPositive();
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when minIncome is null")
    void shouldThrowException_whenMinIncomeIsNull() {
      // Arrange
      request.setMinIncome(null);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Min income must be greater than 0");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when minIncome is zero")
    void shouldThrowException_whenMinIncomeIsZero() {
      // Arrange
      request.setMinIncome(BigDecimal.ZERO);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Min income must be greater than 0");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when minIncome is negative")
    void shouldThrowException_whenMinIncomeIsNegative() {
      // Arrange
      request.setMinIncome(new BigDecimal("-1000000"));

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Min income must be greater than 0");
    }
  }

  // =========================================================================
  // Tests for maxAmount validation
  // =========================================================================
  @Nested
  @DisplayName("MaxAmount Validation")
  class MaxAmountValidationTests {

    @Test
    @DisplayName("should throw IllegalArgumentException when maxAmount is null")
    void shouldThrowException_whenMaxAmountIsNull() {
      // Arrange
      request.setMaxAmount(null);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Max amount must be greater than 0");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when maxAmount is zero")
    void shouldThrowException_whenMaxAmountIsZero() {
      // Arrange
      request.setMaxAmount(BigDecimal.ZERO);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Max amount must be greater than 0");
    }
  }

  // =========================================================================
  // Tests for tenorMonth validation
  // =========================================================================
  @Nested
  @DisplayName("TenorMonth Validation")
  class TenorMonthValidationTests {

    @Test
    @DisplayName("should pass validation when tenorMonth is within valid range")
    void shouldPassValidation_whenTenorMonthIsValid() {
      // Arrange
      request.setTenorMonth(60L); // 5 years

      // Act & Assert - No exception should be thrown
      request.validate();
      assertThat(request.getTenorMonth()).isEqualTo(60L);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when tenorMonth is null")
    void shouldThrowException_whenTenorMonthIsNull() {
      // Arrange
      request.setTenorMonth(null);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Tenor month must be greater than 0");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when tenorMonth is zero")
    void shouldThrowException_whenTenorMonthIsZero() {
      // Arrange
      request.setTenorMonth(0L);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Tenor month must be greater than 0");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when tenorMonth exceeds 360")
    void shouldThrowException_whenTenorMonthExceedsMax() {
      // Arrange
      request.setTenorMonth(361L); // More than 30 years

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Tenor month cannot exceed 360 months (30 years)");
    }

    @Test
    @DisplayName("should pass validation when tenorMonth is exactly 360")
    void shouldPassValidation_whenTenorMonthIsExactly360() {
      // Arrange
      request.setTenorMonth(360L);

      // Act & Assert - No exception should be thrown
      request.validate();
    }
  }

  // =========================================================================
  // Tests for interestRate validation
  // =========================================================================
  @Nested
  @DisplayName("InterestRate Validation")
  class InterestRateValidationTests {

    @Test
    @DisplayName("should pass validation when interestRate is zero")
    void shouldPassValidation_whenInterestRateIsZero() {
      // Arrange
      request.setInterestRate(BigDecimal.ZERO);

      // Act & Assert - No exception should be thrown
      request.validate();
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when interestRate is null")
    void shouldThrowException_whenInterestRateIsNull() {
      // Arrange
      request.setInterestRate(null);

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Interest rate must be 0 or greater");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when interestRate is negative")
    void shouldThrowException_whenInterestRateIsNegative() {
      // Arrange
      request.setInterestRate(new BigDecimal("-0.5"));

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Interest rate must be 0 or greater");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when interestRate exceeds 100")
    void shouldThrowException_whenInterestRateExceeds100() {
      // Arrange
      request.setInterestRate(new BigDecimal("100.01"));

      // Act & Assert
      assertThatThrownBy(() -> request.validate())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Interest rate cannot exceed 100%");
    }

    @Test
    @DisplayName("should pass validation when interestRate is exactly 100")
    void shouldPassValidation_whenInterestRateIsExactly100() {
      // Arrange
      request.setInterestRate(new BigDecimal("100"));

      // Act & Assert - No exception should be thrown
      request.validate();
    }
  }
}
