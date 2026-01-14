package com.example.genggamin.service;

import com.example.genggamin.dto.LoanSimulationRequest;
import com.example.genggamin.dto.LoanSimulationResponse;
import com.example.genggamin.dto.PlafondRequest;
import com.example.genggamin.dto.PlafondResponse;
import com.example.genggamin.entity.Plafond;
import com.example.genggamin.repository.PlafondRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service untuk mengelola Plafond Menggunakan Redis caching untuk performa optimal */
@Service
@Transactional
public class PlafondService {

  private final PlafondRepository plafondRepository;

  public PlafondService(PlafondRepository plafondRepository) {
    this.plafondRepository = plafondRepository;
  }

  /** Get all plafonds yang tidak dihapus Cached dengan key "allPlafonds" */
  @Cacheable(value = "plafonds", key = "'allPlafonds'")
  @Transactional(readOnly = true)
  public List<PlafondResponse> getAllPlafonds() {
    return plafondRepository.findAllActive().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all active plafonds (isActive = true dan tidak dihapus) Cached dengan key "activePlafonds"
   */
  @Cacheable(value = "plafonds", key = "'activePlafonds'")
  @Transactional(readOnly = true)
  public List<PlafondResponse> getActivePlafonds() {
    return plafondRepository.findAllActiveAndNotDeleted().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /** Get plafond by ID (active only) Cached dengan key berdasarkan ID */
  @Cacheable(value = "plafonds", key = "'plafond:' + #id")
  @Transactional(readOnly = true)
  public PlafondResponse getPlafondById(Long id) {
    Plafond plafond =
        plafondRepository
            .findByIdActive(id)
            .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));
    return mapToResponse(plafond);
  }

