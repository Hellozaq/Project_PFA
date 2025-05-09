package com.example.springnodebackend.payload.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    private String street;
    private String city;
    private String country;
    private String phone;
    private String paymentId;
    private String paymentMethod;
    private String notes;
}
