(function () {
    var DRIVER_ID_KEY = "foodyDriverId";
    var DELIVERY_FEE = 2.99;

    function getDriverId() {
        try {
            var raw = localStorage.getItem(DRIVER_ID_KEY);
            return raw ? parseInt(raw, 10) : 1;
        } catch (e) {
            return 1;
        }
    }

    function formatMoney(value) {
        return "$" + Number(value || 0).toFixed(2);
    }

    function formatTime(value) {
        if (!value) return "—";
        var d = new Date(value);
        if (isNaN(d.getTime())) return "—";
        return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    }

    function esc(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function renderOrders(orders) {
        var listEl = document.getElementById("delivery-orders-list");
        if (!listEl) return;
        if (!orders || orders.length === 0) {
            listEl.innerHTML = "<p class=\"text-text-muted dark:text-gray-400\">No active orders.</p>";
            return;
        }
        listEl.innerHTML = orders.map(function (o) {
            return "<a href=\"/orderDetails?orderId=" + o.id + "\" class=\"flex flex-col gap-3 rounded-xl bg-surface-light dark:bg-surface-dark p-5 shadow-sm border border-[#e6e0db] dark:border-[#3e342e] hover:shadow-md transition-all\">" +
                "<div class=\"flex items-center justify-between gap-2\">" +
                "<p class=\"text-base font-bold text-text-main dark:text-white\">Order #" + esc(o.id) + "</p>" +
                "<span class=\"inline-flex items-center rounded-full bg-primary/10 text-primary text-xs font-bold px-2.5 py-1\">" + esc(o.status || "ACTIVE") + "</span>" +
                "</div>" +
                "<p class=\"text-sm text-text-muted dark:text-gray-400\">" + esc(o.restaurantName || "Restaurant") + "</p>" +
                "<p class=\"text-sm text-text-muted dark:text-gray-400\">Dropoff: " + esc(o.deliveryAddress || "No address") + "</p>" +
                "<div class=\"flex items-center justify-between text-xs text-text-muted dark:text-gray-500\">" +
                "<span>Created " + esc(formatTime(o.createdAt)) + "</span>" +
                "<span class=\"text-primary font-bold\">Fee " + formatMoney(DELIVERY_FEE) + "</span>" +
                "</div>" +
                "</a>";
        }).join("");
    }

    function updateSummary(orders) {
        var count = orders ? orders.length : 0;
        var activeCountEl = document.getElementById("delivery-active-count");
        if (activeCountEl) activeCountEl.textContent = String(count);
        var earningsEl = document.getElementById("delivery-earnings");
        if (earningsEl) earningsEl.textContent = formatMoney(count * DELIVERY_FEE);
    }

    function loadOrders() {
        var driverId = getDriverId();
        fetch("/api/drivers/" + driverId + "/orders?status=ACTIVE")
            .then(function (response) { return response.ok ? response.json() : []; })
            .then(function (orders) {
                updateSummary(orders);
                renderOrders(orders);
            })
            .catch(function () {
                updateSummary([]);
                renderOrders([]);
            });
    }

    document.addEventListener("DOMContentLoaded", loadOrders);
})();
