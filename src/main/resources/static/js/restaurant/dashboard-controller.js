(function () {
    function getRestaurantId() {
        try {
            var id = localStorage.getItem("foodyRestaurantId");
            return id ? parseInt(id, 10) : 1;
        } catch (e) { return 1; }
    }
    document.addEventListener("DOMContentLoaded", function () {
        var id = getRestaurantId();
        Promise.all([
            fetch("/api/restaurants/" + id).then(function (r) { return r.ok ? r.json() : null; }),
            fetch("/api/restaurants/" + id + "/dashboard").then(function (r) { return r.ok ? r.json() : null; }),
            fetch("/api/restaurants/" + id + "/orders").then(function (r) { return r.ok ? r.json() : []; })
        ]).then(function (results) {
            var restaurant = results[0];
            var dashboard = results[1];
            var orders = Array.isArray(results[2]) ? results[2] : [];
            var nameEl = document.getElementById("restaurant-name");
            if (restaurant && nameEl) nameEl.textContent = restaurant.name || "Dashboard";
            if (dashboard) {
                var grid = document.getElementById("dashboard-stats");
                if (grid) {
                    var cards = grid.querySelectorAll("p.text-3xl");
                    if (cards.length >= 1) cards[0].textContent = "$" + (dashboard.todayRevenue != null ? Number(dashboard.todayRevenue).toFixed(2) : "0.00");
                    if (cards.length >= 2) cards[1].textContent = String(dashboard.pendingOrders != null ? dashboard.pendingOrders : 0);
                    if (cards.length >= 3) cards[2].textContent = String(dashboard.pendingOrders != null ? dashboard.pendingOrders : 0);
                    if (cards.length >= 4) cards[3].textContent = String(dashboard.menuItemCount != null ? dashboard.menuItemCount : "—");
                }
            }
        }).catch(function () {});
    });
})();
