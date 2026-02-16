package com.example.demo.service;

import com.example.demo.Entities.*;
import com.example.demo.Repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    public AuthService(UserRepository userRepository,
                       ClientRepository clientRepository,
                       RestaurantRepository restaurantRepository,
                       DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.restaurantRepository = restaurantRepository;
        this.driverRepository = driverRepository;
    }

    // ---------- Login ----------

    public Optional<Client> loginClient(String email, String password) {
        return userRepository.findByEmailAndPasswordHash(email, password)
                .filter(u -> u.getRole() == Role.CLIENT)
                .flatMap(u -> clientRepository.findByUserId(u.getId()));
    }

    public Optional<Restaurant> loginRestaurant(String email, String password) {
        return userRepository.findByEmailAndPasswordHash(email, password)
                .filter(u -> u.getRole() == Role.RESTAURANT)
                .flatMap(u -> restaurantRepository.findByUserId(u.getId()));
    }

    public Optional<Driver> loginDriver(String email, String password) {
        return userRepository.findByEmailAndPasswordHash(email, password)
                .filter(u -> u.getRole() == Role.DRIVER)
                .flatMap(u -> driverRepository.findByUserId(u.getId()));
    }

    // ---------- Signup ----------

    @Transactional
    public Client signupClient(String email, String password, String name, String phone, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(password); // MVP: plaintext; use hashing later
        user.setRole(Role.CLIENT);
        user.setActive(true);
        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        client.setName(name);
        client.setPhone(phone);
        client.setAddress(address);
        return clientRepository.save(client);
    }

    @Transactional
    public Restaurant signupRestaurant(String email, String password, String name, String description, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole(Role.RESTAURANT);
        user.setActive(true);
        user = userRepository.save(user);

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
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole(Role.DRIVER);
        user.setActive(true);
        user = userRepository.save(user);

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setVehicleType(vehicleType != null ? vehicleType : "bike");
        driver.setAvailable(false);
        return driverRepository.save(driver);
    }
}
