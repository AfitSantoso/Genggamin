package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName; // from User table
    private String nik;
    private String address;
    private LocalDate dateOfBirth;
    private BigDecimal monthlyIncome;
    private String customerFullName; // from Customer table
    private String customerPhone;
    private String currentAddress;
    private String motherMaidenName;
    private List<EmergencyContactResponse> emergencyContacts;
    private LocalDateTime createdAt;
}
