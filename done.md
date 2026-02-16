# Foody App – Final Result & File Guide

This document describes **what the app does today** and **what each file is for**. Use it to understand the project and explain it to others.

---

## What Is Foody?

**Foody** is a food-delivery web app with three roles:

- **Customer** – browses restaurants, adds to cart, places orders (with delivery address), tracks order with a progress bar, sees past orders.
- **Restaurant** – sees dashboard stats, manages menu (add/delete items), sees order counts.
- **Courier** – sees active orders, opens order details, marks “Picked up” and “Delivered”.

**Tech:** Spring Boot backend (Java, REST APIs, database) + static HTML and JavaScript frontend (no React/Vue).

---

## What Works Today (Final Results)

### Customer flow

- **Home** (`/` or `/roles`): Choose Customer, Restaurant, or Courier. If already logged in (any role), header shows **profile + Log out** instead of Login.
- **Browse** (`/restaurants`): List of restaurants from the API. If logged in as customer → **profile + Log out**; otherwise “Sign In”.
- **Menu** (`/Menu?restaurantId=...`): Restaurant name and menu; add to cart (stored in `localStorage`). Cart shows count and total; link to checkout.
- **Checkout** (`/checkout`): Cart summary + **required delivery address**. “Place order” sends address and items to the API; redirect to order tracking.
- **Order tracking** (`/orderTrack?orderId=...`): **Progress bar** (Preparing → Delivering → Delivered), **delivery address**, **real order items and total** from the API, courier info. No map image.
- **Past orders** (`/pastOrders`): List of orders for the current client. “Track” and “Reorder”. If logged in → **profile + Log out**; otherwise “Login”.

### Restaurant flow

- **Login** (`/restaurant/login` → `/login?role=restaurant`): Email/password; on success stores `foodyRestaurantId` and redirects to `/myRestaurant`. **No Login button once logged in** – header shows **profile + Log out**.
- **Dashboard** (`/myRestaurant`): Restaurant name and stats (today’s revenue, pending orders, menu count) from the API.
- **Menu management** (`/myMenu`): Table of menu items from API; “Add New Item” (prompt), “Delete” per row. Sidebar has **Log out**.
- **Orders** (`/all_orders`): Pending count from API. Header has **Log out**.

### Courier flow

- **Login** (`/delivery/login` → `/login?role=delivery`): On success stores `foodyDriverId`, redirects to `/myDelivery`. **Profile + Log out** everywhere (sidebar or header).
- **Dashboard** (`/myDelivery`): Active orders from API as cards; each links to `/orderDetails?orderId=...`. Sidebar has **Log out**.
- **Order details** (`/orderDetails?orderId=...`): Order info; **“Confirm Pickup”** and **“Mark Delivered”** call the API to update status. Header has **Log out**.

### Auth and header behavior

- **One login page** (`/login`), **one signup page** (`/signup`), with `?role=customer|restaurant|delivery`.
- Once logged in, **Login/Sign In is hidden** and **profile + Log out** is shown (role selection, client pages, restaurant and delivery pages). **Log out** clears the stored ID and redirects to login.

---

## Project Structure: What Each File Does

### Backend (Java – `src/main/java/com/example/demo/`)

**Application & config**

| File | What it does |
|------|----------------|
| `FoodyApplication.java` | Spring Boot entry point. |
| `UserTestLoader.java` | Loads demo users/data on startup (if used). |

**Entities (database tables)**

| File | What it holds |
|------|----------------|
| `Entities/User.java` | Login: id, email, password, role. |
| `Entities/Client.java` | Customer: id, name, phone, address, user. |
| `Entities/Restaurant.java` | Restaurant: id, name, description, address, open, user. |
| `Entities/Driver.java` | Courier: id, user (and any driver-specific fields). |
| `Entities/MenuItem.java` | Dish: id, name, description, price, available, restaurant. |
| `Entities/Order.java` | Order: id, client, restaurant, driver, status, totalPrice, createdAt, **deliveryAddress**. |
| `Entities/OrderItem.java` | Order line: id, order, menuItem, quantity, priceAtOrderTime. |
| `Entities/OrderStatus.java` | Enum: CREATED, ACCEPTED, PREPARING, PICKED_UP, DELIVERED, CANCELLED. |
| `Entities/Role.java` | User role enum. |

**Repositories (database access)**

