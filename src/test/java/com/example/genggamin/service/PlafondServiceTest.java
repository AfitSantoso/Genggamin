package com.example.genggamin.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.genggamin.dto.LoanSimulationRequest;
import com.example.genggamin.dto.LoanSimulationResponse;
import com.example.genggamin.dto.PlafondRequest;
import com.example.genggamin.dto.PlafondResponse;
import com.example.genggamin.entity.Plafond;
import com.example.genggamin.repository.PlafondRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit Tests for PlafondService
 *
 * <p>Best Practices yang diikuti:
 *
 * <ul>
 *   <li>Menggunakan Mockito (bukan @SpringBootTest)
 *   <li>AAA Pattern (Arrange, Act, Assert)
 *   <li>Satu skenario per metode test
 *   <li>Menggunakan AssertJ fluent assertions
 *   <li>Nested class untuk grouping test scenarios
 *   <li>Mock untuk Repository dependencies
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlafondService Unit Tests")
class PlafondServiceTest {

  @Mock private PlafondRepository plafondRepository;

  @InjectMocks private PlafondService plafondService;

  private Plafond testPlafond;
  private PlafondRequest testPlafondRequest;

  @BeforeEach
  void setUp() {
    // Arrange: Setup common test data
    testPlafond =
        Plafond.builder()
            .id(1L)
            .title("Gold Loan")
            .minIncome(new BigDecimal("5000000"))
            .maxAmount(new BigDecimal("50000000"))
            .tenorMonth(12L)
            .interestRate(new BigDecimal("1.5"))
            .isActive(true)
            .isDeleted(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    testPlafondRequest =
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
  // Tests for getAllPlafonds()
  // =========================================================================
  @Nested
  @DisplayName("getAllPlafonds()")
  class GetAllPlafondsTests {

    @Test
    @DisplayName("should return list of PlafondResponse when plafonds exist")
    void shouldReturnPlafondList_whenPlafondsExist() {
      // Arrange
      given(plafondRepository.findAllActive()).willReturn(List.of(testPlafond));

      // Act
      List<PlafondResponse> result = plafondService.getAllPlafonds();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getTitle()).isEqualTo("Gold Loan");
      assertThat(result.get(0).getMaxAmount()).isEqualByComparingTo(new BigDecimal("50000000"));
      verify(plafondRepository, times(1)).findAllActive();
    }

    @Test
    @DisplayName("should return empty list when no plafonds exist")
    void shouldReturnEmptyList_whenNoPlafondsExist() {
      // Arrange
      given(plafondRepository.findAllActive()).willReturn(List.of());

      // Act
      List<PlafondResponse> result = plafondService.getAllPlafonds();

      // Assert
      assertThat(result).isEmpty();
    }
  }

  // =========================================================================
  // Tests for getActivePlafonds()
  // =========================================================================
  @Nested
  @DisplayName("getActivePlafonds()")
  class GetActivePlafondsTests {

    @Test
    @DisplayName("should return only active plafonds")
    void shouldReturnOnlyActivePlafonds_whenCalled() {
      // Arrange
      given(plafondRepository.findAllActiveAndNotDeleted()).willReturn(List.of(testPlafond));

      // Act
      List<PlafondResponse> result = plafondService.getActivePlafonds();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getIsActive()).isTrue();
      verify(plafondRepository).findAllActiveAndNotDeleted();
    }
  }

  // =========================================================================
  // Tests for getPlafondById()
  // =========================================================================
  @Nested
  @DisplayName("getPlafondById()")
  class GetPlafondByIdTests {

    @Test
    @DisplayName("should return PlafondResponse when plafond exists")
    void shouldReturnPlafond_whenIdExists() {
      // Arrange
      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));

