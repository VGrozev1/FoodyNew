package com.example.demo.dto;

import lombok.Data;

@Data
public class SignupDriverRequest {
    private String email;
    private String password;
    private String vehicleType;
}
