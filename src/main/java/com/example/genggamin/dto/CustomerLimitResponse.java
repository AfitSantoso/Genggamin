package com.example.genggamin.dto;

import com.example.genggamin.entity.CustomerLimit;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerLimitResponse {
  private Long id;
  private Long plafondId;
  private String plafondTitle;
  private BigDecimal totalLimit;
  private BigDecimal availableLimit;
  private Boolean isLocked;

  public static CustomerLimitResponse fromEntity(CustomerLimit limit) {
    return CustomerLimitResponse.builder()
        .id(limit.getId())
        .plafondId(limit.getPlafond().getId())
        .plafondTitle(limit.getPlafond().getTitle())
        .totalLimit(limit.getTotalLimit())
        .availableLimit(limit.getAvailableLimit())
        .isLocked(limit.getIsLocked())
        .build();
  }
}
