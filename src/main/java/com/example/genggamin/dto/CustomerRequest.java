package com.example.genggamin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

  private String nik;
  private String address;
  private LocalDate dateOfBirth;
  private BigDecimal monthlyIncome;
  private String fullName;
  private String phone;
  private String currentAddress;
  private String motherMaidenName;
  private List<EmergencyContactRequest> emergencyContacts;
}
