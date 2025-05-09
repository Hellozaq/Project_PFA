package com.example.springnodebackend.controller;

import com.example.springnodebackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/debug")
@RequiredArgsConstructor
@Slf4j
public class AuthDebugController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/token-info")
    public ResponseEntity<?> getTokenInfo(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if token is valid
            boolean isValid = jwtTokenProvider.validateToken(token);
            response.put("isValid", isValid);
            
            if (isValid) {
                // Get username from token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                response.put("username", username);
            }
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/auth-status")
    public ResponseEntity<?> getAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("isAuthenticated", true);
            response.put("principal", authentication.getPrincipal());
            response.put("authorities", authentication.getAuthorities());
            response.put("details", authentication.getDetails());
        } else {
            response.put("isAuthenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }
}
