package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupClientRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name is too long")
    private String name;

    @NotBlank(message = "Phone is required")
    @Size(max = 30, message = "Phone is too long")
    private String phone;

    @Size(max = 255, message = "Address is too long")
    private String address;
}
