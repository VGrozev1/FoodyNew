package com.example.demo.controller;

import com.example.demo.Entities.*;
import com.example.demo.dto.ApiErrorResponse;
import com.example.demo.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.example.demo.security.JwtService;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/client/login")
    public ResponseEntity<?> loginClient(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.loginClient(request.getEmail(), request.getPassword())
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(new LoginResponse(c.getId(), Role.CLIENT.name(), issueToken(request.getEmail()))))
                .orElseGet(() -> authService.isUnverifiedAccount(request.getEmail(), request.getPassword(), Role.CLIENT)
                        ? forbidden("Please verify your email before logging in", httpRequest)
                        : unauthorized("Invalid email or password", httpRequest));
    }

    @PostMapping("/client/signup")
    public ResponseEntity<?> signupClient(@Valid @RequestBody SignupClientRequest request, HttpServletRequest httpRequest) {
        try {
            Client c = authService.signupClient(
                    request.getEmail(), request.getPassword(),
                    request.getName(), request.getPhone(), request.getAddress());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new SignupResponse(c.getId(), Role.CLIENT.name(), true, request.getEmail(), "Verification code sent to email")
            );
        } catch (IllegalArgumentException e) {
            return isConflictMessage(e.getMessage())
                    ? conflict(e.getMessage(), httpRequest)
                    : badRequest(e.getMessage(), httpRequest);
        }
    }

    @PostMapping("/restaurant/login")
    public ResponseEntity<?> loginRestaurant(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.loginRestaurant(request.getEmail(), request.getPassword())
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(new LoginResponse(r.getId(), Role.RESTAURANT.name(), issueToken(request.getEmail()))))
                .orElseGet(() -> authService.isUnverifiedAccount(request.getEmail(), request.getPassword(), Role.RESTAURANT)
                        ? forbidden("Please verify your email before logging in", httpRequest)
                        : unauthorized("Invalid email or password", httpRequest));
    }

    @PostMapping("/restaurant/signup")
    public ResponseEntity<?> signupRestaurant(@Valid @RequestBody SignupRestaurantRequest request, HttpServletRequest httpRequest) {
        try {
            Restaurant r = authService.signupRestaurant(
                    request.getEmail(), request.getPassword(),
                    request.getName(), request.getDescription(), request.getAddress());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new SignupResponse(r.getId(), Role.RESTAURANT.name(), true, request.getEmail(), "Verification code sent to email")
            );
        } catch (IllegalArgumentException e) {
            return isConflictMessage(e.getMessage())
                    ? conflict(e.getMessage(), httpRequest)
                    : badRequest(e.getMessage(), httpRequest);
        }
    }

    @PostMapping("/driver/login")
    public ResponseEntity<?> loginDriver(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.loginDriver(request.getEmail(), request.getPassword())
                .<ResponseEntity<?>>map(d -> ResponseEntity.ok(new LoginResponse(d.getId(), Role.DRIVER.name(), issueToken(request.getEmail()))))
                .orElseGet(() -> authService.isUnverifiedAccount(request.getEmail(), request.getPassword(), Role.DRIVER)
                        ? forbidden("Please verify your email before logging in", httpRequest)
                        : unauthorized("Invalid email or password", httpRequest));
    }

    @PostMapping("/driver/signup")
    public ResponseEntity<?> signupDriver(@Valid @RequestBody SignupDriverRequest request, HttpServletRequest httpRequest) {
        try {
            Driver d = authService.signupDriver(
                    request.getEmail(), request.getPassword(),
                    request.getVehicleType());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new SignupResponse(d.getId(), Role.DRIVER.name(), true, request.getEmail(), "Verification code sent to email")
            );
        } catch (IllegalArgumentException e) {
            return isConflictMessage(e.getMessage())
                    ? conflict(e.getMessage(), httpRequest)
                    : badRequest(e.getMessage(), httpRequest);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request, HttpServletRequest httpRequest) {
        try {
            authService.verifyEmailCode(request.getEmail(), request.getCode());
            return ResponseEntity.ok(new ApiErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.OK.value(),
                    HttpStatus.OK.getReasonPhrase(),
                    "Email verified successfully",
                    null,
                    httpRequest.getRequestURI()
            ));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage(), httpRequest);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request, HttpServletRequest httpRequest) {
        try {
            authService.resendVerificationCode(request.getEmail());
            return ResponseEntity.ok(new ApiErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.OK.value(),
                    HttpStatus.OK.getReasonPhrase(),
                    "Verification code sent",
                    null,
                    httpRequest.getRequestURI()
            ));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage(), httpRequest);
        }
    }

    private ResponseEntity<ApiErrorResponse> unauthorized(String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    private ResponseEntity<ApiErrorResponse> conflict(String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                message != null ? message : "Conflict",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    private ResponseEntity<ApiErrorResponse> forbidden(String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                message != null ? message : "Forbidden",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    private ResponseEntity<ApiErrorResponse> badRequest(String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message != null ? message : "Bad request",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String issueToken(String email) {
        return authService.findByEmail(email)
                .filter(User::isActive)
                .map(jwtService::generateToken)
                .orElse(null);
    }

    private boolean isConflictMessage(String message) {
        if (message == null) return false;
        return message.toLowerCase().contains("already registered");
    }
}
