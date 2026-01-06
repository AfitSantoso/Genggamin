package com.example.genggamin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "customer_id",
      referencedColumnName = "id",
      insertable = false,
      updatable = false)
  @JsonIgnore
  private Customer customer;

  @Column(name = "contact_name", nullable = false, length = 150)
  private String contactName;

  @Column(name = "contact_phone", nullable = false, length = 20)
  private String contactPhone;

  @Column(name = "relationship", length = 50)
  private String relationship;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
