package com.example.demo.dto;

import lombok.Data;

@Data
public class SignupRestaurantRequest {
    private String email;
    private String password;
    private String name;
    private String description;
    private String address;
}
