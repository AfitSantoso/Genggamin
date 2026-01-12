package com.example.genggamin.repository;

import com.example.genggamin.entity.LoanApproval;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApprovalRepository extends JpaRepository<LoanApproval, Long> {
  Optional<LoanApproval> findByLoanId(Long loanId);
}
