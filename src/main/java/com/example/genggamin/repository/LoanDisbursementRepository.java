package com.example.genggamin.repository;

import com.example.genggamin.entity.LoanDisbursement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, Long> {

  Optional<LoanDisbursement> findByLoanId(Long loanId);

  boolean existsByLoanId(Long loanId);
}
