package com.example.genggamin.repository;

import com.example.genggamin.entity.Loan;
import com.example.genggamin.entity.Loan.LoanStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
  List<Loan> findByUserId(Long userId);

  List<Loan> findByStatus(LoanStatus status);
}
