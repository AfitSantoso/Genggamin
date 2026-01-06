package com.example.genggamin.service;

import com.example.genggamin.dto.LoanActionRequest;
import com.example.genggamin.dto.LoanRequest;
import com.example.genggamin.dto.LoanResponse;
import com.example.genggamin.entity.Customer;
import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Loan.LoanStatus;
import com.example.genggamin.entity.Plafond;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.CustomerRepository;
import com.example.genggamin.repository.LoanRepository;
import com.example.genggamin.repository.PlafondRepository;
import com.example.genggamin.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {

  private final LoanRepository loanRepository;
  private final UserRepository userRepository;
  private final CustomerRepository customerRepository;
  private final PlafondRepository plafondRepository;

  public LoanService(
      LoanRepository loanRepository,
      UserRepository userRepository,
      CustomerRepository customerRepository,
      PlafondRepository plafondRepository) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.customerRepository = customerRepository;
    this.plafondRepository = plafondRepository;
  }

  @Transactional
  public LoanResponse submitLoan(String username, LoanRequest request) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Customer customer =
        customerRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Customer profile not found. Please create your profile first."));

    // Cari plafond berdasarkan customer income
    List<Plafond> eligiblePlafonds =
        plafondRepository.findByIncomeRange(customer.getMonthlyIncome());

    if (eligiblePlafonds.isEmpty()) {
      throw new RuntimeException(
          "No eligible plafond found for your income: " + customer.getMonthlyIncome());
    }

    // Ambil plafond pertama yang sesuai (bisa ditambahkan logic lebih kompleks)
    Plafond plafond = eligiblePlafonds.get(0);

    // Validasi amount tidak melebihi max plafond
    if (request.getAmount().compareTo(plafond.getMaxAmount()) > 0) {
      throw new RuntimeException(
          "Loan amount ("
              + request.getAmount()
              + ") exceeds maximum allowed ("
              + plafond.getMaxAmount()
              + ") for your income level");
    }

    // Validasi tenure tidak melebihi max plafond
    if (request.getTenureMonths() > plafond.getTenorMonth()) {
      throw new RuntimeException(
          "Loan tenure ("
              + request.getTenureMonths()
              + " months) exceeds maximum allowed ("
              + plafond.getTenorMonth()
              + " months) for your income level");
    }

    Loan loan =
        Loan.builder()
            .customer(customer)
            .plafondId(plafond.getId())
            .amount(request.getAmount())
            .tenureMonths(request.getTenureMonths())
            .interestRate(plafond.getInterestRate())
            .purpose(request.getPurpose())
            .status(LoanStatus.SUBMITTED)
            .submittedAt(LocalDateTime.now())
            .build();

    Loan savedLoan = loanRepository.save(loan);
    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getMyLoans(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Customer customer =
        customerRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Customer profile not found"));

    return loanRepository.findByCustomerId(customer.getId()).stream()
        .map(LoanResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansForReview() {
    return loanRepository.findByStatus(LoanStatus.SUBMITTED).stream()
        .map(LoanResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public LoanResponse reviewLoan(Long loanId, String username, LoanActionRequest request) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.SUBMITTED) {
      throw new RuntimeException("Loan is not in SUBMITTED status");
    }

    loan.setStatus(LoanStatus.UNDER_REVIEW);
    loan.setReviewNotes(request.getNotes());
    loan.setReviewedBy(username);
    loan.setReviewedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);
    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansForApproval() {
    return loanRepository.findByStatus(LoanStatus.UNDER_REVIEW).stream()
        .map(LoanResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public LoanResponse approveLoan(Long loanId, String username, LoanActionRequest request) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.UNDER_REVIEW) {
      throw new RuntimeException("Loan is not in UNDER_REVIEW status");
    }

    if (request.getApproved() != null && request.getApproved()) {
      loan.setStatus(LoanStatus.APPROVED);
      loan.setApprovalNotes(request.getNotes());
      loan.setApprovedBy(username);
      loan.setApprovedAt(LocalDateTime.now());
    } else {
      loan.setStatus(LoanStatus.REJECTED);
      loan.setApprovalNotes(request.getNotes());
      loan.setApprovedBy(username);
      loan.setApprovedAt(LocalDateTime.now());
    }

    Loan savedLoan = loanRepository.save(loan);
    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansForDisbursement() {
    return loanRepository.findByStatus(LoanStatus.APPROVED).stream()
        .map(LoanResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public LoanResponse disburseLoan(Long loanId, String username, LoanActionRequest request) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.APPROVED) {
      throw new RuntimeException("Loan is not in APPROVED status");
    }

    loan.setStatus(LoanStatus.DISBURSED);
    loan.setDisbursementNotes(request.getNotes());
    loan.setDisbursedBy(username);
    loan.setDisbursedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);
    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getAllLoans() {
    return loanRepository.findAll().stream()
        .map(LoanResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanById(Long loanId) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
    return LoanResponse.fromEntity(loan);
  }
}
