package com.example.demo.service;

import com.example.demo.Entities.*;
import com.example.demo.Repositories.*;
import com.example.demo.dto.CreateOrderItemDto;
import com.example.demo.dto.CreateOrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ClientRepository clientRepository,
                        RestaurantRepository restaurantRepository,
                        MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.clientRepository = clientRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Order order = new Order();
        order.setClient(client);
        order.setRestaurant(restaurant);
        order.setDriver(null);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : "");
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderItemDto dto : request.getItems()) {
            if (dto.getQuantity() <= 0) continue;
            MenuItem menuItem = menuItemRepository.findById(dto.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + dto.getMenuItemId()));
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("Menu item does not belong to restaurant");
            }
            BigDecimal linePrice = menuItem.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
            total = total.add(linePrice);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenuItem(menuItem);
            item.setQuantity(dto.getQuantity());
            item.setPriceAtOrderTime(menuItem.getPrice());
            orderItemRepository.save(item);
        }
        order.setTotalPrice(total);
        return orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<Order> findByClientId(Long clientId) {
        return orderRepository.findByClientId(clientId);
    }

    public List<Order> findByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status) {
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, status);
    }

    public List<Order> findByDriverId(Long driverId) {
        return orderRepository.findByDriverId(driverId);
    }

    public List<Order> findActiveByDriverId(Long driverId) {
        return orderRepository.findByDriverIdAndStatusIn(driverId,
                java.util.List.of(OrderStatus.CREATED, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.PICKED_UP));
    }

    @Transactional
    public Optional<Order> updateStatus(Long orderId, OrderStatus status) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                });
    }
}
