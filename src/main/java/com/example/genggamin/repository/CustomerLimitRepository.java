package com.example.genggamin.repository;

import com.example.genggamin.entity.CustomerLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerLimitRepository extends JpaRepository<CustomerLimit, Long> {
    java.util.List<CustomerLimit> findByCustomer_Id(Long customerId);
    Optional<CustomerLimit> findByCustomer_IdAndPlafond_Id(Long customerId, Long plafondId);
}
