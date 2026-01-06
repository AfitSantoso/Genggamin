package com.example.genggamin.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactResponse {

  private Long id;
  private String contactName;
  private String contactPhone;
  private String relationship;
  private LocalDateTime createdAt;
}
