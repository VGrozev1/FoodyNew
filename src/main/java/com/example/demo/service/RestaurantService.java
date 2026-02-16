package com.example.demo.service;

import com.example.demo.Entities.MenuItem;
import com.example.demo.Entities.Restaurant;
import com.example.demo.Repositories.MenuItemRepository;
import com.example.demo.Repositories.RestaurantRepository;
import com.example.demo.dto.CreateMenuItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public List<Restaurant> findOpen() {
        return restaurantRepository.findByOpenTrue();
    }

    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Optional<Restaurant> findByUserId(Long userId) {
        return restaurantRepository.findByUserId(userId);
    }

    public List<MenuItem> getMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public List<MenuItem> getAvailableMenu(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    @Transactional
    public MenuItem createMenuItem(Long restaurantId, CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setName(request.getName() != null ? request.getName() : "");
        item.setDescription(request.getDescription() != null ? request.getDescription() : "");
        item.setPrice(request.getPrice() != null ? request.getPrice() : java.math.BigDecimal.ZERO);
        item.setAvailable(request.isAvailable());
        return menuItemRepository.save(item);
    }

    @Transactional
    public Optional<MenuItem> updateMenuItem(Long restaurantId, Long itemId, CreateMenuItemRequest request) {
        Optional<MenuItem> opt = menuItemRepository.findById(itemId);
        if (opt.isEmpty()) return Optional.empty();
        MenuItem item = opt.get();
        if (!item.getRestaurant().getId().equals(restaurantId))
            throw new IllegalArgumentException("Menu item does not belong to restaurant");
        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        item.setAvailable(request.isAvailable());
        return Optional.of(menuItemRepository.save(item));
    }

    @Transactional
    public boolean deleteMenuItem(Long restaurantId, Long itemId) {
        Optional<MenuItem> opt = menuItemRepository.findById(itemId);
        if (opt.isEmpty()) return false;
        if (!opt.get().getRestaurant().getId().equals(restaurantId)) return false;
        menuItemRepository.delete(opt.get());
        return true;
    }
}
