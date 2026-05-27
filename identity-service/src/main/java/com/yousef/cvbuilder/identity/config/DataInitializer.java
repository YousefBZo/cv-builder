package com.yousef.cvbuilder.identity.config;

import com.yousef.cvbuilder.identity.dto.RegisterRequest;
import com.yousef.cvbuilder.identity.entity.UserAccount;
import com.yousef.cvbuilder.identity.repository.UserRepository;
import com.yousef.cvbuilder.identity.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final IdentityService identityService;
    private final UserRepository userRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            upsertDemoUser("demo@example.com", "Demo User");
            upsertDemoUser("yousef@example.com", "Yousef");
        };
    }

    private void upsertDemoUser(String email, String fullName) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setFullName(fullName);

        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            existing.setFullName(fullName);
            existing.setPassword("password");
            existing.setStatus("ACTIVE");
            userRepository.save(existing);
            System.out.println("User updated: " + email);
        }, () -> {
            try {
                identityService.register(request);
                System.out.println("User created: " + email);
            } catch (Exception e) {
                System.err.println("Failed to create user " + email + ": " + e.getMessage());
            }
        });
    }
}
