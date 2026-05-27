package com.yousef.cvbuilder.identity.service;

import com.yousef.cvbuilder.identity.dto.RegisterRequest;
import com.yousef.cvbuilder.identity.entity.UserAccount;
import com.yousef.cvbuilder.identity.event.UserRegisteredEvent;
import com.yousef.cvbuilder.identity.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserAccount register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new RuntimeException("Email already registered!");
        });

        UserAccount account = UserAccount.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(request.getPassword()) // Note: Should be hashed via BCrypt in production
                .fullName(request.getFullName())
                .status("ACTIVE")
                .build();

        UserAccount saved = userRepository.save(account);

        // Publish event to Kafka so dependent services can consume it and create local profiles
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(saved.getId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .build();

        kafkaTemplate.send("user-registration-topic", saved.getId(), event);

        return saved;
    }

    public UserAccount login(String email, String password) {
        UserAccount account = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!account.getPassword().equals(password)) {
            throw new RuntimeException("Invalid credentials");
        }
        return account;
    }
}
