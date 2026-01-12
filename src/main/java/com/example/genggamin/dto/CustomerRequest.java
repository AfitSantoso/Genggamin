package com.example.genggamin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

  private String nik;
  private LocalDate dateOfBirth;
  private String placeOfBirth;
  private String address;
  private String currentAddress;
  private String phone;
  private BigDecimal monthlyIncome;
  private String occupation;
  private String motherMaidenName;
  private EmergencyContactRequest emergencyContact;
}
