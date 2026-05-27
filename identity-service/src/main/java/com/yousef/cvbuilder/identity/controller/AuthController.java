package com.yousef.cvbuilder.identity.controller;

import com.yousef.cvbuilder.identity.dto.LoginRequest;
import com.yousef.cvbuilder.identity.dto.RegisterRequest;
import com.yousef.cvbuilder.identity.entity.UserAccount;
import com.yousef.cvbuilder.identity.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IdentityService identityService;

    @PostMapping("/register")
    public ResponseEntity<UserAccount> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(identityService.register(request));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return authenticate(request.getEmail(), request.getPassword());
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginForm(@ModelAttribute LoginRequest request) {
        return authenticate(request.getEmail(), request.getPassword());
    }

    private ResponseEntity<String> authenticate(String email, String password) {
        try {
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body("Email and password are required");
            }

            UserAccount account = identityService.login(email, password);
            return ResponseEntity.ok("Login Successful! User ID: " + account.getId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
