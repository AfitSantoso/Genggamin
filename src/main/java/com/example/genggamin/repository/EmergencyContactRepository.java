package com.example.genggamin.repository;

import com.example.genggamin.entity.EmergencyContact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

  List<EmergencyContact> findByCustomerId(Long customerId);

  void deleteByCustomerId(Long customerId);
}
