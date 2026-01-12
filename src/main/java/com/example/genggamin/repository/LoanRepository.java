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
}
