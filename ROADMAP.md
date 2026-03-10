## Foody App Roadmap

Status legend: ✅ = completed, 🔜 = planned / to do

---

### Phase 1 – Navigation & page wiring

- ✅ **1.1 Normalize routes and headers (core flows)**
  - Home/roles mapped to `/` and `/roles` via `PageController`.
  - Role cards on `roles` page navigate to:
    - Customer → `/restaurants`
    - Restaurant → `/restaurant/login`
    - Courier → `/delivery/login`
  - Main client pages wired: `/login`, `/signup`, `/restaurants`, `/checkout`, `/pastOrders`, `/orderTrack`.
- done **1.2 Complete back/forward links between all client pages**
  - From browse restaurants → menu → cart → tracking → back to browse/orders.
  - Past orders entries link to reorder and track (e.g. `/orderTrack?orderId=...`).
- ✅ **1.3 Clean up or replace dead links**
  - Replace `href="#"` in headers/footers with real routes (or remove).
  - Ensure all header nav items on every page point to valid routes.

---

### Phase 2 – Back-end domain & API structure

- ✅ **2.1 JPA entities and repositories**
  - Entities: `Client`, `Restaurant`, `Driver`, `MenuItem`, `Order`, `OrderItem`, `User`.
  - Repositories: `ClientRepository`, `RestaurantRepository`, `DriverRepository`, `MenuItemRepository`, `OrderRepository`, `OrderItemRepository`, `UserRepository`.
- ✅ **2.2 Service layer**
  - Create services to encapsulate DB logic:
    - `AuthService` (login/signup for all roles).
    - `ClientService`, `RestaurantService`, `DriverService`.
    - `OrderService` (create orders, load orders, update status).
- ✅ **2.3 REST controllers (JSON APIs)**
  - `AuthController` (`/api/auth/...`):
    - `POST /api/auth/client/login`
    - `POST /api/auth/client/signup`
    - `POST /api/auth/restaurant/login`
    - `POST /api/auth/restaurant/signup`
    - `POST /api/auth/driver/login`
    - `POST /api/auth/driver/signup`
  - `RestaurantController`:
    - `GET /api/restaurants`
    - `GET /api/restaurants/{id}/menu`
  - `OrderController`:
    - `POST /api/orders`
    - `GET /api/orders/{id}`
    - `GET /api/clients/{clientId}/orders`
  - `PageController` only for page routing/forwards (stub `/api/login` removed).

---

### Phase 3 – DB writes for auth & signup

- ✅ **3.1 Client signup (`/signup`)**
  - Frontend:
    - Implement JS (`Signup-contoller.js`) to collect form data.
    - `fetch('POST /api/auth/client/signup', { body: JSON.stringify(...) })`.
    - On success, redirect to `/login` or `/restaurants`.
  - Backend:
    - Validate input (email, password, required fields).
    - Create `Client` (and possibly `User`), call `clientRepository.save(...)`.
    - Enforce email uniqueness.
- ✅ **3.2 Restaurant signup (`/restaurant/signup`)**
  - Frontend:
    - Add JS to POST to `/api/auth/restaurant/signup`.
    - On success, redirect to `/restaurant/login` or `/myRestaurant`.
  - Backend:
    - Persist `Restaurant` (and `User`) via `restaurantRepository.save(...)`.
    - Store contact + credentials.
- ✅ **3.3 Courier signup (`/delivery/signup`)**
  - Frontend:
    - Add JS to POST to `/api/auth/driver/signup`.
    - On success, redirect to `/delivery/login` or `/myDelivery`.
  - Backend:
    - Persist `Driver` (and `User`) via `driverRepository.save(...)`.
- ✅ **3.4 Logins (all roles)**
  - Frontend:
    - `client_login.html`, `restaurant_login.html`, `deliery_login.html` send JSON to role-specific login endpoints.
    - On success, redirect:
      - Client → `/restaurants`
      - Restaurant → `/myRestaurant`
      - Courier → `/myDelivery`
  - Backend:
    - Verify credentials against DB.
    - For MVP: plaintext passwords; later replace with hashing.

---

### Phase 4 – DB-backed ordering flow

