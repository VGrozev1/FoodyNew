package com.example.demo.service;

import com.example.demo.Entities.*;
import com.example.demo.Repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Encapsulates login and signup for all roles (client, restaurant, driver).
 * MVP: passwords stored as plaintext in passwordHash; replace with hashing later.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RestaurantRepository restaurantRepository;
    private final DriverRepository driverRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       ClientRepository clientRepository,
                       RestaurantRepository restaurantRepository,
                       DriverRepository driverRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.restaurantRepository = restaurantRepository;
        this.driverRepository = driverRepository;
        this.emailService = emailService;
    }

    // ---------- Login ----------

    @Transactional
    public Optional<Client> loginClient(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmailAndPasswordHash(normalizedEmail, password)
                .filter(User::isEmailVerified)
                .filter(u -> u.getRole() == Role.CLIENT)
                .map(this::ensureClientProfile);
    }

    @Transactional
    public Optional<Restaurant> loginRestaurant(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmailAndPasswordHash(normalizedEmail, password)
                .filter(User::isEmailVerified)
                .filter(u -> u.getRole() == Role.RESTAURANT)
                .map(this::ensureRestaurantProfile);
    }

    @Transactional
    public Optional<Driver> loginDriver(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmailAndPasswordHash(normalizedEmail, password)
                .filter(User::isEmailVerified)
                .filter(u -> u.getRole() == Role.DRIVER)
                .map(this::ensureDriverProfile);
    }

    // ---------- Signup ----------

    @Transactional
    public Client signupClient(String email, String password, String name, String phone, String address) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(password); // MVP: plaintext; use hashing later
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user.setEmailVerified(false);
        user = userRepository.save(user);
        issueVerificationCode(user);

        Client client = new Client();
        client.setUser(user);
        client.setName(name);
        client.setPhone(phone);
        client.setAddress(address);
        return clientRepository.save(client);
    }

    @Transactional
    public Restaurant signupRestaurant(String email, String password, String name, String description, String address) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(password);
        user.setRole(Role.RESTAURANT);
        user.setActive(true);
        user.setEmailVerified(false);
        user = userRepository.save(user);
        issueVerificationCode(user);

        Restaurant restaurant = new Restaurant();
        restaurant.setUser(user);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setAddress(address);
        restaurant.setOpen(false);
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Driver signupDriver(String email, String password, String vehicleType) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(password);
        user.setRole(Role.DRIVER);
        user.setActive(true);
        user.setEmailVerified(false);
        user = userRepository.save(user);
        issueVerificationCode(user);

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setVehicleType(vehicleType != null ? vehicleType : "bike");
        driver.setAvailable(false);
        return driverRepository.save(driver);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    public boolean isUnverifiedAccount(String email, String password, Role role) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmailAndPasswordHash(normalizedEmail, password)
                .filter(u -> u.getRole() == role)
                .filter(u -> !u.isEmailVerified())
                .isPresent();
    }

    @Transactional
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }
        issueVerificationCode(user);
    }

    @Transactional
    public boolean verifyEmailCode(String email, String code) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            return true;
        }
        if (user.getVerificationCode() == null || user.getVerificationCodeExpiresAt() == null) {
            throw new IllegalArgumentException("No verification code found. Please request a new code.");
        }
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired. Please request a new code.");
        }
        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
        return true;
    }

    private void issueVerificationCode(User user) {
        String code = generateCode();
        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.sendVerificationCode(user.getEmail(), code);
    }

    private String generateCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private Client ensureClientProfile(User user) {
        return clientRepository.findByUserId(user.getId()).orElseGet(() -> {
            Client c = new Client();
            c.setUser(user);
            c.setName(user.getEmail());
            c.setPhone("");
            c.setAddress("");
            return clientRepository.save(c);
        });
    }

    private Restaurant ensureRestaurantProfile(User user) {
        return restaurantRepository.findByUserId(user.getId()).orElseGet(() -> {
            Restaurant r = new Restaurant();
            r.setUser(user);
            r.setName(user.getEmail());
            r.setDescription("");
            r.setAddress("");
            r.setOpen(false);
            return restaurantRepository.save(r);
        });
    }

    private Driver ensureDriverProfile(User user) {
        return driverRepository.findByUserId(user.getId()).orElseGet(() -> {
            Driver d = new Driver();
            d.setUser(user);
            d.setVehicleType("bike");
            d.setAvailable(false);
            return driverRepository.save(d);
        });
    }
}
