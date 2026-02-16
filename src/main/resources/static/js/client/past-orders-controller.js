(function () {
    var CLIENT_ID_KEY = "foodyClientId";

    function getClientId() {
        try {
            var id = localStorage.getItem(CLIENT_ID_KEY);
            return id ? parseInt(id, 10) : 1;
        } catch (e) {
            return 1;
        }
    }

    function formatDate(isoOrDate) {
        if (!isoOrDate) return "—";
        var d = typeof isoOrDate === "string" ? new Date(isoOrDate) : isoOrDate;
        if (isNaN(d.getTime())) return "—";
        var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        return months[d.getMonth()] + " " + d.getDate() + ", " + d.getFullYear();
    }

    function statusClass(status) {
        if (status === "DELIVERED") return "rounded-full bg-green-100 dark:bg-green-900/30 px-2 py-0.5 text-xs font-bold text-green-700 dark:text-green-400";
        if (status === "CANCELLED") return "rounded-full bg-red-100 dark:bg-red-900/30 px-2 py-0.5 text-xs font-bold text-red-700 dark:text-red-400";
        return "rounded-full bg-yellow-100 dark:bg-yellow-900/30 px-2 py-0.5 text-xs font-bold text-yellow-700 dark:text-yellow-400";
    }

    function escapeHtml(text) {
        var div = document.createElement("div");
        div.textContent = text == null ? "" : text;
        return div.innerHTML;
    }

    document.addEventListener("DOMContentLoaded", function () {
        var tbody = document.getElementById("past-orders-tbody");
        var searchInput = document.querySelector('input[placeholder="Search restaurant or dish..."]');
        var clientId = getClientId();

        if (!tbody) return;

        fetch("/api/clients/" + clientId + "/orders")
            .then(function (res) {
                if (!res.ok) throw new Error("Could not load orders");
                return res.json();
            })
            .then(function (orders) {
                if (!Array.isArray(orders)) orders = [];
                if (orders.length === 0) {
                    tbody.innerHTML = "<tr><td colspan=\"5\" class=\"px-4 py-8 text-center text-gray-500 dark:text-gray-400\">No orders yet.</td></tr>";
                    return;
                }
                tbody.innerHTML = orders.map(function (order) {
                    var status = order.status || "CREATED";
                    var restaurantName = escapeHtml(order.restaurantName || "Restaurant");
                    return (
                        "<tr class=\"border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800/30\" data-order-id=\"" + order.id + "\" data-restaurant-id=\"" + (order.restaurantId || "") + "\">" +
                        "<td class=\"px-4 py-3 font-medium text-[#181411] dark:text-white\">" + order.id + "</td>" +
                        "<td class=\"px-4 py-3 text-gray-600 dark:text-gray-300\">" + restaurantName + "</td>" +
                        "<td class=\"px-4 py-3 text-gray-600 dark:text-gray-300\">" + formatDate(order.createdAt) + "</td>" +
                        "<td class=\"px-4 py-3\"><span class=\"" + statusClass(status) + "\">" + escapeHtml(status) + "</span></td>" +
                        "<td class=\"px-4 py-3 flex items-center gap-2\">" +
                        "<a href=\"/orderTrack?orderId=" + order.id + "\" class=\"text-primary font-semibold hover:underline\">Track</a>" +
                        "<span class=\"text-gray-300\">|</span>" +
                        "<button type=\"button\" class=\"reorder-btn text-primary font-semibold hover:underline\" data-order-id=\"" + order.id + "\" data-restaurant-id=\"" + (order.restaurantId || "") + "\">Reorder</button>" +
                        "<span class=\"text-gray-300\">|</span>" +
                        "<button type=\"button\" title=\"View Receipt\" class=\"text-gray-500 hover:text-primary\">Receipt</button>" +
                        "</td></tr>"
                    );
                }).join("");

                tbody.querySelectorAll(".reorder-btn").forEach(function (btn) {
                    btn.addEventListener("click", function () {
                        var orderId = btn.getAttribute("data-order-id");
                        var restaurantId = btn.getAttribute("data-restaurant-id");
                        if (!orderId || !restaurantId) {
                            alert("Cannot reorder.");
                            return;
                        }
                        btn.disabled = true;
                        fetch("/api/orders/" + orderId + "/items")
                            .then(function (res) {
                                if (!res.ok) throw new Error("Could not load order items");
                                return res.json();
                            })
                            .then(function (items) {
                                if (!items || items.length === 0) throw new Error("Order has no items");
                                return fetch("/api/orders", {
                                    method: "POST",
                                    headers: { "Content-Type": "application/json" },
                                    body: JSON.stringify({
                                        clientId: clientId,
                                        restaurantId: parseInt(restaurantId, 10),
                                        items: items
                                    })
                                });
                            })
                            .then(function (res) {
                                if (res.status !== 201) throw new Error("Reorder failed");
                                return res.json();
                            })
                            .then(function (newOrder) {
                                window.location.href = "/orderTrack?orderId=" + newOrder.id;
                            })
                            .catch(function (err) {
                                alert(err.message || "Reorder failed.");
                                btn.disabled = false;
                            });
                    });
                });

                document.querySelectorAll('[title="View Receipt"]').forEach(function (btn) {
                    btn.addEventListener("click", function () { alert("Receipt coming soon!"); });
                });
                var filterBtn = document.querySelector("button");
                if (filterBtn && filterBtn.textContent.indexOf("Filter") !== -1) {
                    filterBtn.addEventListener("click", function () { alert("Filter options coming soon!"); });
                }
            })
            .catch(function () {
                tbody.innerHTML = "<tr><td colspan=\"5\" class=\"px-4 py-8 text-center text-red-500\">Failed to load orders.</td></tr>";
            });

        if (searchInput) {
            searchInput.addEventListener("input", function () {
                var val = (searchInput.value || "").toLowerCase();
                var rows = tbody.querySelectorAll("tr");
                rows.forEach(function (row) {
                    if (row.cells.length < 2) return;
                    var text = (row.cells[1].textContent || "").toLowerCase() + " " + (row.cells[0].textContent || "").toLowerCase();
                    row.style.display = !val || text.indexOf(val) !== -1 ? "" : "none";
                });
            });
        }
    });
})();