      // Act
      PlafondResponse result = plafondService.getPlafondById(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getTitle()).isEqualTo("Gold Loan");
    }

    @Test
    @DisplayName("should throw RuntimeException when plafond not found")
    void shouldThrowException_whenPlafondNotFound() {
      // Arrange
      given(plafondRepository.findByIdActive(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> plafondService.getPlafondById(999L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Plafond not found");
    }
  }

  // =========================================================================
  // Tests for getPlafondsByIncome()
  // =========================================================================
  @Nested
  @DisplayName("getPlafondsByIncome()")
  class GetPlafondsByIncomeTests {

    @Test
    @DisplayName("should return matching plafonds for given income")
    void shouldReturnMatchingPlafonds_whenIncomeProvided() {
      // Arrange
      BigDecimal income = new BigDecimal("10000000");
      given(plafondRepository.findByIncomeRange(income)).willReturn(List.of(testPlafond));

      // Act
      List<PlafondResponse> result = plafondService.getPlafondsByIncome(income);

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getMinIncome()).isLessThanOrEqualTo(income);
      verify(plafondRepository).findByIncomeRange(income);
    }
  }

  // =========================================================================
  // Tests for simulateLoan()
  // =========================================================================
  @Nested
  @DisplayName("simulateLoan()")
  class SimulateLoanTests {

    @Test
    @DisplayName("should calculate loan simulation with plafondId")
    void shouldCalculateSimulation_whenPlafondIdProvided() {
      // Arrange
      LoanSimulationRequest request =
          LoanSimulationRequest.builder()
              .plafondId(1L)
              .amount(new BigDecimal("10000000"))
              .tenor(12L)
              .build();

      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));

      // Act
      LoanSimulationResponse result = plafondService.simulateLoan(request);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getLoanAmount()).isEqualByComparingTo(new BigDecimal("10000000"));
      assertThat(result.getTenorMonth()).isEqualTo(12);
      assertThat(result.getPlafondId()).isEqualTo(1L);
      // Validate calculation: Total Interest = 10000000 * 0.015 * 12 = 1800000
      assertThat(result.getTotalInterest()).isEqualByComparingTo(new BigDecimal("1800000.00"));
      // Total Payment = 10000000 + 1800000 = 11800000
      assertThat(result.getTotalPayment()).isEqualByComparingTo(new BigDecimal("11800000.00"));
      // Monthly Installment = 11800000 / 12 = 983333.33
      assertThat(result.getMonthlyInstallment()).isEqualByComparingTo(new BigDecimal("983333.33"));
    }

    @Test
    @DisplayName("should calculate loan simulation by tenor when no plafondId")
    void shouldFindMatchingPlafond_whenTenorProvided() {
      // Arrange
      LoanSimulationRequest request =
          LoanSimulationRequest.builder().amount(new BigDecimal("10000000")).tenor(12L).build();

      given(plafondRepository.findByTenor(12L)).willReturn(List.of(testPlafond));

      // Act
      LoanSimulationResponse result = plafondService.simulateLoan(request);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getTenorMonth()).isEqualTo(12L);
      verify(plafondRepository).findByTenor(12L);
    }

    @Test
    @DisplayName("should throw exception when tenor exceeds plafond max tenor")
    void shouldThrowException_whenTenorExceedsMaxTenor() {
      // Arrange
      LoanSimulationRequest request =
          LoanSimulationRequest.builder()
              .plafondId(1L)
              .amount(new BigDecimal("10000000"))
              .tenor(24L) // Exceeds plafond's 12 months
              .build();

      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));

      // Act & Assert
      assertThatThrownBy(() -> plafondService.simulateLoan(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("exceeds maximum tenor");
    }

    @Test
    @DisplayName("should throw exception when amount exceeds max amount")
    void shouldThrowException_whenAmountExceedsMaxAmount() {
      // Arrange
      LoanSimulationRequest request =
          LoanSimulationRequest.builder()
              .plafondId(1L)
              .amount(new BigDecimal("100000000")) // Exceeds plafond's 50M max
              .tenor(12L)
              .build();

      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));

      // Act & Assert
      assertThatThrownBy(() -> plafondService.simulateLoan(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("exceeds the maximum limit");
    }

    @Test
    @DisplayName("should throw exception when no suitable plafond found")
    void shouldThrowException_whenNoSuitablePlafondFound() {
      // Arrange
      LoanSimulationRequest request =
          LoanSimulationRequest.builder().amount(new BigDecimal("100000000")).tenor(12L).build();

      given(plafondRepository.findByTenor(12L)).willReturn(List.of(testPlafond));

      // Act & Assert
      assertThatThrownBy(() -> plafondService.simulateLoan(request))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("No suitable plafond found");
    }
  }

  // =========================================================================
  // Tests for createPlafond()
  // =========================================================================
  @Nested
  @DisplayName("createPlafond()")
  class CreatePlafondTests {

    @Test
    @DisplayName("should create plafond when valid request provided")
    void shouldCreatePlafond_whenValidRequest() {
      // Arrange
      given(
              plafondRepository.existsByMinIncomeAndMaxAmountAndTenor(
                  any(BigDecimal.class), any(BigDecimal.class), any(Long.class)))
          .willReturn(false);
      given(plafondRepository.saveAndFlush(any(Plafond.class))).willReturn(testPlafond);

      // Act
      PlafondResponse result = plafondService.createPlafond(testPlafondRequest);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getTitle()).isEqualTo("Gold Loan");
      verify(plafondRepository).saveAndFlush(any(Plafond.class));
    }

    @Test
    @DisplayName("should throw exception when duplicate plafond exists")
    void shouldThrowException_whenDuplicatePlafondExists() {
      // Arrange
      given(
              plafondRepository.existsByMinIncomeAndMaxAmountAndTenor(
                  any(BigDecimal.class), any(BigDecimal.class), any(Long.class)))
          .willReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> plafondService.createPlafond(testPlafondRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when title is empty")
    void shouldThrowException_whenTitleIsEmpty() {
      // Arrange
      testPlafondRequest.setTitle("");

      // Act & Assert
      assertThatThrownBy(() -> plafondService.createPlafond(testPlafondRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Title cannot be empty");
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when minIncome is zero or negative")
    void shouldThrowException_whenMinIncomeIsInvalid() {
      // Arrange
      testPlafondRequest.setMinIncome(BigDecimal.ZERO);

      // Act & Assert
      assertThatThrownBy(() -> plafondService.createPlafond(testPlafondRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Min income must be greater than 0");
    }
  }

  // =========================================================================
  // Tests for updatePlafond()
  // =========================================================================
  @Nested
  @DisplayName("updatePlafond()")
  class UpdatePlafondTests {

    @Test
    @DisplayName("should update plafond when valid request provided")
    void shouldUpdatePlafond_whenValidRequest() {
      // Arrange
      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));
      given(
              plafondRepository.existsByMinIncomeAndMaxAmountAndTenorAndIdNot(
                  any(BigDecimal.class), any(BigDecimal.class), any(Long.class), any(Long.class)))
          .willReturn(false);
      given(plafondRepository.saveAndFlush(any(Plafond.class))).willReturn(testPlafond);

      // Act
      PlafondResponse result = plafondService.updatePlafond(1L, testPlafondRequest);

      // Assert
      assertThat(result).isNotNull();
      verify(plafondRepository).saveAndFlush(any(Plafond.class));
    }

    @Test
    @DisplayName("should throw exception when plafond not found")
    void shouldThrowException_whenPlafondNotFoundForUpdate() {
      // Arrange
      given(plafondRepository.findByIdActive(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> plafondService.updatePlafond(999L, testPlafondRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Plafond not found");
    }
  }

  // =========================================================================
  // Tests for deletePlafond() - Soft Delete
  // =========================================================================
  @Nested
  @DisplayName("deletePlafond() - Soft Delete")
  class DeletePlafondTests {

    @Test
    @DisplayName("should soft delete plafond when exists")
    void shouldSoftDeletePlafond_whenExists() {
      // Arrange
      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));
      given(plafondRepository.saveAndFlush(any(Plafond.class))).willReturn(testPlafond);

      // Act
      plafondService.deletePlafond(1L, "admin");

      // Assert
      verify(plafondRepository).saveAndFlush(any(Plafond.class));
      assertThat(testPlafond.getIsDeleted()).isTrue();
      assertThat(testPlafond.getDeletedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("should throw exception when plafond not found for delete")
    void shouldThrowException_whenPlafondNotFoundForDelete() {
      // Arrange
      given(plafondRepository.findByIdActive(999L)).willReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> plafondService.deletePlafond(999L, "admin"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Plafond not found");
    }
  }

  // =========================================================================
  // Tests for restorePlafond()
  // =========================================================================
  @Nested
  @DisplayName("restorePlafond()")
  class RestorePlafondTests {

    @Test
    @DisplayName("should restore soft-deleted plafond")
    void shouldRestorePlafond_whenDeleted() {
      // Arrange
      testPlafond.softDelete("admin");
      given(plafondRepository.findById(1L)).willReturn(Optional.of(testPlafond));
      given(plafondRepository.saveAndFlush(any(Plafond.class))).willReturn(testPlafond);

      // Act
      PlafondResponse result = plafondService.restorePlafond(1L);

      // Assert
      assertThat(result).isNotNull();
      verify(plafondRepository).saveAndFlush(any(Plafond.class));
    }

    @Test
    @DisplayName("should throw exception when plafond is not deleted")
    void shouldThrowException_whenPlafondNotDeleted() {
      // Arrange
      given(plafondRepository.findById(1L)).willReturn(Optional.of(testPlafond));

      // Act & Assert
      assertThatThrownBy(() -> plafondService.restorePlafond(1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Plafond is not deleted");
    }
  }

  // =========================================================================
  // Tests for toggleActiveStatus()
  // =========================================================================
  @Nested
  @DisplayName("toggleActiveStatus()")
  class ToggleActiveStatusTests {

    @Test
    @DisplayName("should toggle active status from true to false")
    void shouldToggleActiveStatus_whenCalled() {
      // Arrange
      given(plafondRepository.findByIdActive(1L)).willReturn(Optional.of(testPlafond));
      given(plafondRepository.saveAndFlush(any(Plafond.class))).willReturn(testPlafond);

      // Act
      plafondService.toggleActiveStatus(1L);

      // Assert
      assertThat(testPlafond.getIsActive()).isFalse(); // Toggled from true to false
      verify(plafondRepository).saveAndFlush(any(Plafond.class));
    }
  }
}
