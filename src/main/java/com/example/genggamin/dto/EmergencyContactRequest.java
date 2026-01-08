package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {

  private String name;
  private String phone;
  private String relationship; // ORANG_TUA, SAUDARA, PASANGAN, TEMAN
}
