(function () {
    function getOrderId() {
        var p = new URLSearchParams(window.location.search);
        return p.get("orderId");
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
                var btns = document.querySelectorAll("button");
                var markPickedUp = Array.from(btns).find(function (b) { return b.textContent.indexOf("Picked Up") !== -1 || b.textContent.indexOf("Pick") !== -1; });
                var markDelivered = Array.from(btns).find(function (b) { return b.textContent.indexOf("Delivered") !== -1; });
                function updateStatus(status) {
                    fetch("/api/orders/" + orderId + "/status", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ status: status })
                    }).then(function (r) {
                        if (r.ok) window.location.reload();
                    });
                }
                if (markPickedUp && order.status !== "PICKED_UP" && order.status !== "DELIVERED") {
                    markPickedUp.addEventListener("click", function () { updateStatus("PICKED_UP"); });
                }
                if (markDelivered) {
                    markDelivered.addEventListener("click", function () { updateStatus("DELIVERED"); });
                }
            });
    });
})();
