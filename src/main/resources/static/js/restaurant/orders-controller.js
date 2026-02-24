(function () {
    var RESTAURANT_ID_KEY = "foodyRestaurantId";
    var ACTIVE = { CREATED: true, ACCEPTED: true, PREPARING: true, PICKED_UP: true };

    function getRestaurantId() {
        try {
            var raw = localStorage.getItem(RESTAURANT_ID_KEY);
            return raw ? parseInt(raw, 10) : 1;
        } catch (e) {
            return 1;
        }
    }

    function esc(v) {
        return String(v == null ? "" : v)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function formatMoney(v) {
        return "$" + Number(v || 0).toFixed(2);
    }

    function loadOrderItems(orderId) {
        return fetch("/api/orders/" + orderId + "/items")
            .then(function (r) { return r.ok ? r.json() : []; })
            .catch(function () { return []; });
    }

    function updateOrderStatus(orderId, status) {
        return fetch("/api/orders/" + orderId + "/status", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status: status })
        }).then(function (r) {
            if (!r.ok) throw new Error("Could not update order.");
            return r.json();
        });
    }

    function cardHtml(order) {
        var items = (order.items || []).map(function (it) {
            return "<li class=\"text-sm text-gray-800 dark:text-gray-200\"><strong>" + esc(it.quantity) + "x</strong> " + esc(it.menuItemName || "Item") + "</li>";
        }).join("");
        var actions = "";
        if (order.status === "CREATED") {
            actions = "<div class=\"flex gap-2 mt-3\">" +
                "<button data-order-id=\"" + esc(order.id) + "\" data-next-status=\"ACCEPTED\" class=\"restaurant-status-btn flex-1 bg-primary hover:bg-orange-600 text-white py-2 rounded-lg font-bold text-sm\">Accept</button>" +
                "<button data-order-id=\"" + esc(order.id) + "\" data-next-status=\"CANCELLED\" class=\"restaurant-status-btn px-4 border border-[#e6dfdb] dark:border-[#3e2c22] text-[#181411] dark:text-white py-2 rounded-lg font-bold text-sm\">Decline</button>" +
                "</div>";
        }
        return "<div class=\"flex flex-col gap-3 rounded-xl bg-white dark:bg-[#1a120d] p-5 shadow-sm border border-[#e6dfdb] dark:border-[#3e2c22]\">" +
            "<div class=\"flex items-center justify-between\">" +
            "<h4 class=\"text-lg font-bold\">#" + esc(order.id) + "</h4>" +
            "<span class=\"text-xs font-bold px-2 py-1 rounded bg-primary/10 text-primary\">" + esc(order.status) + "</span>" +
            "</div>" +
            "<p class=\"text-xs text-[#8a7260]\">Delivery: " + esc(order.deliveryAddress || "No address") + "</p>" +
            "<ul class=\"space-y-1\">" + (items || "<li class=\"text-sm text-gray-500\">No items</li>") + "</ul>" +
            "<div class=\"pt-2 border-t border-[#e6dfdb] dark:border-[#3e2c22] flex items-center justify-between\">" +
            "<span class=\"font-bold\">" + formatMoney(order.totalPrice) + "</span>" +
            "</div>" +
            actions +
            "</div>";
    }

    function renderBoard(orders) {
        var board = document.querySelector(".grid.grid-cols-1.md\\:grid-cols-2.xl\\:grid-cols-3.gap-6.h-full.min-h-\\[500px\\]");
        if (!board) return;

        var created = orders.filter(function (o) { return o.status === "CREATED"; });
        var kitchen = orders.filter(function (o) { return o.status === "ACCEPTED" || o.status === "PREPARING" || o.status === "PICKED_UP"; });
        var history = orders.filter(function (o) { return !ACTIVE[o.status]; });

        board.innerHTML =
            "<div class=\"flex flex-col gap-4 h-full\"><div class=\"flex items-center justify-between pb-2 border-b-2 border-primary\"><h3 class=\"text-lg font-bold\">New Orders <span class=\"ml-2 text-xs bg-primary text-white px-2 py-0.5 rounded-full\">" + created.length + "</span></h3></div>" +
            (created.length ? created.map(cardHtml).join("") : "<div class=\"rounded-xl bg-white dark:bg-[#1a120d] p-4 border border-[#e6dfdb] dark:border-[#3e2c22]\">No new orders.</div>") +
            "</div>" +
            "<div class=\"flex flex-col gap-4 h-full\"><div class=\"flex items-center justify-between pb-2 border-b-2 border-blue-500\"><h3 class=\"text-lg font-bold\">In Kitchen <span class=\"ml-2 text-xs bg-blue-100 text-blue-600 px-2 py-0.5 rounded-full\">" + kitchen.length + "</span></h3></div>" +
            (kitchen.length ? kitchen.map(cardHtml).join("") : "<div class=\"rounded-xl bg-white dark:bg-[#1a120d] p-4 border border-[#e6dfdb] dark:border-[#3e2c22]\">No in-progress orders.</div>") +
            "</div>" +
            "<div class=\"flex flex-col gap-4 h-full\"><div class=\"flex items-center justify-between pb-2 border-b-2 border-green-500\"><h3 class=\"text-lg font-bold\">History <span class=\"ml-2 text-xs bg-green-100 text-green-600 px-2 py-0.5 rounded-full\">" + history.length + "</span></h3></div>" +
            (history.length ? history.map(cardHtml).join("") : "<div class=\"rounded-xl bg-white dark:bg-[#1a120d] p-4 border border-[#e6dfdb] dark:border-[#3e2c22]\">No historical orders yet.</div>") +
            "</div>";

        Array.from(board.querySelectorAll(".restaurant-status-btn")).forEach(function (btn) {
            btn.addEventListener("click", function () {
                var orderId = btn.getAttribute("data-order-id");
                var nextStatus = btn.getAttribute("data-next-status");
                updateOrderStatus(orderId, nextStatus).then(loadOrders).catch(function (e) {
                    alert(e.message || "Update failed");
                });
            });
        });
    }

    function loadOrders() {
        var restaurantId = getRestaurantId();
        fetch("/api/restaurants/" + restaurantId + "/orders")
            .then(function (r) { return r.ok ? r.json() : []; })
            .then(function (orders) {
                return Promise.all(orders.map(function (o) {
                    return loadOrderItems(o.id).then(function (items) {
                        o.items = items || [];
                        return o;
                    });
                }));
            })
            .then(renderBoard)
            .catch(function () {});
    }

    document.addEventListener("DOMContentLoaded", loadOrders);
})();
