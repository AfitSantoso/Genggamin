package com.example.genggamin.repository;

import com.example.genggamin.entity.LoanReview;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanReviewRepository extends JpaRepository<LoanReview, Long> {
  Optional<LoanReview> findByLoanId(Long loanId);
}
