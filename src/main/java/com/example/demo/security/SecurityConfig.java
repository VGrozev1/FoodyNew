package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/roles",
                                "/login",
                                "/signup",
                                "/restaurant/login",
                                "/restaurant/signup",
                                "/delivery/login",
                                "/delivery/signup",
                                "/restaurants",
                                "/Menu",
                                "/checkout",
                                "/orderTrack",
                                "/pastOrders",
                                "/myRestaurant",
                                "/myMenu",
                                "/all_orders",
                                "/myDelivery",
                                "/orderDetails",
                                "/my_restaurant",
                                "/*.html",
                                "/js/**",
                                "/css/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants", "/api/restaurants/*", "/api/restaurants/*/menu").permitAll()

                        .requestMatchers("/api/restaurants/*/dashboard", "/api/restaurants/*/orders", "/api/restaurants/*/menuItems/**").hasRole("RESTAURANT")
                        .requestMatchers("/api/drivers/**").hasRole("DRIVER")
                        .requestMatchers("/api/clients/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/status").hasAnyRole("RESTAURANT", "DRIVER")
                        .requestMatchers("/api/orders/**").hasAnyRole("CLIENT", "RESTAURANT", "DRIVER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Unauthorized\",\"path\":\"" + jsonEscape(request.getRequestURI()) + "\"}");
                }).accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Forbidden\",\"path\":\"" + jsonEscape(request.getRequestURI()) + "\"}");
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
