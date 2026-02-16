// Order tracking: orderId from URL, fetch order + items, progress bar (Preparing / Delivering / Delivered), real order summary.
document.addEventListener("DOMContentLoaded", function () {
    var params = new URLSearchParams(window.location.search);
    var orderId = params.get("orderId");

    var noOrderEl = document.getElementById("track-no-order");
    var contentEl = document.getElementById("track-content");
    var statusBadge = document.getElementById("order-status-badge");
    var updatedText = document.getElementById("order-updated");
    var orderNumberEl = document.getElementById("order-number");
    var orderEtaEl = document.getElementById("order-eta");
    var orderEtaLabel = document.getElementById("order-eta-label");
    var deliveryAddressText = document.getElementById("delivery-address-text");
    var courierDescEl = document.getElementById("courier-desc");
    var orderItemsList = document.getElementById("order-items-list");
    var orderTotalEl = document.getElementById("order-total");
    var backBtn = document.getElementById("back-to-home-button");
    var callBtn = document.getElementById("call-courier-btn");

    var barPreparing = document.getElementById("bar-preparing");
    var barDelivering = document.getElementById("bar-delivering");
    var barDelivered = document.getElementById("bar-delivered");
    var stepPreparingLabel = document.getElementById("step-preparing-label");
    var stepDeliveringLabel = document.getElementById("step-delivering-label");
    var stepDeliveredLabel = document.getElementById("step-delivered-label");

    function formatEta(isoOrDate) {
        if (!isoOrDate) return "—";
        var d = typeof isoOrDate === "string" ? new Date(isoOrDate) : isoOrDate;
        if (isNaN(d.getTime())) return "—";
        var h = d.getHours();
        var m = d.getMinutes();
        var am = h < 12;
        h = h % 12 || 12;
        return h + ":" + (m < 10 ? "0" : "") + m + " " + (am ? "AM" : "PM");
    }

    // 0 = Preparing, 1 = Delivering, 2 = Delivered
    function statusToProgressStep(status) {
        if (status === "DELIVERED") return 2;
        if (status === "PICKED_UP") return 1;
        return 0; // CREATED, ACCEPTED, PREPARING
    }

    function statusToLabel(status) {
        if (status === "DELIVERED") return "Delivered";
        if (status === "PICKED_UP") return "Out for delivery";
        if (status === "CANCELLED") return "Cancelled";
        return "Preparing";
    }

    function esc(t) {
        if (t == null) return "";
        var d = document.createElement("div");
        d.textContent = t;
        return d.innerHTML;
    }

    function setProgressBar(step) {
        var primary = "bg-primary";
        var muted = "text-text-muted dark:text-gray-400";
        var active = "text-primary font-bold";
        if (barPreparing) barPreparing.style.width = step >= 0 ? "100%" : "0%";
        if (barDelivering) barDelivering.style.width = step >= 1 ? "100%" : "0%";
        if (barDelivered) barDelivered.style.width = step >= 2 ? "100%" : "0%";
        if (stepPreparingLabel) {
            stepPreparingLabel.className = "text-sm font-semibold " + (step >= 0 ? active : muted);
        }
        if (stepDeliveringLabel) {
            stepDeliveringLabel.className = "text-sm font-semibold " + (step >= 1 ? active : muted);
        }
        if (stepDeliveredLabel) {
            stepDeliveredLabel.className = "text-sm font-semibold " + (step >= 2 ? active : muted);
        }
    }

    function renderOrder(order, items) {
        if (orderNumberEl) orderNumberEl.textContent = "Order #" + order.id;
        if (statusBadge) statusBadge.textContent = statusToLabel(order.status);
        if (updatedText) updatedText.textContent = "Updated just now";
        var etaStr = formatEta(order.estimatedDeliveryAt);
        if (orderEtaEl) orderEtaEl.textContent = etaStr;
        if (order.status === "DELIVERED" && orderEtaLabel) orderEtaLabel.textContent = "Delivered at";
        else if (orderEtaLabel) orderEtaLabel.textContent = "Est. arrival";
        if (deliveryAddressText) deliveryAddressText.textContent = (order.deliveryAddress && order.deliveryAddress.trim()) ? order.deliveryAddress.trim() : "—";
        if (courierDescEl) courierDescEl.textContent = order.driverName ? order.driverName + " is on the way" : "No courier assigned yet.";
        setProgressBar(statusToProgressStep(order.status));

        if (orderItemsList) {
            if (!items || items.length === 0) {
                orderItemsList.innerHTML = "<li class=\"text-sm text-text-muted dark:text-gray-400\">No items</li>";
            } else {
                orderItemsList.innerHTML = items.map(function (item) {
                    var qty = item.quantity || 1;
                    var price = item.price != null ? Number(item.price) : 0;
                    var lineTotal = (qty * price).toFixed(2);
                    var name = (item.itemName != null ? item.itemName : "Item") + (item.menuItemId ? " #" + item.menuItemId : "");
                    return "<li class=\"flex justify-between items-start text-sm\">" +
                        "<div class=\"flex gap-2\"><span class=\"font-bold text-primary\">" + qty + "×</span><span class=\"text-text-main dark:text-gray-200\">" + esc(name) + "</span></div>" +
                        "<span class=\"font-medium text-text-main dark:text-gray-200\">$" + lineTotal + "</span></li>";
                }).join("");
            }
        }
        if (orderTotalEl) {
            var total = order.totalPrice != null ? Number(order.totalPrice).toFixed(2) : "0.00";
            orderTotalEl.textContent = "$" + total;
        }

        if (callBtn && order.driverPhone) {
            callBtn.onclick = function () { window.location.href = "tel:" + order.driverPhone; };
        } else if (callBtn) {
            callBtn.onclick = null;
            callBtn.disabled = true;
            callBtn.classList.add("opacity-50", "cursor-not-allowed");
        }
    }

    function fetchOrder() {
        if (!orderId) {
            if (noOrderEl) noOrderEl.classList.remove("hidden");
            if (contentEl) contentEl.classList.add("hidden");
            return;
        }
        if (noOrderEl) noOrderEl.classList.add("hidden");
        if (contentEl) contentEl.classList.remove("hidden");

        Promise.all([
            fetch("/api/orders/" + encodeURIComponent(orderId)).then(function (r) { return r.ok ? r.json() : null; }),
            fetch("/api/orders/" + encodeURIComponent(orderId) + "/items").then(function (r) { return r.ok ? r.json() : []; })
        ])
            .then(function (results) {
                var order = results[0];
                var items = Array.isArray(results[1]) ? results[1] : [];
                if (!order) {
                    if (statusBadge) statusBadge.textContent = "Not found";
                    if (updatedText) updatedText.textContent = "Could not load order.";
                    return;
                }
                renderOrder(order, items);
            })
            .catch(function () {
                if (statusBadge) statusBadge.textContent = "Error";
                if (updatedText) updatedText.textContent = "Could not load order.";
            });
    }

    fetchOrder();
    setInterval(fetchOrder, 15000);

    if (backBtn) backBtn.addEventListener("click", function () { window.location.href = "/restaurants"; });
});