  /** Get plafonds by income range Untuk matching dengan customer income */
  @Cacheable(value = "plafonds", key = "'byIncome:' + #income")
  @Transactional(readOnly = true)
  public List<PlafondResponse> getPlafondsByIncome(BigDecimal income) {
    return plafondRepository.findByIncomeRange(income).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  /**
   * Simulate loan installment based on amount and tenor (or specific plafond)
   *
   * @param request simulation request containing amount, tenor, and optional plafondId
   * @return LoanSimulationResponse with calculation details
   */
  public LoanSimulationResponse simulateLoan(LoanSimulationRequest request) {
    // Determine which plafond to use
    Plafond selectedPlafond = null;

    if (request.getPlafondId() != null) {
      selectedPlafond =
          plafondRepository
              .findByIdActive(request.getPlafondId())
              .orElseThrow(
                  () -> new RuntimeException("Plafond not found with id: " + request.getPlafondId()));

      // Validate tenor matches (requested tenor must be <= plafond's max tenor)
      if (request.getTenor() > selectedPlafond.getTenorMonth()) {
        throw new RuntimeException("Requested tenor (" + request.getTenor() + ") exceeds maximum tenor (" + selectedPlafond.getTenorMonth() + ") for this plafond");
      }
    } else {
      // Find matching plafond by tenor and amount
      List<Plafond> candidates = plafondRepository.findByTenor(request.getTenor());
      
      // Filter by maxAmount >= requested amount
      // Sort by interest rate ASC (best rate for user)
      selectedPlafond = candidates.stream()
          .filter(p -> p.getMaxAmount().compareTo(request.getAmount()) >= 0)
          .min(Comparator.comparing(Plafond::getInterestRate))
          .orElseThrow(() -> new RuntimeException("No suitable plafond found for the requested amount and tenor"));
    }

    // Validate max amount again just in case (e.g. if plafondId was used but amount exceeds it)
    if (request.getAmount().compareTo(selectedPlafond.getMaxAmount()) > 0) {
       // Should we block? The user might just want to see hypotheticals.
       // The prompt says "simulasi", maybe strict validation is good to guide them.
       // Let's assume strict validation or just warning. I'll throw exception for now to be safe.
       throw new RuntimeException("Requested amount exceeds the maximum limit for this plafond: " + selectedPlafond.getMaxAmount());
    }

    BigDecimal principal = request.getAmount();
    BigDecimal annualRatePercent = selectedPlafond.getInterestRate();
    BigDecimal tenorMonths = BigDecimal.valueOf(request.getTenor());

    // Calculation: Flat Rate
    // Interest per year = Principal * (Rate/100)
    // Total Interest = Interest per year * (Tenor / 12)
    // Total Payment = Principal + Total Interest
    // Monthly Installment = Total Payment / Tenor

    BigDecimal rateDecimal = annualRatePercent.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
    BigDecimal years = tenorMonths.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
    
    BigDecimal totalInterest = principal.multiply(rateDecimal).multiply(years).setScale(2, RoundingMode.HALF_UP);
    BigDecimal totalPayment = principal.add(totalInterest).setScale(2, RoundingMode.HALF_UP);
    BigDecimal monthlyInstallment = totalPayment.divide(tenorMonths, 2, RoundingMode.HALF_UP);

    return LoanSimulationResponse.builder()
        .loanAmount(principal)
        .tenorMonth(request.getTenor())
        .interestRate(annualRatePercent)
        .monthlyInstallment(monthlyInstallment)
        .totalInterest(totalInterest)
        .totalPayment(totalPayment)
        .plafondId(selectedPlafond.getId())
        .build();
  }

  /** Create new plafond Evict cache untuk refresh data Hanya bisa dilakukan oleh ADMIN */
  @CacheEvict(value = "plafonds", allEntries = true)
  public PlafondResponse createPlafond(PlafondRequest request) {
    // Validasi request
    request.validate();

    // Check duplicate
    if (plafondRepository.existsByMinIncomeAndMaxAmountAndTenor(
        request.getMinIncome(), request.getMaxAmount(), request.getTenorMonth())) {
      throw new RuntimeException(
          "Plafond with same min income, max amount, and tenor already exists");
    }

    // Build entity
    Plafond plafond =
        Plafond.builder()
            .minIncome(request.getMinIncome())
            .maxAmount(request.getMaxAmount())
            .tenorMonth(request.getTenorMonth())
            .interestRate(request.getInterestRate())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .isDeleted(false)
            .build();

    Plafond saved = plafondRepository.saveAndFlush(plafond);
    return mapToResponse(saved);
  }

  /** Update existing plafond Evict cache untuk refresh data Hanya bisa dilakukan oleh ADMIN */
  @CacheEvict(value = "plafonds", allEntries = true)
  public PlafondResponse updatePlafond(Long id, PlafondRequest request) {
    // Validasi request
    request.validate();

    // Find existing plafond
    Plafond plafond =
        plafondRepository
            .findByIdActive(id)
            .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

    // Check duplicate (exclude current plafond)
    if (plafondRepository.existsByMinIncomeAndMaxAmountAndTenorAndIdNot(
        request.getMinIncome(), request.getMaxAmount(), request.getTenorMonth(), id)) {
      throw new RuntimeException(
          "Plafond with same min income, max amount, and tenor already exists");
    }

    // Update fields
    plafond.setMinIncome(request.getMinIncome());
    plafond.setMaxAmount(request.getMaxAmount());
    plafond.setTenorMonth(request.getTenorMonth());
    plafond.setInterestRate(request.getInterestRate());

    if (request.getIsActive() != null) {
      plafond.setIsActive(request.getIsActive());
    }

    Plafond updated = plafondRepository.saveAndFlush(plafond);
    return mapToResponse(updated);
  }

  /** Soft delete plafond Evict cache untuk refresh data Hanya bisa dilakukan oleh ADMIN */
  @CacheEvict(value = "plafonds", allEntries = true)
  public void deletePlafond(Long id, String deletedBy) {
    Plafond plafond =
        plafondRepository
            .findByIdActive(id)
            .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

    // Soft delete
    plafond.softDelete(deletedBy);
    plafondRepository.saveAndFlush(plafond);
  }

  /** Restore soft deleted plafond Hanya bisa dilakukan oleh ADMIN */
  @CacheEvict(value = "plafonds", allEntries = true)
  public PlafondResponse restorePlafond(Long id) {
    Plafond plafond =
        plafondRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

    if (!plafond.getIsDeleted()) {
      throw new RuntimeException("Plafond is not deleted");
    }

    plafond.restore();
    Plafond restored = plafondRepository.saveAndFlush(plafond);
    return mapToResponse(restored);
  }

  /** Toggle plafond active status Hanya bisa dilakukan oleh ADMIN */
  @CacheEvict(value = "plafonds", allEntries = true)
  public PlafondResponse toggleActiveStatus(Long id) {
    Plafond plafond =
        plafondRepository
            .findByIdActive(id)
            .orElseThrow(() -> new RuntimeException("Plafond not found with id: " + id));

    plafond.setIsActive(!plafond.getIsActive());
    Plafond updated = plafondRepository.saveAndFlush(plafond);
    return mapToResponse(updated);
  }

  /** Helper method untuk mapping Entity ke DTO */
  private PlafondResponse mapToResponse(Plafond plafond) {
    return PlafondResponse.builder()
        .id(plafond.getId())
        .minIncome(plafond.getMinIncome())
        .maxAmount(plafond.getMaxAmount())
        .tenorMonth(plafond.getTenorMonth())
        .interestRate(plafond.getInterestRate())
        .isActive(plafond.getIsActive())
        .createdAt(plafond.getCreatedAt())
        .updatedAt(plafond.getUpdatedAt())
        .build();
  }
}
