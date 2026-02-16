(function () {
    var DRIVER_ID_KEY = "foodyDriverId";
    function getDriverId() {
        try {
            var id = localStorage.getItem(DRIVER_ID_KEY);
            return id ? parseInt(id, 10) : 1;
        } catch (e) { return 1; }
    }
    document.addEventListener("DOMContentLoaded", function () {
        var id = getDriverId();
        fetch("/api/drivers/" + id + "/orders?status=ACTIVE")
            .then(function (r) { return r.ok ? r.json() : []; })
            .then(function (orders) {
                var listEl = document.getElementById("delivery-orders-list");
                if (listEl) {
                    listEl.innerHTML = orders.length > 0 ? orders.map(function (o) {
                        return "<a href=\"/orderDetails?orderId=" + o.id + "\" class=\"flex flex-col gap-3 rounded-xl bg-surface-light dark:bg-surface-dark p-5 shadow-sm border border-[#e6e0db] dark:border-[#3e342e] hover:shadow-md transition-all\">" +
                            "<div class=\"flex justify-between\"><h4 class=\"font-bold text-text-main dark:text-white\">Order #" + o.id + "</h4><span class=\"text-xs font-bold uppercase text-primary\">" + (o.status || "") + "</span></div>" +
                            "<p class=\"text-sm text-text-muted dark:text-gray-400\">" + (o.restaurantName || "Restaurant") + " • $" + (o.totalPrice != null ? Number(o.totalPrice).toFixed(2) : "0") + "</p>" +
                            "</a>";
                    }).join("") : "<p class=\"text-text-muted dark:text-gray-400\">No active orders.</p>";
                }
            }).catch(function () {});
    });
})();
