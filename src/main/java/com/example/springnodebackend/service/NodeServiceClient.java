package com.example.springnodebackend.service;

import com.example.springnodebackend.payload.request.NotificationRequest;
import com.example.springnodebackend.payload.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${node-service.base-url}")
    private String nodeServiceBaseUrl;
    
    @Value("${node-service.endpoints.notifications}")
    private String notificationsEndpoint;
    
    @Value("${node-service.endpoints.real-time-events}")
    private String eventsEndpoint;
    
    public NotificationResponse sendNotification(NotificationRequest request, String token) {
        String url = nodeServiceBaseUrl + notificationsEndpoint;
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);
        
        log.info("Sending notification to Node.js service: {}", request);
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                ResponseEntity<NotificationResponse> response = restTemplate.exchange(
                        url, 
                        HttpMethod.POST, 
                        entity, 
                        NotificationResponse.class);
                
                log.info("Notification sent successfully: {}", response.getBody());
                return response.getBody();
            } catch (Exception e) {
                lastException = e;
                log.warn("Error sending notification to Node.js service (attempt {} of {}): {}", 
                        retryCount + 1, maxRetries, e.getMessage());
                retryCount++;
                
                if (retryCount < maxRetries) {
                    try {
                        // Exponential backoff: wait longer between each retry
                        Thread.sleep(1000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.error("Failed to send notification after {} attempts", maxRetries, lastException);
        throw new RuntimeException("Failed to send notification after multiple attempts", lastException);
    }
    
    public void sendEvent(Object event, String token) {
        String url = nodeServiceBaseUrl + eventsEndpoint;
        
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", "Bearer " + token);
        }
        
        HttpEntity<Object> entity = new HttpEntity<>(event, headers);
        
        log.info("Sending event to Node.js service: {}", event);
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                restTemplate.exchange(
                        url, 
                        HttpMethod.POST, 
                        entity, 
                        Void.class);
                
                log.info("Event sent successfully");
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("Error sending event to Node.js service (attempt {} of {}): {}", 
                        retryCount + 1, maxRetries, e.getMessage());
                retryCount++;
                
                if (retryCount < maxRetries) {
                    try {
                        // Exponential backoff: wait longer between each retry
                        Thread.sleep(1000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        log.error("Failed to send event after {} attempts", maxRetries, lastException);
        throw new RuntimeException("Failed to send event after multiple attempts", lastException);
    }
}