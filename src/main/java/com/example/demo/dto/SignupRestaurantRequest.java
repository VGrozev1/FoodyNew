package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRestaurantRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 120, message = "Restaurant name is too long")
    private String name;

    @Size(max = 500, message = "Description is too long")
    private String description;

    @Size(max = 255, message = "Address is too long")
    private String address;
}
