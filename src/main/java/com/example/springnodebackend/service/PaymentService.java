package com.example.springnodebackend.service;

import com.example.springnodebackend.model.Order;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final OrderService orderService;
    
    public PaymentIntent createPaymentIntent(String orderId) throws StripeException {
        Order order = orderService.getOrderById(orderId);
        
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue())
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .putMetadata("orderId", orderId)
                .build();
        
        return PaymentIntent.create(params);
    }
    
    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        String orderId = paymentIntent.getMetadata().get("orderId");
        
        if ("succeeded".equals(paymentIntent.getStatus())) {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.PAID);
        }
        
        return paymentIntent;
    }
}