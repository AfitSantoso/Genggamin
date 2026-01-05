package com.example.genggamin.service;

import com.example.genggamin.dto.LoanActionRequest;
import com.example.genggamin.dto.LoanRequest;
import com.example.genggamin.dto.LoanResponse;
import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Loan.LoanStatus;
import com.example.genggamin.entity.User;
import com.example.genggamin.repository.LoanRepository;
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

  public LoanService(LoanRepository loanRepository, UserRepository userRepository) {
    this.loanRepository = loanRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public LoanResponse submitLoan(String username, LoanRequest request) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Loan loan =
        Loan.builder()
            .user(user)
            .amount(request.getAmount())
            .tenureMonths(request.getTenureMonths())
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

    return loanRepository.findByUserId(user.getId()).stream()
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