- ✅ **4.1 Browse restaurants (`/restaurants`)**
  - Backend: `GET /api/restaurants` returns list of restaurants (already in place from Phase 2).
  - Frontend:
    - In `browse-restaurants-controller.js`, fetch restaurant list from `/api/restaurants` on load; render cards (name, description, open/closed). Count shown in “Restaurants Nearby”.
    - On card click, navigate to `/Menu?restaurantId=...`. Favorite button does not trigger navigation.
- ✅ **4.2 Menu page (`/Menu`)**
  - Backend: `GET /api/restaurants/{id}/menu` returns menu items; added `GET /api/restaurants/{id}` for restaurant name.
  - Frontend:
      - `menu-controller.js` reads `restaurantId` from query; fetches restaurant + menu; renders name and menu cards (name, description, price; unavailable items shown but not addable).
    - Add to cart: items in memory and `localStorage` key `foodyCart` (per-restaurant; cart has `restaurantId`, `restaurantName`, `items` with `id`, `name`, `price`, `quantity`). Sidebar + mobile bar show count and total (+ $2.99 delivery); checkout link to `/checkout`. Search filters menu cards by name/description.
- ✅ **4.3 Cart & checkout (`/checkout`)**
  - Backend: POST /api/orders creates Order + OrderItems (already in place).
  - Frontend: checkout-controller.js reads cart from localStorage; Place order POSTs clientId, restaurantId, items; on 201 redirects to /orderTrack?orderId=...
- ✅ **4.4 Order tracking (`/orderTrack`)**
  - Backend: GET /api/orders/{id} returns OrderResponseDto with estimatedDeliveryAt, driverName, driverPhone, restaurantName.
  - Frontend: order-track-controller.js reads orderId from query; polls API; updates status, ETA, timeline steps, courier.
- ✅ **4.5 Past orders (`/pastOrders`)**
  - Backend: GET /api/clients/{clientId}/orders; GET /api/orders/{id}/items for Reorder; OrderResponseDto includes restaurantName.
  - Frontend: past-orders-controller.js fetches orders by clientId; table with Track and Reorder (POST new order from items, redirect to track).

---

### Phase 5 – Role dashboards (restaurant & courier)

- ✅ **5.1 Restaurant dashboard & tools**
  - Pages: `/myRestaurant`, `/myMenu`, `/all_orders`, `/my_restaurant`.
  - Backend:
    - `GET /api/restaurants/{id}/dashboard` (key stats, charts).
    - `GET /api/restaurants/{id}/orders?status=...`.
    - `GET/POST/PUT/DELETE /api/restaurants/{id}/menuItems`.
  - Frontend:
    - Dashboard: `dashboard-controller.js` loads restaurant name, dashboard stats (today revenue, pending orders, menu count).
    - Menu: `menu-controller.js` loads menu from API, Add New Item (prompt), Delete per item.
    - Orders: `orders-controller.js` updates pending count from API. Login stores `foodyRestaurantId`.
- ✅ **5.2 Courier dashboard**
  - Pages: `/myDelivery`, `/orderDetails`.
  - Backend:
    - `GET /api/drivers/{id}/orders?status=ACTIVE`.
    - Reuse `GET /api/orders/{id}`.
    - `POST /api/orders/{id}/status` to mark picked-up/delivered.
  - Frontend:
    - `dashboard-controller.js` loads active orders into `#delivery-orders-list`; cards link to `/orderDetails?orderId=...`.
    - `order-details-controller.js` loads order by id, wires “Mark Picked Up” / “Mark Delivered” to `POST /api/orders/{id}/status`. Login stores `foodyDriverId`.

---

### Phase 6 – UI cleanup & structure

- ✅ **6.1 Identify and rationalize duplicate pages**
  - Signup page: Customer uses form on `/signup`; Restaurant and Courier cards are direct links to `/restaurant/signup` and `/delivery/signup` (no dead buttons).
- ✅ **6.2 Normalize JS controllers**
  - Fixed typos: `Signup-contoller.js` → `signup-controller.js`, `client_order_track-crontoller.js` → `order-track-controller.js`.
  - Grouped scripts: `js/auth/signup-controller.js`, `js/auth/login-controller.js`, `js/client/browse-restaurants-controller.js`, `js/client/order-track-controller.js`. All loaded with `/js/...` paths.
