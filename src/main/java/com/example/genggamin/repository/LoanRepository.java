package com.example.genggamin.repository;

import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Loan.LoanStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
  List<Loan> findByCustomerId(Long customerId);

  List<Loan> findByStatus(LoanStatus status);

  @Query(
      "SELECT l FROM Loan l WHERE l.id IN "
          + "(SELECT lr.loanId FROM LoanReview lr) "
          + "ORDER BY l.updatedAt DESC")
  List<Loan> findAllReviewedLoans();

  @Query(
      "SELECT l FROM Loan l WHERE l.id IN "
          + "(SELECT la.loanId FROM LoanApproval la) "
          + "ORDER BY l.updatedAt DESC")
  List<Loan> findAllApprovedLoans();

  @org.springframework.data.jpa.repository.query.Procedure(
      procedureName = "sp_CreateLoanWithLimitCheck")
  void createLoanWithLimitCheck(
      @org.springframework.data.repository.query.Param("CustomerID") Long customerId,
      @org.springframework.data.repository.query.Param("PlafondID") Long plafondId,
      @org.springframework.data.repository.query.Param("LoanAmount")
          java.math.BigDecimal loanAmount,
      @org.springframework.data.repository.query.Param("TenorMonth") Long tenorMonth,
      @org.springframework.data.repository.query.Param("InterestRate")
          java.math.BigDecimal interestRate,
      @org.springframework.data.repository.query.Param("Purpose") String purpose,
      @org.springframework.data.repository.query.Param("Latitude") java.math.BigDecimal latitude,
      @org.springframework.data.repository.query.Param("Longitude") java.math.BigDecimal longitude);

  @Query(
      value = "SELECT TOP 1 * FROM loans WHERE customer_id = :customerId ORDER BY id DESC",
      nativeQuery = true)
  java.util.Optional<Loan> findLatestLoanByCustomerId(
      @org.springframework.data.repository.query.Param("customerId") Long customerId);
}
