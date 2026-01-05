package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactRequest {

  private String contactName;
  private String contactPhone;
  private String relationship; // ORANG_TUA, SAUDARA, PASANGAN, TEMAN
}
