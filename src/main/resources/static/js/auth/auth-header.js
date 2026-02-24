/**
 * Auth header: when logged in, show profile + Log out; when logged out, show Login (if element exists).
 * Set data-auth-role on body: "client" | "restaurant" | "delivery" | "any"
 * Optional: #header-login (show when logged out), #header-profile (show when logged in), #auth-logout-btn (wire to logout).
 */
(function () {
    var KEYS = { client: "foodyClientId", restaurant: "foodyRestaurantId", delivery: "foodyDriverId" };
    var TOKEN_KEY = "foodyAuthToken";
    var ROLE_KEY = "foodyAuthRole";

    function getToken() {
        try { return localStorage.getItem(TOKEN_KEY); } catch (e) { return null; }
    }

    // Attach JWT automatically for API calls.
    function patchFetchWithAuth() {
        if (window.__foodyAuthFetchPatched) return;
        var nativeFetch = window.fetch;
        window.fetch = function (input, init) {
            var token = getToken();
            var requestUrl = typeof input === "string" ? input : (input && input.url ? input.url : "");
            var shouldAttach = token && requestUrl && requestUrl.indexOf("/api/") !== -1;
            if (!shouldAttach) {
                return nativeFetch(input, init);
            }
            var nextInit = init ? Object.assign({}, init) : {};
            var headers = new Headers(nextInit.headers || {});
            if (!headers.has("Authorization")) {
                headers.set("Authorization", "Bearer " + token);
            }
            nextInit.headers = headers;
            return nativeFetch(input, nextInit);
        };
        window.__foodyAuthFetchPatched = true;
    }

    function isLoggedIn(role) {
        try {
            if (role === "any")
                return !!(localStorage.getItem(KEYS.client) || localStorage.getItem(KEYS.restaurant) || localStorage.getItem(KEYS.delivery)) && !!localStorage.getItem(TOKEN_KEY);
            var key = KEYS[role];
            return key ? (!!localStorage.getItem(key) && !!localStorage.getItem(TOKEN_KEY)) : false;
        } catch (e) { return false; }
    }
    function logout(role) {
        try {
            if (role === "any") {
                localStorage.removeItem(KEYS.client);
                localStorage.removeItem(KEYS.restaurant);
                localStorage.removeItem(KEYS.delivery);
                localStorage.removeItem(TOKEN_KEY);
                localStorage.removeItem(ROLE_KEY);
                window.location.href = "/login";
                return;
            }
            localStorage.removeItem(KEYS[role]);
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(ROLE_KEY);
            var loginUrl = "/login?role=" + (role === "client" ? "customer" : role);
            window.location.href = loginUrl;
        } catch (e) { window.location.href = "/login"; }
    }
    document.addEventListener("DOMContentLoaded", function () {
        patchFetchWithAuth();
        var body = document.body;
        var role = (body && body.getAttribute("data-auth-role")) || "any";
        var loggedIn = isLoggedIn(role);
        if (role !== "any" && !loggedIn) {
            var loginUrl = "/login?role=" + (role === "client" ? "customer" : role);
            window.location.href = loginUrl;
            return;
        }
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
