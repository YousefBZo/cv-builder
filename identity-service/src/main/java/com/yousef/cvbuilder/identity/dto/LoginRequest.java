package com.yousef.cvbuilder.identity.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
