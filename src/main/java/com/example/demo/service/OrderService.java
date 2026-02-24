package com.example.demo.service;

import com.example.demo.Entities.Client;
import com.example.demo.Entities.Driver;
import com.example.demo.Entities.MenuItem;
import com.example.demo.Entities.Order;
import com.example.demo.Entities.OrderItem;
import com.example.demo.Entities.OrderStatus;
import com.example.demo.Entities.Restaurant;
import com.example.demo.Repositories.ClientRepository;
import com.example.demo.Repositories.DriverRepository;
import com.example.demo.Repositories.MenuItemRepository;
import com.example.demo.Repositories.OrderItemRepository;
import com.example.demo.Repositories.OrderRepository;
import com.example.demo.Repositories.RestaurantRepository;
import com.example.demo.dto.CreateOrderItemDto;
import com.example.demo.dto.CreateOrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ClientRepository clientRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final DriverRepository driverRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ClientRepository clientRepository,
                        RestaurantRepository restaurantRepository,
                        MenuItemRepository menuItemRepository,
                        DriverRepository driverRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.clientRepository = clientRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        if (request == null || request.getClientId() == null || request.getRestaurantId() == null) {
            throw new IllegalArgumentException("Invalid order request");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Order order = new Order();
        order.setClient(client);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.CREATED);
        order.setDeliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress().trim() : "");

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> itemsToPersist = new ArrayList<>();
        for (CreateOrderItemDto itemDto : request.getItems()) {
            if (itemDto == null || itemDto.getMenuItemId() == null || itemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid order item");
            }
            MenuItem menuItem = menuItemRepository.findById(itemDto.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("Menu item does not belong to selected restaurant");
            }

            BigDecimal linePrice = menuItem.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            total = total.add(linePrice);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPriceAtOrderTime(menuItem.getPrice());
            itemsToPersist.add(orderItem);
        }

        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(itemsToPersist);
        return savedOrder;
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

    public List<Order> findActiveByDriverId(Long driverId) {
        return orderRepository.findByDriverIdAndStatusIn(driverId, activeStatuses());
    }

    public List<Order> findActiveOrders() {
        return orderRepository.findByStatusIn(activeStatuses());
    }

    @Transactional
    public Optional<Order> updateStatus(Long orderId, OrderStatus status) {
        return updateStatus(orderId, status, null);
    }

    @Transactional
    public Optional<Order> updateStatus(Long orderId, OrderStatus status, Long driverId) {
        Optional<Order> opt = orderRepository.findById(orderId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Order order = opt.get();
        if (driverId != null && (status == OrderStatus.ACCEPTED || status == OrderStatus.PICKED_UP)) {
            Optional<Driver> driver = driverRepository.findById(driverId);
            if (driver.isPresent()) {
                order.setDriver(driver.get());
            }
        }
        order.setStatus(status);
        return Optional.of(orderRepository.save(order));
    }

    private List<OrderStatus> activeStatuses() {
        return List.of(OrderStatus.CREATED, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.PICKED_UP);
    }
}
