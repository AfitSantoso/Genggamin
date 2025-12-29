package com.example.genggamin.repository;

import com.example.genggamin.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
    
    boolean existsByNik(String nik);
    
    Optional<Customer> findByNik(String nik);
}
