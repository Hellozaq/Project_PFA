package com.example.springnodebackend.controller;

import com.example.springnodebackend.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntent> createPaymentIntent(@RequestParam String orderId) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(orderId));
    }
    
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentIntent> confirmPayment(@PathVariable String paymentIntentId) throws StripeException {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentIntentId));
    }
}