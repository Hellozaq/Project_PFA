package com.example.springnodebackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, 
            @NonNull HttpServletResponse response, 
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.info("========== START AUTH FILTER ==========");
            log.info("Processing request to: {} {}", request.getMethod(), request.getRequestURI());
            
            // Print all headers for debugging
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                log.info("Header: {} = {}", headerName, request.getHeader(headerName));
            }
            
            String jwt = parseJwt(request);
            
            if (jwt == null) {
                log.warn("⚠️ No JWT token found in request to: {}", request.getRequestURI());
            } else {
                log.info("✅ JWT token found: {}", jwt.substring(0, Math.min(10, jwt.length())) + "...");
                
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromToken(jwt);
                    log.info("✅ Valid JWT token for user: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.info("✅ User details loaded: {}", userDetails);
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ Authentication set for user: {} with authorities: {}", username, userDetails.getAuthorities());
                } else {
                    log.error("❌ Invalid JWT token validation failed");
                }
            }
            
            // Check if authentication was set
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                log.info("✅ Authentication is set in SecurityContext: {}", auth.getName());
            } else {
                log.warn("⚠️ No authentication in SecurityContext");
            }
            
            log.info("========== END AUTH FILTER ==========");
        } catch (Exception e) {
            log.error("❌ Authentication error: {}", e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}