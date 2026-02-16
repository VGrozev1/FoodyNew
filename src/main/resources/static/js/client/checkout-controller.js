(function () {
    var CART_KEY = "foodyCart";
    var CLIENT_ID_KEY = "foodyClientId";
    var DELIVERY_FEE = 2.99;

    function loadCart() {
        try {
            var raw = localStorage.getItem(CART_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    }

    function getClientId() {
        try {
            var id = localStorage.getItem(CLIENT_ID_KEY);
            return id ? parseInt(id, 10) : 1;
        } catch (e) {
            return 1;
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        var cart = loadCart();
        var emptyEl = document.getElementById("checkout-empty");
        var contentEl = document.getElementById("checkout-content");
        var cartItemsEl = document.getElementById("cart-items");
        var restaurantNameEl = document.getElementById("restaurant-name");
        var cartTotalEl = document.getElementById("cart-total");
        var placeBtn = document.getElementById("place-order-btn");

        if (!cart || !cart.items || cart.items.length === 0) {
            if (emptyEl) emptyEl.classList.remove("hidden");
            if (contentEl) contentEl.classList.add("hidden");
            return;
        }

        if (restaurantNameEl) restaurantNameEl.textContent = cart.restaurantName || "Restaurant";
        var subtotal = 0;
        cart.items.forEach(function (line) {
            var price = (line.price != null ? Number(line.price) : 0) * (line.quantity || 1);
            subtotal += price;
            var li = document.createElement("li");
            li.className = "flex justify-between items-start text-sm";
            li.innerHTML = "<span class=\"text-[#181411] dark:text-gray-200\">" + (line.quantity > 1 ? line.quantity + "× " : "") + (line.name || "Item") + "</span><span class=\"font-medium text-[#181411] dark:text-gray-200\">$" + price.toFixed(2) + "</span>";
            if (cartItemsEl) cartItemsEl.appendChild(li);
        });
        var total = subtotal + DELIVERY_FEE;
        if (cartTotalEl) cartTotalEl.textContent = "$" + total.toFixed(2);

        if (!placeBtn) return;
        var addressInput = document.getElementById("delivery-address");
        var addressError = document.getElementById("address-error");
        placeBtn.addEventListener("click", function () {
            var address = addressInput ? (addressInput.value || "").trim() : "";
            if (!address) {
                if (addressError) { addressError.classList.remove("hidden"); addressError.textContent = "Please enter your delivery address."; }
                return;
            }
            if (addressError) addressError.classList.add("hidden");
            placeBtn.disabled = true;
            placeBtn.textContent = "Placing order…";
            var clientId = getClientId();
            var body = {
                clientId: clientId,
                restaurantId: cart.restaurantId,
                deliveryAddress: address,
                items: cart.items.map(function (line) {
                    return {
                        menuItemId: typeof line.id === "number" ? line.id : parseInt(String(line.id), 10),
                        quantity: line.quantity || 1
                    };
                })
            };
            fetch("/api/orders", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            })
                .then(function (res) {
                    if (res.status === 201) return res.json();
                    if (res.status === 400) throw new Error("Invalid cart or restaurant.");
                    throw new Error("Order failed.");
                })
                .then(function (order) {
                    localStorage.removeItem(CART_KEY);
                    window.location.href = "/orderTrack?orderId=" + encodeURIComponent(order.id);
                })
                .catch(function (err) {
                    alert(err.message || "Could not place order. Try again.");
                    placeBtn.disabled = false;
                    placeBtn.textContent = "Place order";
                });
        });
    });
})();
