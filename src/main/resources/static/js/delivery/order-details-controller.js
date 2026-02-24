(function () {
    var DRIVER_ID_KEY = "foodyDriverId";
    function getOrderId() {
        var p = new URLSearchParams(window.location.search);
        return p.get("orderId");
    }
    function getDriverId() {
        try {
            var id = localStorage.getItem(DRIVER_ID_KEY);
            return id ? parseInt(id, 10) : null;
        } catch (e) {
            return null;
        }
    }
    document.addEventListener("DOMContentLoaded", function () {
        var orderId = getOrderId();
        if (!orderId) {
            document.querySelector("main").innerHTML = "<p class=\"text-center py-12\">No order selected. Open from dashboard.</p>";
            return;
        }
        fetch("/api/orders/" + orderId)
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (order) {
                if (!order) return;
                var h1 = document.querySelector("main h1");
                if (h1) h1.textContent = "Order #" + order.id;
                var etaEl = document.querySelector("p.text-text-sub-light");
                if (etaEl) etaEl.textContent = "Est. Delivery: " + (order.estimatedDeliveryAt ? new Date(order.estimatedDeliveryAt).toLocaleTimeString() : "—");
                var statusEl = document.querySelector(".text-sm.font-bold.text-primary");
                if (statusEl) statusEl.textContent = order.status || "—";
                var markPickedUp = document.querySelector(".mark-picked-up-btn");
                var markDelivered = document.querySelector(".mark-delivered-btn");
                function updateStatus(status) {
                    var driverId = getDriverId();
                    var payload = { status: status };
                    if (driverId != null) payload.driverId = driverId;
                    fetch("/api/orders/" + orderId + "/status", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify(payload)
                    }).then(function (r) {
                        if (r.ok) window.location.reload();
                    });
                }
                if (markPickedUp) {
                    var pickupStatus = (order.status === "CREATED") ? "ACCEPTED" : "PICKED_UP";
                    var pickupDone = order.status === "PICKED_UP" || order.status === "DELIVERED";
                    markPickedUp.disabled = pickupDone;
                    if (pickupDone) {
                        markPickedUp.classList.add("opacity-60", "cursor-not-allowed");
                    } else {
                        markPickedUp.addEventListener("click", function () { updateStatus(pickupStatus); });
                    }
                }
                if (markDelivered) {
                    var delivered = order.status === "DELIVERED";
                    markDelivered.disabled = delivered;
                    if (delivered) {
                        markDelivered.classList.add("opacity-60", "cursor-not-allowed");
                    } else {
                        markDelivered.addEventListener("click", function () { updateStatus("DELIVERED"); });
                    }
                }
            });
    });
})();
