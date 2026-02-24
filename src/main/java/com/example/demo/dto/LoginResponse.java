package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String role; // "CLIENT", "RESTAURANT", "DRIVER"
    private String token;

    public LoginResponse(Long id, String role) {
        this.id = id;
        this.role = role;
    }
}
