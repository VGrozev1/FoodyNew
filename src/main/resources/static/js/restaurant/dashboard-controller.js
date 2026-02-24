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

    function statusChip(status) {
        if (status === "CREATED") return "<span class=\"bg-primary/10 text-primary px-2 py-1 rounded text-xs font-bold uppercase tracking-wider\">New</span>";
        if (status === "ACCEPTED" || status === "PREPARING") return "<span class=\"bg-blue-50 text-blue-600 px-2 py-1 rounded text-xs font-bold uppercase tracking-wider\">Preparing</span>";
        if (status === "PICKED_UP") return "<span class=\"bg-green-50 text-green-600 px-2 py-1 rounded text-xs font-bold uppercase tracking-wider\">Picked Up</span>";
        return "<span class=\"bg-gray-100 text-gray-600 px-2 py-1 rounded text-xs font-bold uppercase tracking-wider\">" + esc(status || "—") + "</span>";
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

    function renderActiveOrders(activeOrders) {
        var heading = Array.from(document.querySelectorAll("h2")).find(function (el) {
            return el.textContent && el.textContent.indexOf("Active Orders") !== -1;
        });
        if (!heading) return;

        var countBadge = heading.querySelector("span");
        if (countBadge) countBadge.textContent = String(activeOrders.length);

        var list = heading.parentElement && heading.parentElement.nextElementSibling;
        if (!list) return;

        if (activeOrders.length === 0) {
            list.innerHTML = "<div class=\"rounded-xl bg-white dark:bg-zinc-900 p-5 border border-[#e6dfdb] dark:border-zinc-800\">No active orders yet.</div>";
            return;
        }

        list.innerHTML = activeOrders.map(function (o) {
            var itemsHtml = (o.items || []).length
                ? o.items.map(function (it) {
                    return "<li class=\"text-sm text-gray-800 dark:text-gray-200\"><strong>" + esc(it.quantity) + "x</strong> " + esc(it.menuItemName || "Item") + "</li>";
                }).join("")
                : "<li class=\"text-sm text-gray-500\">No items</li>";

            var actions = "";
            if (o.status === "CREATED") {
                actions = "<div class=\"flex gap-2 mt-3\">" +
                    "<button data-order-id=\"" + esc(o.id) + "\" data-next-status=\"ACCEPTED\" class=\"restaurant-status-btn px-4 py-2 rounded-lg text-sm font-bold bg-primary text-white hover:bg-orange-600\">Accept</button>" +
                    "<button data-order-id=\"" + esc(o.id) + "\" data-next-status=\"CANCELLED\" class=\"restaurant-status-btn px-4 py-2 rounded-lg text-sm font-bold border border-gray-200 text-gray-700 dark:text-gray-300\">Reject</button>" +
                    "</div>";
            }

            return "<div class=\"rounded-xl bg-white dark:bg-zinc-900 p-5 border border-[#e6dfdb] dark:border-zinc-800 shadow-sm\">" +
                "<div class=\"flex items-center justify-between gap-3\">" +
                "<h4 class=\"text-lg font-bold\">Order #" + esc(o.id) + "</h4>" +
                statusChip(o.status) +
                "</div>" +
                "<p class=\"text-sm text-gray-500 mt-1\">To: " + esc(o.deliveryAddress || "No address") + "</p>" +
                "<ul class=\"mt-3 space-y-1\">" + itemsHtml + "</ul>" +
                "<div class=\"mt-3 pt-3 border-t border-gray-100 dark:border-zinc-800 flex items-center justify-between\">" +
                "<span class=\"font-bold\">" + formatMoney(o.totalPrice) + "</span>" +
                "</div>" +
                actions +
                "</div>";
        }).join("");

        Array.from(list.querySelectorAll(".restaurant-status-btn")).forEach(function (btn) {
            btn.addEventListener("click", function () {
                var orderId = btn.getAttribute("data-order-id");
                var nextStatus = btn.getAttribute("data-next-status");
                updateOrderStatus(orderId, nextStatus).then(loadDashboardData).catch(function (e) {
                    alert(e.message || "Update failed");
                });
            });
        });
    }

    function loadDashboardData() {
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
            .then(function (ordersWithItems) {
                var active = (ordersWithItems || []).filter(function (o) { return !!ACTIVE[o.status]; });
                renderActiveOrders(active);
            })
            .catch(function () {});
    }

    document.addEventListener("DOMContentLoaded", loadDashboardData);
})();
