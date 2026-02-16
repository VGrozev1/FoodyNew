(function () {
    function getRestaurantId() {
        try {
            var id = localStorage.getItem("foodyRestaurantId");
            return id ? parseInt(id, 10) : 1;
        } catch (e) { return 1; }
    }
    document.addEventListener("DOMContentLoaded", function () {
        var id = getRestaurantId();
        fetch("/api/restaurants/" + id + "/orders").then(function (r) { return r.ok ? r.json() : []; }).then(function (orders) {
            var pending = orders.filter(function (o) { return o.status !== "DELIVERED" && o.status !== "CANCELLED"; });
            var num = document.getElementById("pending-count");
            if (num) num.textContent = String(pending.length);
        }).catch(function () {});
    });
})();
