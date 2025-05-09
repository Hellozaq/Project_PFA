package com.example.springnodebackend.controller;

import com.example.springnodebackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/token")
@RequiredArgsConstructor
@Slf4j
public class TokenTestController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<?> testToken(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract token from header
            String token = authHeader.substring(7);
            
            // Test token validation
            boolean isValid = jwtTokenProvider.validateToken(token);
            response.put("isValid", isValid);
            
            if (isValid) {
                // Get username from token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                response.put("username", username);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing token: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
