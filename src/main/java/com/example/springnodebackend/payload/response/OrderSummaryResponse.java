package com.example.springnodebackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private String id;
    private String status;
    private BigDecimal totalAmount;
    private int itemCount;
    private String createdAt;
}
