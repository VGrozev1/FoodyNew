(function () {
    var CART_KEY = "foodyCart";
    var DELIVERY_FEE = 2.99;

    function getRestaurantIdFromUrl() {
        var params = new URLSearchParams(window.location.search);
        var id = params.get("restaurantId");
        return id ? parseInt(id, 10) : null;
    }

    function loadCart() {
        try {
            var raw = localStorage.getItem(CART_KEY);
            if (!raw) return null;
            return JSON.parse(raw);
        } catch (e) {
            return null;
        }
    }

    function saveCart(cart) {
        try {
            localStorage.setItem(CART_KEY, JSON.stringify(cart));
        } catch (e) {}
    }

    function initCartForRestaurant(restaurantId, restaurantName) {
        var cart = loadCart();
        if (cart && cart.restaurantId === restaurantId) return cart;
        return { restaurantId: restaurantId, restaurantName: restaurantName || "Restaurant", items: [] };
    }

    function cartSubtotal(cart) {
        if (!cart || !cart.items) return 0;
        return cart.items.reduce(function (sum, line) {
            return sum + line.price * (line.quantity || 1);
        }, 0);
    }

    function cartTotal(cart) {
        return cartSubtotal(cart) + DELIVERY_FEE;
    }

    function escapeHtml(text) {
        var div = document.createElement("div");
        div.textContent = text == null ? "" : text;
        return div.innerHTML;
    }

    function renderMenuCard(item, onAddToCart) {
        var available = item.available !== false;
        var price = item.price != null ? Number(item.price) : 0;
        var name = escapeHtml(item.name || "Item");
        var desc = escapeHtml((item.description || "").trim() ? item.description : "");
        var cardClass = "group rounded-xl bg-white dark:bg-surface-dark border border-border-light dark:border-gray-700 p-4 shadow-sm hover:shadow-md transition-all";
        if (!available) cardClass += " opacity-70";
        var addBtn = available
            ? '<button type="button" class="menu-add-btn flex items-center justify-center size-10 rounded-full bg-primary text-white hover:bg-primary/90 transition-colors" data-id="' + escapeHtml(String(item.id)) + '" data-name="' + escapeHtml(String(item.name)) + '" data-price="' + price + '" aria-label="Add to cart"><span class="material-symbols-outlined text-xl">add</span></button>'
            : '<span class="text-sm text-text-secondary dark:text-gray-500">Unavailable</span>';
        return (
            '<div class="' + cardClass + '" data-menu-item data-name="' + name + '" data-desc="' + desc + '">' +
            '  <div class="flex gap-4">' +
            '    <div class="flex-1 min-w-0">' +
            '      <h4 class="font-bold text-text-main dark:text-white truncate">' + name + '</h4>' +
            (desc ? '      <p class="text-sm text-text-secondary dark:text-gray-400 mt-1 line-clamp-2">' + desc + '</p>' : '') +
            '      <p class="mt-2 font-bold text-primary">$' + price.toFixed(2) + '</p>' +
            '    </div>' +
            '    <div class="flex items-center flex-none">' + addBtn + '</div>' +
            '  </div>' +
            '</div>'
        );
    }

    function updateCartUI(cart) {
        var cartList = document.getElementById("cart-list");
        var cartCountNum = document.getElementById("cart-count-num");
        var cartTotalEl = document.getElementById("cart-total");
        var headerCartCount = document.getElementById("header-cart-count");
        var mobileCartCount = document.getElementById("mobile-cart-count");
        var mobileCartTotal = document.getElementById("mobile-cart-total");
        var deliveryFeeEl = document.getElementById("delivery-fee");

        if (!cart || !cart.items || cart.items.length === 0) {
            if (cartList) cartList.innerHTML = '<p class="text-sm text-text-secondary dark:text-gray-400">Your cart is empty.</p>';
            if (cartCountNum) cartCountNum.textContent = "0";
            if (cartTotalEl) cartTotalEl.textContent = "$" + DELIVERY_FEE.toFixed(2);
            if (headerCartCount) headerCartCount.textContent = "0";
            if (mobileCartCount) mobileCartCount.textContent = "0 items";
            if (mobileCartTotal) mobileCartTotal.textContent = "$0.00";
            if (deliveryFeeEl) deliveryFeeEl.textContent = "$" + DELIVERY_FEE.toFixed(2);
            return;
        }

        var total = cartTotal(cart);
        var count = cart.items.reduce(function (n, line) { return n + (line.quantity || 1); }, 0);

        if (cartList) {
            cartList.innerHTML = cart.items.map(function (line, index) {
                var name = escapeHtml(line.name || "Item");
                var price = (line.price != null ? Number(line.price) : 0) * (line.quantity || 1);
                return (
                    '<div class="flex gap-3">' +
                    '  <div class="flex-1 min-w-0">' +
                    '    <div class="flex justify-between gap-2">' +
                    '      <h4 class="font-semibold text-sm text-text-main dark:text-white">' + name + (line.quantity > 1 ? ' × ' + line.quantity : '') + '</h4>' +
                    '      <span class="font-semibold text-sm text-text-main dark:text-white">$' + price.toFixed(2) + '</span>' +
                    '    </div>' +
                    '    <button type="button" class="cart-remove text-xs text-text-secondary hover:text-red-500 mt-1" data-index="' + index + '">Remove</button>' +
                    '  </div>' +
                    '</div>'
                );
            }).join("");
            cartList.querySelectorAll(".cart-remove").forEach(function (btn) {
                btn.addEventListener("click", function () {
                    var idx = parseInt(btn.getAttribute("data-index"), 10);
                    cart.items.splice(idx, 1);
                    saveCart(cart);
                    updateCartUI(cart);
                });
            });
        }

        if (cartCountNum) cartCountNum.textContent = String(count);
        if (cartTotalEl) cartTotalEl.textContent = "$" + total.toFixed(2);
        if (headerCartCount) headerCartCount.textContent = String(count);
        if (mobileCartCount) mobileCartCount.textContent = count + " item" + (count === 1 ? "" : "s");
        if (mobileCartTotal) mobileCartTotal.textContent = "$" + total.toFixed(2);
        if (deliveryFeeEl) deliveryFeeEl.textContent = "$" + DELIVERY_FEE.toFixed(2);
    }

    document.addEventListener("DOMContentLoaded", function () {
        var restaurantId = getRestaurantIdFromUrl();
        if (!restaurantId) {
            window.location.href = "/restaurants";
            return;
        }

        var menuLoading = document.getElementById("menu-loading");
        var menuError = document.getElementById("menu-error");
        var menuItems = document.getElementById("menu-items");
        var restaurantNameEl = document.getElementById("restaurant-name");
        var pageTitle = document.getElementById("page-title");

        function showError(msg) {
            if (menuLoading) menuLoading.classList.add("hidden");
            if (menuItems) menuItems.classList.add("hidden");
            if (menuError) {
                menuError.textContent = msg || "Failed to load menu.";
                menuError.classList.remove("hidden");
            }
        }

        function setRestaurantName(name) {
            var n = name || "Menu";
            if (restaurantNameEl) restaurantNameEl.textContent = n;
            if (pageTitle) pageTitle.textContent = n;
            document.title = "Foody – " + n;
        }

        var cart = initCartForRestaurant(restaurantId, null);
        updateCartUI(cart);

        Promise.all([
            fetch("/api/restaurants/" + restaurantId).then(function (r) { return r.ok ? r.json() : null; }),
            fetch("/api/restaurants/" + restaurantId + "/menu").then(function (r) {
                if (!r.ok) throw new Error("Menu not found");
                return r.json();
            })
        ]).then(function (results) {
            var restaurant = results[0];
            var list = Array.isArray(results[1]) ? results[1] : [];
            if (restaurant && restaurant.name) {
                setRestaurantName(restaurant.name);
                cart.restaurantName = restaurant.name;
                saveCart(cart);
            }
            if (menuLoading) menuLoading.classList.add("hidden");
            if (menuError) menuError.classList.add("hidden");
            if (!menuItems) return;
            menuItems.classList.remove("hidden");
            if (list.length === 0) {
                menuItems.innerHTML = '<p class="col-span-full text-text-secondary dark:text-gray-400 py-8">No menu items yet.</p>';
                return;
            }
            menuItems.innerHTML = list.map(function (item) { return renderMenuCard(item); }).join("");
            menuItems.querySelectorAll(".menu-add-btn").forEach(function (btn) {
                btn.addEventListener("click", function () {
                    var id = btn.getAttribute("data-id");
                    var name = btn.getAttribute("data-name");
                    var price = parseFloat(btn.getAttribute("data-price"), 10) || 0;
                    var existing = cart.items.find(function (line) { return String(line.id) === String(id); });
                    if (existing) existing.quantity = (existing.quantity || 1) + 1;
                    else cart.items.push({ id: id, name: name, price: price, quantity: 1 });
                    saveCart(cart);
                    updateCartUI(cart);
                });
            });
        }).catch(function (err) {
            if (menuError) {
                menuError.innerHTML = (err.message || "Could not load menu.") + ' Go back to <a href="/restaurants" class="text-primary underline">restaurants</a>.';
                menuError.classList.remove("hidden");
            }
            if (menuLoading) menuLoading.classList.add("hidden");
            if (menuItems) menuItems.classList.add("hidden");
        });

        var searchInput = document.getElementById("menu-search");
        if (searchInput) {
            searchInput.addEventListener("input", function () {
                var val = (searchInput.value || "").toLowerCase().trim();
                var cards = document.querySelectorAll("#menu-items [data-menu-item]");
                cards.forEach(function (card) {
                    var name = (card.getAttribute("data-name") || "").toLowerCase();
                    var desc = (card.getAttribute("data-desc") || "").toLowerCase();
                    var show = !val || name.indexOf(val) !== -1 || desc.indexOf(val) !== -1;
                    card.style.display = show ? "" : "none";
                });
            });
        }
    });
})();
