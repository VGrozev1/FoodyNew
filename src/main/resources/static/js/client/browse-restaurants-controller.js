(function () {
    var DEFAULT_IMAGE = "https://lh3.googleusercontent.com/aida-public/AB6AXuBW-z9nrmaexIPZAFtnJYK1ij5-bhwtSzISveBV3HJm7RNINkHNdKWDPjUwqKcCsFsuk2GwdBWUJinbZn9jBH46iwUfODcaC0P5jgDqg_UWLgT5FV7F69q22Y0z8MhX3UUajfgE3d_6_MtaYeIyU2mds6v_oH6ernrXlUReQmmK1IA5y0Lsx-4DL8uvqrDs1cpOHPf3LXvj5XZuCEYp3WJW7mLbBgtBDnD6TN7_193U3R6aw9oRRk3Jfe6Vdd9dnhVWHhgwRPRX6kQ";

    function renderRestaurantCard(r) {
        var id = r.id;
        var name = escapeHtml(r.name || "Restaurant");
        var description = escapeHtml((r.description || "").trim() ? r.description : "Delivery");
        var open = r.open;
        var openLabel = open ? "Open" : "Closed";
        var openClass = open ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400" : "bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400";
        var img = DEFAULT_IMAGE;
        return (
            '<div class="group relative flex flex-col gap-4 rounded-xl bg-surface-light dark:bg-surface-dark p-3 shadow-sm border border-border-light dark:border-border-dark hover:shadow-lg hover:-translate-y-1 transition-all duration-300 cursor-pointer" data-restaurant-id="' + id + '">' +
            '  <div class="relative h-48 w-full overflow-hidden rounded-lg">' +
            '    <div class="w-full h-full bg-cover bg-center transition-transform duration-500 group-hover:scale-110" style="background-image: url(\'' + img + '\');"></div>' +
            '    <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-60"></div>' +
            '    <button type="button" class="absolute top-2 right-2 flex size-8 items-center justify-center rounded-full bg-white/90 text-text-secondary hover:text-red-500 hover:bg-white transition-colors shadow-sm" aria-label="Favorite"><span class="material-symbols-outlined text-[20px]">favorite</span></button>' +
            '    <div class="absolute bottom-2 left-2 flex items-center gap-1 rounded-md bg-white/95 px-2 py-1 text-xs font-bold text-text-main shadow-sm"><span class="material-symbols-outlined text-[14px]">schedule</span> 20-30 min</div>' +
            '  </div>' +
            '  <div class="flex flex-col gap-1 px-1 pb-1">' +
            '    <div class="flex items-center justify-between">' +
            '      <h3 class="text-lg font-bold text-text-main dark:text-white truncate">' + name + '</h3>' +
            '      <div class="flex items-center gap-1 rounded-full ' + openClass + ' px-2 py-0.5"><span class="text-xs font-bold">' + openLabel + '</span></div>' +
            '    </div>' +
            '    <p class="text-sm text-text-secondary dark:text-gray-400">' + description + '</p>' +
            '    <div class="flex items-center gap-3 text-sm text-text-secondary dark:text-gray-400 mt-1">' +
            '      <span class="flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">local_shipping</span> Delivery</span>' +
            '      <span class="w-1 h-1 rounded-full bg-gray-300"></span>' +
            '      <span class="text-primary font-bold">$$</span>' +
            '    </div>' +
            '  </div>' +
            '</div>'
        );
    }

    function escapeHtml(text) {
        var div = document.createElement("div");
        div.textContent = text;
        return div.innerHTML;
    }

    function loadRestaurants() {
        var grid = document.getElementById("restaurant-grid");
        var countEl = document.getElementById("restaurant-count");
        if (!grid) return;

        grid.innerHTML = '<p class="col-span-full text-center text-text-secondary dark:text-gray-400 py-12">Loading restaurants…</p>';

        fetch("/api/restaurants")
            .then(function (res) {
                if (!res.ok) throw new Error("Could not load restaurants");
                return res.json();
            })
            .then(function (list) {
                if (!Array.isArray(list)) list = [];
                if (countEl) countEl.textContent = list.length + " restaurant" + (list.length === 1 ? "" : "s");
                if (list.length === 0) {
                    grid.innerHTML = '<p class="col-span-full text-center text-text-secondary dark:text-gray-400 py-12">No restaurants yet. Check back later!</p>';
                    return;
                }
                grid.innerHTML = list.map(renderRestaurantCard).join("");
                grid.querySelectorAll("[data-restaurant-id]").forEach(function (card) {
                    card.addEventListener("click", function (e) {
                        if (e.target.closest("button")) return;
                        var id = card.getAttribute("data-restaurant-id");
                        if (id) window.location.href = "/Menu?restaurantId=" + encodeURIComponent(id);
                    });
                });
            })
            .catch(function () {
                grid.innerHTML = '<p class="col-span-full text-center text-red-600 dark:text-red-400 py-12">Failed to load restaurants. Try again later.</p>';
            });
    }

    function getFindFoodBtn() {
        return Array.from(document.querySelectorAll("button")).find(function (el) {
            return el.textContent.indexOf("Find Food") !== -1 || (el.querySelector(".material-symbols-outlined") && el.querySelector(".material-symbols-outlined").textContent === "search");
        });
    }

    function init() {
        loadRestaurants();

        var searchInput = document.querySelector('input[placeholder*="address"]');
        var findFoodBtn = getFindFoodBtn();
        var categoryLinks = document.querySelectorAll("#category-rail a.group");

        if (findFoodBtn) {
            findFoodBtn.addEventListener("click", function (e) {
                e.preventDefault();
                var val = searchInput ? searchInput.value : "No address";
                alert("Searching for: " + val);
            });
        }

        categoryLinks.forEach(function (link) {
            link.addEventListener("click", function (e) {
                e.preventDefault();
                var categoryName = link.querySelector("span") ? link.querySelector("span").textContent.trim() : "";
                var grid = document.getElementById("restaurant-grid");
                if (grid && categoryName) {
                    grid.innerHTML = "<h2 class=\"col-span-full text-center text-2xl py-20\">Loading " + categoryName + "…</h2>";
                    loadRestaurants();
                }
            });
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
