(function () {
    function getRestaurantId() {
        try {
            var id = localStorage.getItem("foodyRestaurantId");
            return id ? parseInt(id, 10) : 1;
        } catch (e) { return 1; }
    }
    function esc(t) { var d = document.createElement("div"); d.textContent = t == null ? "" : t; return d.innerHTML; }
    function row(item) {
        var price = item.price != null ? Number(item.price).toFixed(2) : "0.00";
        return "<tr data-item-id=\"" + item.id + "\"><td class=\"p-4\"><span class=\"font-bold\">" + esc(item.name) + "</span><span class=\"text-xs block\">#" + item.id + "</span></td>" +
            "<td class=\"p-4\"><span class=\"text-sm\">" + esc(item.description || "") + "</span></td><td class=\"p-4\">—</td>" +
            "<td class=\"p-4\"><span class=\"font-bold\">$" + price + "</span></td>" +
            "<td class=\"p-4\"><span class=\"text-xs\">" + (item.available ? "Available" : "Sold out") + "</span></td>" +
            "<td class=\"p-4\"><button type=\"button\" class=\"menu-del rounded px-2 py-1 text-red-600\" data-id=\"" + item.id + "\">Delete</button></td></tr>";
    }
    document.addEventListener("DOMContentLoaded", function () {
        var id = getRestaurantId();
        var tbody = document.getElementById("menu-items-tbody");
        if (!tbody) return;
        function load() {
            fetch("/api/restaurants/" + id + "/menu").then(function (r) { return r.ok ? r.json() : []; }).then(function (list) {
                tbody.innerHTML = list.length ? list.map(row).join("") : "<tr><td colspan=\"6\" class=\"p-8 text-center\">No items.</td></tr>";
                tbody.querySelectorAll(".menu-del").forEach(function (btn) {
                    btn.onclick = function () {
                        if (!confirm("Delete?")) return;
                        fetch("/api/restaurants/" + id + "/menuItems/" + btn.getAttribute("data-id"), { method: "DELETE" }).then(function (res) { if (res.status === 204) load(); });
                    };
                });
            });
        }
        load();
        var addBtn = document.getElementById("menu-add-btn");
        if (addBtn) {
            addBtn.onclick = function () {
                var name = prompt("Name:");
                if (!name) return;
                var price = parseFloat(prompt("Price:", "9.99")) || 0;
                fetch("/api/restaurants/" + id + "/menuItems", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ name: name, description: "", price: price, available: true }) }).then(function (r) { if (r.ok || r.status === 201) load(); });
            };
        }
    });
})();
