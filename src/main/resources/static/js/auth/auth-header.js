/**
 * Auth header: when logged in, show profile + Log out; when logged out, show Login (if element exists).
 * Set data-auth-role on body: "client" | "restaurant" | "delivery" | "any"
 * Optional: #header-login (show when logged out), #header-profile (show when logged in), #auth-logout-btn (wire to logout).
 */
(function () {
    var KEYS = { client: "foodyClientId", restaurant: "foodyRestaurantId", delivery: "foodyDriverId" };
    function isLoggedIn(role) {
        try {
            if (role === "any")
                return !!(localStorage.getItem(KEYS.client) || localStorage.getItem(KEYS.restaurant) || localStorage.getItem(KEYS.delivery));
            var key = KEYS[role];
            return key ? !!localStorage.getItem(key) : false;
        } catch (e) { return false; }
    }
    function logout(role) {
        try {
            if (role === "any") {
                localStorage.removeItem(KEYS.client);
                localStorage.removeItem(KEYS.restaurant);
                localStorage.removeItem(KEYS.delivery);
                window.location.href = "/login";
                return;
            }
            localStorage.removeItem(KEYS[role]);
            var loginUrl = "/login?role=" + (role === "client" ? "customer" : role);
            window.location.href = loginUrl;
        } catch (e) { window.location.href = "/login"; }
    }
    document.addEventListener("DOMContentLoaded", function () {
        var body = document.body;
        var role = (body && body.getAttribute("data-auth-role")) || "any";
        var loggedIn = isLoggedIn(role);
        var loginEl = document.getElementById("header-login");
        var profileEl = document.getElementById("header-profile");
        var logoutBtn = document.getElementById("auth-logout-btn");
        if (loginEl) loginEl.style.display = loggedIn ? "none" : "";
        if (profileEl) profileEl.style.display = loggedIn ? "" : "none";
        if (logoutBtn) {
            logoutBtn.onclick = function (e) { e.preventDefault(); logout(role); };
        }
    });
})();
