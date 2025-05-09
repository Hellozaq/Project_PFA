package com.example.springnodebackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {
    private List<OrderSummaryResponse> orders;
    private int totalOrders;
}
