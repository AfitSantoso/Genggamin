package com.example.genggamin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
