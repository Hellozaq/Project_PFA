package com.example.springnodebackend.config;

import com.example.springnodebackend.model.Role;
import com.example.springnodebackend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Initialize roles if they don't exist
        for (Role.ERole roleName : Role.ERole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }
    }
}