| File | What it does |
|------|----------------|
| `Repositories/UserRepository.java` | Find/save User by id, email. |
| `Repositories/ClientRepository.java` | Find/save Client. |
| `Repositories/RestaurantRepository.java` | Find/save Restaurant. |
| `Repositories/DriverRepository.java` | Find/save Driver. |
| `Repositories/MenuItemRepository.java` | Find/save MenuItem, e.g. by restaurant. |
| `Repositories/OrderRepository.java` | Find/save Order; by client, restaurant, driver, status. |
| `Repositories/OrderItemRepository.java` | Find/save OrderItem, e.g. by order id. |

**Services (business logic)**

| File | What it does |
|------|----------------|
| `service/AuthService.java` | Login/signup for client, restaurant, driver; creates User + role entity. |
| `service/ClientService.java` | Client-related logic. |
| `service/RestaurantService.java` | Restaurant + menu item CRUD (create/update/delete menu items). |
| `service/DriverService.java` | Driver-related logic. |
| `service/OrderService.java` | Create order (with deliveryAddress), find by id/client/restaurant/driver, update status, get order items. |

**DTOs (data sent/received by API)**

| File | What it holds |
|------|----------------|
| `dto/LoginRequest.java` | Email, password (login body). |
| `dto/LoginResponse.java` | Id (and maybe role) returned after login. |
| `dto/SignupClientRequest.java` | Name, email, password, phone, address (client signup). |
| `dto/SignupRestaurantRequest.java` | Name, email, password, description, address (restaurant signup). |
| `dto/SignupDriverRequest.java` | Driver signup fields. |
| `dto/RestaurantDto.java` | Id, name, description, address, open. |
| `dto/MenuItemDto.java` | Id, name, description, price, available. |
| `dto/CreateOrderRequest.java` | clientId, restaurantId, **deliveryAddress**, items (menuItemId, quantity). |
| `dto/CreateOrderItemDto.java` | menuItemId, quantity. |
| `dto/OrderResponseDto.java` | id, status, totalPrice, createdAt, clientId, restaurantId, driverId, estimatedDeliveryAt, driverName, driverPhone, restaurantName, **deliveryAddress**. |
| `dto/OrderItemResponseDto.java` | menuItemId, **itemName**, quantity, **price** (for track page). |
| `dto/UpdateOrderStatusRequest.java` | status (e.g. PICKED_UP, DELIVERED). |
| `dto/CreateMenuItemRequest.java` | name, description, price, available. |
| `dto/RestaurantDashboardDto.java` | todayRevenue, pendingOrders, menuItemCount (and similar). |

**Controllers (REST API and pages)**

| File | What it does |
|------|----------------|
| `controller/PageController.java` | Serves HTML: maps `/`, `/roles`, `/login`, `/signup`, `/restaurants`, `/Menu`, `/checkout`, `/pastOrders`, `/orderTrack`, `/myRestaurant`, `/myMenu`, `/all_orders`, `/myDelivery`, `/orderDetails`, and redirects for `/restaurant/login`, `/delivery/login`, etc. |
| `controller/AuthController.java` | `POST /api/auth/client/login`, `.../client/signup`, `.../restaurant/login`, `.../restaurant/signup`, `.../driver/login`, `.../driver/signup`. |
| `controller/RestaurantController.java` | `GET /api/restaurants`, `GET /api/restaurants/{id}`, `GET /api/restaurants/{id}/menu`, `GET /api/restaurants/{id}/dashboard`, `GET /api/restaurants/{id}/orders`, `POST/PUT/DELETE /api/restaurants/{id}/menuItems/...`. |
| `controller/OrderController.java` | `POST /api/orders`, `GET /api/orders/{id}`, `GET /api/orders/{id}/items`, `GET /api/clients/{clientId}/orders`, `POST /api/orders/{id}/status`. |
| `controller/DriverController.java` | `GET /api/drivers/{id}/orders?status=ACTIVE` (and list of orders). |

---

### Frontend (HTML & JS – `src/main/resources/static/`)

**HTML pages (what you see)**

