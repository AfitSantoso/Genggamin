package com.example.genggamin.service;

import com.example.genggamin.dto.LoanActionRequest;
import com.example.genggamin.dto.LoanRequest;
import com.example.genggamin.dto.LoanResponse;
import com.example.genggamin.dto.LoanWithApprovalResponse;
import com.example.genggamin.dto.LoanWithDisbursementResponse;
import com.example.genggamin.dto.LoanWithReviewResponse;
import com.example.genggamin.entity.Customer;
import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Loan.LoanStatus;
import com.example.genggamin.entity.LoanApproval;
import com.example.genggamin.entity.LoanDisbursement;
import com.example.genggamin.entity.LoanReview;
import com.example.genggamin.entity.Plafond;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.CustomerRepository;
import com.example.genggamin.repository.LoanApprovalRepository;
import com.example.genggamin.repository.LoanDisbursementRepository;
import com.example.genggamin.repository.LoanRepository;
import com.example.genggamin.repository.LoanReviewRepository;
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
  private final LoanReviewRepository loanReviewRepository;
  private final LoanApprovalRepository loanApprovalRepository;
  private final LoanDisbursementRepository loanDisbursementRepository;
  private final EmailService emailService;

  public LoanService(
      LoanRepository loanRepository,
      UserRepository userRepository,
      CustomerRepository customerRepository,
      PlafondRepository plafondRepository,
      LoanReviewRepository loanReviewRepository,
      LoanApprovalRepository loanApprovalRepository,
      LoanDisbursementRepository loanDisbursementRepository,
      EmailService emailService) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
    this.customerRepository = customerRepository;
    this.plafondRepository = plafondRepository;
    this.loanReviewRepository = loanReviewRepository;
    this.loanApprovalRepository = loanApprovalRepository;
    this.loanDisbursementRepository = loanDisbursementRepository;
    this.emailService = emailService;
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

    // Validasi plafondId wajib diisi
    if (request.getPlafondId() == null) {
      throw new RuntimeException("Plafond ID is required. Please select a plafond.");
    }

    // Ambil plafond berdasarkan ID yang dipilih customer
    Plafond plafond =
        plafondRepository
            .findById(request.getPlafondId())
            .orElseThrow(() -> new RuntimeException("Plafond not found with ID: " + request.getPlafondId()));

    // Validasi plafond aktif
    if (!plafond.getIsActive()) {
      throw new RuntimeException("Selected plafond is not active");
    }

    // Validasi plafond eligible untuk customer income
    List<Plafond> eligiblePlafonds =
        plafondRepository.findByIncomeRange(customer.getMonthlyIncome());

    boolean isEligible = eligiblePlafonds.stream()
        .anyMatch(p -> p.getId().equals(plafond.getId()));

    if (!isEligible) {
      throw new RuntimeException(
          "Selected plafond is not eligible for your income level (Rp " + customer.getMonthlyIncome() + ")");
    }

    // Validasi amount tidak melebihi max plafond
    if (request.getAmount().compareTo(plafond.getMaxAmount()) > 0) {
      throw new RuntimeException(
          "Loan amount ("
              + request.getAmount()
              + ") exceeds maximum allowed ("
              + plafond.getMaxAmount()
              + ") for selected plafond");
    }

    // Validasi tenure tidak melebihi max plafond
    if (request.getTenureMonths() > plafond.getTenorMonth()) {
      throw new RuntimeException(
          "Loan tenure ("
              + request.getTenureMonths()
              + " months) exceeds maximum allowed ("
              + plafond.getTenorMonth()
              + " months) for selected plafond");
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

  /**
   * Populate review and approval data from loan_reviews and loan_approvals tables into Loan
   * @Transient fields
   */
  private void populateLoanDetails(Loan loan) {
    // Populate review data
    loanReviewRepository
        .findByLoanId(loan.getId())
        .ifPresent(
            review -> {
              loan.setReviewNotes(review.getReviewNotes());
              loan.setReviewedAt(review.getReviewedAt());
              // Get reviewer username
              userRepository
                  .findById(review.getReviewedBy())
                  .ifPresent(user -> loan.setReviewedBy(user.getUsername()));
            });

    // Populate approval data
    loanApprovalRepository
        .findByLoanId(loan.getId())
        .ifPresent(
            approval -> {
              loan.setApprovalNotes(approval.getApprovalNotes());
              loan.setApprovedAt(approval.getApprovedAt());
              // Get approver username
              userRepository
                  .findById(approval.getApprovedBy())
                  .ifPresent(user -> loan.setApprovedBy(user.getUsername()));
            });

    // Populate disbursement data
    loanDisbursementRepository
        .findByLoanId(loan.getId())
        .ifPresent(
            disbursement -> {
              loan.setDisbursedAt(disbursement.getDisbursementDate());
              // Get disburser username
              userRepository
                  .findById(disbursement.getDisbursedBy())
                  .ifPresent(user -> loan.setDisbursedBy(user.getUsername()));
            });
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
        .map(
            loan -> {
              populateLoanDetails(loan);
              return LoanResponse.fromEntity(loan);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansForReview() {
    return loanRepository.findByStatus(LoanStatus.SUBMITTED).stream()
        .map(
            loan -> {
              populateLoanDetails(loan);
              return LoanResponse.fromEntity(loan);
            })
        .collect(Collectors.toList());
  }

  @Transactional
  public LoanResponse reviewLoan(Long loanId, String username, LoanActionRequest request) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.SUBMITTED) {
      throw new RuntimeException("Loan is not in SUBMITTED status");
    }

    // Get user ID
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Determine review status based on action
    String reviewStatus;
    LoanStatus newLoanStatus;

    if ("APPROVE".equalsIgnoreCase(request.getAction())) {
      reviewStatus = "APPROVED";
      newLoanStatus = LoanStatus.UNDER_REVIEW;
    } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
      reviewStatus = "REJECTED";
      newLoanStatus = LoanStatus.REJECTED;
    } else {
      throw new RuntimeException("Invalid action. Must be APPROVE or REJECT");
    }

    // Save to loan_reviews table
    LoanReview loanReview =
        LoanReview.builder()
            .loanId(loanId)
            .reviewedBy(user.getId())
            .reviewNotes(request.getNotes())
            .reviewStatus(reviewStatus)
            .reviewedAt(LocalDateTime.now())
            .build();

    loanReviewRepository.save(loanReview);

    // Update loan status
    loan.setStatus(newLoanStatus);
    loan.setUpdatedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Populate review details
    populateLoanDetails(savedLoan);

    // Send email if rejected
    if (newLoanStatus == LoanStatus.REJECTED) {
      try {
        User customerUser =
            userRepository.findById(savedLoan.getCustomer().getUserId()).orElse(null);
        if (customerUser != null) {
          emailService.sendLoanRejectedEmail(
              customerUser.getEmail(),
              customerUser.getUsername(),
              savedLoan.getId(),
              request.getNotes());
        }
      } catch (Exception e) {
        // Log error (or use logger if available in class)
        System.err.println("Failed to send rejection email: " + e.getMessage());
      }
    }

    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanWithReviewResponse> getReviewedLoans() {
    List<Loan> reviewedLoans = loanRepository.findAllReviewedLoans();

    return reviewedLoans.stream()
        .map(
            loan -> {
              LoanReview review =
                  loanReviewRepository
                      .findByLoanId(loan.getId())
                      .orElseThrow(
                          () ->
                              new RuntimeException("Review not found for loan id: " + loan.getId()));
              return LoanWithReviewResponse.fromEntities(loan, review);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansForApproval() {
    return loanRepository.findByStatus(LoanStatus.UNDER_REVIEW).stream()
        .map(
            loan -> {
              populateLoanDetails(loan);
              return LoanResponse.fromEntity(loan);
            })
        .collect(Collectors.toList());
  }

  @Transactional
  public LoanResponse approveLoan(Long loanId, String username, LoanActionRequest request) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.UNDER_REVIEW) {
      throw new RuntimeException("Loan is not in UNDER_REVIEW status");
    }

    // Get user ID
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Determine approval status based on action
    String approvalStatus;
    LoanStatus newLoanStatus;

    if (request.getApproved() != null && request.getApproved()) {
      approvalStatus = "APPROVED";
      newLoanStatus = LoanStatus.APPROVED;
    } else {
      approvalStatus = "REJECTED";
      newLoanStatus = LoanStatus.REJECTED;
    }

    // Save to loan_approvals table
    LoanApproval loanApproval =
        LoanApproval.builder()
            .loanId(loanId)
            .approvedBy(user.getId())
            .approvalStatus(approvalStatus)
            .approvalNotes(request.getNotes())
            .approvedAt(LocalDateTime.now())
            .build();

    loanApprovalRepository.save(loanApproval);

    // Update loan status
    loan.setStatus(newLoanStatus);
    loan.setUpdatedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);

    // Populate previous stages details (Review & Approval)
    populateLoanDetails(savedLoan);

    // Send email
    try {
      User customerUser =
          userRepository.findById(savedLoan.getCustomer().getUserId()).orElse(null);
      if (customerUser != null) {
        if (newLoanStatus == LoanStatus.APPROVED) {
          emailService.sendLoanApprovedEmail(
              customerUser.getEmail(),
              customerUser.getUsername(),
              savedLoan.getId(),
              savedLoan.getAmount());
        } else if (newLoanStatus == LoanStatus.REJECTED) {
          emailService.sendLoanRejectedEmail(
              customerUser.getEmail(),
              customerUser.getUsername(),
              savedLoan.getId(),
              request.getNotes());
        }
      }
    } catch (Exception e) {
      System.err.println("Failed to send approval/rejection email: " + e.getMessage());
    }

    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanWithApprovalResponse> getApprovedLoansWithDetails() {
    List<Loan> approvedLoans = loanRepository.findAllApprovedLoans();

    return approvedLoans.stream()
        .map(
            loan -> {
              LoanApproval approval =
                  loanApprovalRepository
                      .findByLoanId(loan.getId())
                      .orElseThrow(
                          () ->
                              new RuntimeException(
                                  "Approval not found for loan id: " + loan.getId()));
              return LoanWithApprovalResponse.fromEntities(loan, approval);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getLoansForDisbursement() {
    return loanRepository.findByStatus(LoanStatus.APPROVED).stream()
        .map(
            loan -> {
              populateLoanDetails(loan);
              return LoanResponse.fromEntity(loan);
            })
        .collect(Collectors.toList());
  }

  @Transactional
  public LoanResponse disburseLoan(Long loanId, String username, LoanActionRequest request) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));

    if (loan.getStatus() != LoanStatus.APPROVED) {
      throw new RuntimeException("Loan is not in APPROVED status");
    }

    // Check if loan already disbursed
    if (loanDisbursementRepository.existsByLoanId(loanId)) {
      throw new RuntimeException("Loan has already been disbursed");
    }

    // Validate action
    if (!"DISBURSE".equalsIgnoreCase(request.getAction())) {
      throw new RuntimeException("Invalid action. Must be DISBURSE");
    }

    // Validate bank account
    if (request.getBankAccount() == null || request.getBankAccount().trim().isEmpty()) {
      throw new RuntimeException("Bank account is required for disbursement");
    }

    // Get user ID
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Save to loan_disbursements table
    LoanDisbursement disbursement =
        new LoanDisbursement(
            loanId,
            user.getId(),
            loan.getAmount(),
            request.getBankAccount(),
            "COMPLETED");

    loanDisbursementRepository.save(disbursement);

    // Update loan status
    loan.setStatus(LoanStatus.DISBURSED);
    loan.setUpdatedAt(LocalDateTime.now());

    Loan savedLoan = loanRepository.save(loan);
    
    // Populate previous stages details (Review, Approval, Disbursement)
    populateLoanDetails(savedLoan);
    
    // Manually set disbursement notes from request since we don't persist it
    savedLoan.setDisbursementNotes(request.getNotes());

    // Send email
    try {
      User customerUser =
          userRepository.findById(savedLoan.getCustomer().getUserId()).orElse(null);
      if (customerUser != null) {
        emailService.sendLoanDisbursedEmail(
            customerUser.getEmail(),
            customerUser.getUsername(),
            savedLoan.getId(),
            savedLoan.getAmount(),
            request.getBankAccount());
      }
    } catch (Exception e) {
      System.err.println("Failed to send disbursement email: " + e.getMessage());
    }

    return LoanResponse.fromEntity(savedLoan);
  }

  @Transactional(readOnly = true)
  public List<LoanWithDisbursementResponse> getDisbursedLoansWithDetails() {
    List<Loan> disbursedLoans = loanRepository.findByStatus(LoanStatus.DISBURSED);

    return disbursedLoans.stream()
        .map(
            loan -> {
              // Find disbursement, return null if not found (will be filtered out)
              LoanDisbursement disbursement =
                  loanDisbursementRepository
                      .findByLoanId(loan.getId())
                      .orElse(null);
              
              // Skip loan if no disbursement data found
              if (disbursement == null) {
                return null;
              }
              
              LoanWithDisbursementResponse response = new LoanWithDisbursementResponse();
              // Set loan fields
              response.setId(loan.getId());
              response.setCustomerId(loan.getCustomer().getId());
              response.setPlafondId(loan.getPlafondId());
              response.setLoanAmount(loan.getAmount());
              response.setTenorMonth(loan.getTenureMonths());
              response.setInterestRate(loan.getInterestRate());
              response.setPurpose(loan.getPurpose());
              response.setStatus(loan.getStatus().toString());
              response.setSubmissionDate(loan.getSubmittedAt());
              response.setCreatedAt(loan.getCreatedAt());
              response.setUpdatedAt(loan.getUpdatedAt());
              
              // Set disbursement fields
              response.setDisbursementId(disbursement.getId());
              response.setDisbursedBy(disbursement.getDisbursedBy());
              response.setDisbursementAmount(disbursement.getDisbursementAmount());
              response.setDisbursementDate(disbursement.getDisbursementDate());
              response.setBankAccount(disbursement.getBankAccount());
              response.setDisbursementStatus(disbursement.getStatus());
              
              // Populate approval details
              loanApprovalRepository.findByLoanId(loan.getId())
                  .ifPresent(approval -> {
                      response.setApprovalId(approval.getId());
                      response.setApprovedBy(approval.getApprovedBy());
                      response.setApprovalStatus(approval.getApprovalStatus());
                      response.setApprovalNotes(approval.getApprovalNotes());
                      response.setApprovedAt(approval.getApprovedAt());
                  });
              
              return response;
            })
        .filter(response -> response != null) // Filter out null responses
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<LoanResponse> getAllLoans() {
    return loanRepository.findAll().stream()
        .map(
            loan -> {
              populateLoanDetails(loan);
              return LoanResponse.fromEntity(loan);
            })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public LoanResponse getLoanById(Long loanId) {
    Loan loan =
        loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
    populateLoanDetails(loan);
    return LoanResponse.fromEntity(loan);
  }
}
