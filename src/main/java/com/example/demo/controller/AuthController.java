package com.example.demo.controller;

import com.example.demo.Entities.*;
import com.example.demo.dto.*;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/client/login")
    public ResponseEntity<LoginResponse> loginClient(@RequestBody LoginRequest request) {
        return authService.loginClient(request.getEmail(), request.getPassword())
                .map(c -> ResponseEntity.ok(new LoginResponse(c.getId(), Role.CLIENT.name())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/client/signup")
    public ResponseEntity<LoginResponse> signupClient(@RequestBody SignupClientRequest request) {
        try {
            Client c = authService.signupClient(
                    request.getEmail(), request.getPassword(),
                    request.getName(), request.getPhone(), request.getAddress());
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(c.getId(), Role.CLIENT.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/restaurant/login")
    public ResponseEntity<LoginResponse> loginRestaurant(@RequestBody LoginRequest request) {
        return authService.loginRestaurant(request.getEmail(), request.getPassword())
                .map(r -> ResponseEntity.ok(new LoginResponse(r.getId(), Role.RESTAURANT.name())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/restaurant/signup")
    public ResponseEntity<LoginResponse> signupRestaurant(@RequestBody SignupRestaurantRequest request) {
        try {
            Restaurant r = authService.signupRestaurant(
                    request.getEmail(), request.getPassword(),
                    request.getName(), request.getDescription(), request.getAddress());
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(r.getId(), Role.RESTAURANT.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/driver/login")
    public ResponseEntity<LoginResponse> loginDriver(@RequestBody LoginRequest request) {
        return authService.loginDriver(request.getEmail(), request.getPassword())
                .map(d -> ResponseEntity.ok(new LoginResponse(d.getId(), Role.DRIVER.name())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/driver/signup")
    public ResponseEntity<LoginResponse> signupDriver(@RequestBody SignupDriverRequest request) {
        try {
            Driver d = authService.signupDriver(
                    request.getEmail(), request.getPassword(),
                    request.getVehicleType());
            return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(d.getId(), Role.DRIVER.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
