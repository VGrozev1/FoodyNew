package com.example.demo.dto;

import lombok.Data;

@Data
public class SignupClientRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String address;
}