| File | URL(s) | What it is |
|------|--------|-------------|
| `user_role_selection.html` | `/`, `/roles` | Home: choose Customer / Restaurant / Courier. Header: Login or profile + Log out. |
| `login.html` | `/login` | One login form; role from `?role=`. |
| `signup.html` | `/signup` | One signup form; role from `?role=`. |
| `client_browse_restaurants.html` | `/restaurants` | Restaurant cards; header Sign In or profile + Log out. |
| `client_restaurant_menu_view.html` | `/Menu?restaurantId=...` | Menu cards, cart sidebar, checkout link. |
| `client_shopping_cart_and_checkout.html` | `/checkout` | Cart + **delivery address** field; “Place order”. |
| `client_order_tracking.html` | `/orderTrack?orderId=...` | **Progress bar** (Preparing / Delivering / Delivered), address, order summary, courier. |
| `client_past_orders.html` | `/pastOrders` | Table of orders; Track, Reorder. Header Login or profile + Log out. |
| `restaurant_dashboard.html` | `/myRestaurant` | Dashboard with stats; header profile + Log out. |
| `restaurant_menu_management.html` | `/myMenu` | Menu table, Add item, Delete; sidebar Log out. |
| `restaurant_order_management.html` | `/all_orders` | Order columns and pending count; header Log out. |
| `restaurant_views_ratings.html` | `/my_restaurant` | Restaurant views/ratings (optional). |
| `delivery_dashboard.html` | `/myDelivery` | Active order cards; sidebar Log out. |
| `delivery_order_details.html` | `/orderDetails?orderId=...` | Order details, Confirm Pickup, Mark Delivered; header Log out. |

**JavaScript (what makes pages work)**

| File | Used on | What it does |
|------|---------|----------------|
| `js/auth/auth-header.js` | Role selection, client browse/past orders, restaurant pages, delivery pages | Shows Login vs profile + Log out from `localStorage`; wires **Log out** (clear ID, redirect to login). |
| `js/auth/login-controller.js` | `login.html` | Sends login to the right API; stores `foodyClientId` / `foodyRestaurantId` / `foodyDriverId`; redirects. |
| `js/auth/signup-controller.js` | `signup.html` | Sends signup to the right API; redirects. |
| `js/client/browse-restaurants-controller.js` | Browse restaurants | Fetches `/api/restaurants`, renders cards; links to `/Menu?restaurantId=...`. |
| `js/client/menu-controller.js` | Menu page | Fetches restaurant + menu; cart in `localStorage` (foodyCart); checkout link. |
| `js/client/checkout-controller.js` | Checkout | Reads cart; **validates delivery address**; POSTs order with **deliveryAddress**; redirects to `/orderTrack?orderId=...`. |
| `js/client/order-track-controller.js` | Order tracking | Fetches order + items; **progress bar** (Preparing/Delivering/Delivered); fills address, items, total, courier; polls. |
| `js/client/past-orders-controller.js` | Past orders | Fetches `/api/clients/{id}/orders`; table with Track and Reorder. |
| `js/restaurant/dashboard-controller.js` | Restaurant dashboard | Fetches restaurant + dashboard API; fills name and stats. |
| `js/restaurant/menu-controller.js` | Menu management | Fetches menu; Add (prompt → POST), Delete (DELETE); uses `#menu-items-tbody`, `#menu-add-btn`. |
| `js/restaurant/orders-controller.js` | Restaurant orders | Fetches orders; updates `#pending-count`. |
| `js/delivery/dashboard-controller.js` | Delivery dashboard | Fetches active orders; fills `#delivery-orders-list` with cards linking to `/orderDetails?orderId=...`. |
| `js/delivery/order-details-controller.js` | Order details | Fetches order; wires “Confirm Pickup” and “Mark Delivered” to `POST /api/orders/{id}/status`. |

**Config**

| File | What it does |
|------|----------------|
| `application.yaml` | Spring Boot config (e.g. server port, datasource). |

---

## Main URLs (Quick Reference)

| Who | Entry and main URLs |
|-----|----------------------|
| **Customer** | `/` or `/roles` → `/restaurants` → `/Menu?restaurantId=...` → `/checkout` → `/orderTrack?orderId=...`, `/pastOrders` |
| **Restaurant** | `/restaurant/login` → `/myRestaurant`, `/myMenu`, `/all_orders` |
| **Courier** | `/delivery/login` → `/myDelivery`, `/orderDetails?orderId=...` |
| **Auth** | `/login`, `/signup` (use `?role=customer|restaurant|delivery`) |

---

## Summary in One Sentence

Foody is a full food-delivery app: customers browse, order with a **delivery address**, and track orders with a **progress bar** and real data; restaurants see a dashboard and manage their menu; couriers see active orders and update status; **once logged in, the header shows profile + Log out** everywhere instead of Login, and every role can log out from the current page.
