package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping({"/", "/roles"})
    public String rolesPage() {
        return "forward:/user_role_selection.html";
    }

    @GetMapping("/my_restaurant")
    public String myRestaurantPage() {
        return "forward:/restaurant_views_ratings.html";
    }

    @GetMapping("/all_orders")
    public String PendingOrdersPage() {
        return "forward:/restaurant_order_management.html";
    }

    @GetMapping("/myMenu")
    public String MyMenuPage() {
        return "forward:/restaurant_menu_management.html";
    }

    @GetMapping("/myRestaurant")
    public String MyRestaurantPage() {
        return "forward:/restaurant_dashboard.html";
    }

    @GetMapping("/orderDetails")
    public String OrderDetailsPage() {
        return "forward:/delivery_order_details.html";
    }

    @GetMapping("/myDelivery")
    public String MyDeliveryPage() {
        return "forward:/delivery_dashboard.html";
    }

    @GetMapping("/checkout")
    public String checkoutPage() {
        return "forward:/client_shopping_cart_and_checkout.html";
    }

    @GetMapping("/Menu")
    public String MenuPage() {
        return "forward:/client_restaurant_menu_view.html";
    }

    @GetMapping("/pastOrders")
    public String PastOrdersPage() {
        return "forward:/client_past_orders.html";
    }

    @GetMapping("/orderTrack")
    public String OrderTrackPage() {
        return "forward:/client_order_tracking.html";
    }

    @GetMapping("/restaurants")
    public String restaurantsPage() {
        return "forward:/client_browse_restaurants.html";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "forward:/signup.html";
    }

    @GetMapping("/delivery/login")
    public String deliveryLoginPage() {
        return "redirect:/login?role=delivery";
    }

    @GetMapping("/delivery/signup")
    public String deliverySignupPage() {
        return "redirect:/signup?role=delivery";
    }

    @GetMapping("/restaurant/login")
    public String restaurantLoginPage() {
        return "redirect:/login?role=restaurant";
    }

    @GetMapping("/restaurant/signup")
    public String restaurantSignupPage() {
        return "redirect:/signup?role=restaurant";
    }
}