- 🔜 **6.3 Optional: move heavy pages to templates**
  - For pages with lots of duplication, consider moving from pure static HTML to Thymeleaf templates under `src/main/resources/templates/`.
- ✅ **6.4 Clean up duplicate frontend pages**
  - Consolidated three login pages (client_login, restaurant_login, deliery_login) into one `login.html` with role from `?role=`. `/restaurant/login` and `/delivery/login` redirect to `/login?role=restaurant` and `/login?role=delivery`; one form submits to the correct API and redirects by role.
  - Consolidated three signup pages (client_sign_up, restaurant_signup, delivery_signup) into one `signup.html` with role from `?role=`. `/restaurant/signup` and `/delivery/signup` redirect to `/signup?role=restaurant` and `/signup?role=delivery`; one form with role-specific fields submits to the correct signup API and redirects to the matching login.
  - Fixed filename typos: `restaurant_menu_managment.html` → `restaurant_menu_management.html`, `deliver_guy_dashboard.html` → `delivery_dashboard.html`. Updated PageController accordingly.

---

### Phase 7 – Validation, security, and developer experience

- 🔜 **7.1 Validation & error handling**
  - Add Bean Validation annotations to DTOs/entities.
  - Return structured error responses from APIs.
  - Show error messages in the UI (e.g. invalid login, signup validation).
- 🔜 **7.2 Authentication & authorization**
  - Introduce Spring Security:
    - Session-based login or JWT.
    - Protect role-specific routes and APIs by role (client/restaurant/driver).
- 🔜 **7.3 Developer experience & data seeding**
  - Enable Spring Boot DevTools for hot reload.
  - Add a `README` with:
    - How to run the app.
    - Example users / demo data.
    - Overview of main routes.
  - Seed DB with demo data using `data.sql` or a more complete `UserTestLoader`.

---

### Improvement suggestions (choose what to proceed with)

Pick any item below; we can implement it next. Each is marked **\*NEW\*** so you can easily find and decide.

- **\*NEW\*** **Email deliverability (DKIM / own domain)**  
  - Buy a cheap domain, add it in Brevo, set DKIM/DMARC DNS. Use `no-reply@yourdomain.com` as sender so Gmail/Yahoo/Microsoft accept mail and it doesn’t go to spam.

- **\*NEW\*** **Password hashing**  
  - Replace plaintext passwords with bcrypt (or Argon2) in `AuthService` and login checks. Store only hashes in DB.

- **\*NEW\*** **Secrets from environment only**  
  - Move DB URL/username/password and JWT secret out of `application.yaml` into env vars (e.g. `DATABASE_URL`, `JWT_SECRET`) so nothing sensitive is committed; document required vars in README.

- **\*NEW\*** **Remove unused SMTP config**  
  - Delete `spring.mail` from `application.yaml` (we use Brevo API now). Keeps config clean and avoids confusion.

- **\*NEW\*** **README for run + env**  
  - Add or update README: how to run locally (`./mvnw spring-boot:run`), required env vars (`BREVO_API_KEY`, `MAIL_FROM`, DB vars, JWT if moved), and optional Railway/Deploy notes.

- **\*NEW\*** **Structured API errors in one place**  
  - Ensure all controllers use the same error DTO and status codes; frontend shows a single “message” from API errors (login, signup, orders) for consistency.

- **\*NEW\*** **Rate limit signup / verify-email**  
  - Add simple rate limiting (e.g. per IP or per email) on signup and resend-verification to reduce abuse and Brevo usage.

- **\*NEW\*** **Optional: Thymeleaf for key pages**  
  - Convert one or two heavy static HTML pages (e.g. login, signup) to Thymeleaf templates for less duplication and easier i18n later.

- **\*NEW\*** **Health or readiness endpoint**  
  - Add `GET /actuator/health` (or a simple `/api/health`) for Railway/monitoring to check if the app is up and optionally DB connectivity.

- **\*NEW\*** **Stronger JWT secret in production**  
  - Load JWT secret from env (e.g. `JWT_SECRET`) with no default in production; keep a dev default only for local runs.

