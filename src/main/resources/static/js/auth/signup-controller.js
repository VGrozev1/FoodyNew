// Single signup page: role from URL (?role=restaurant | ?role=delivery). Default: customer.
// POST to /api/auth/client|restaurant|driver/signup, then redirect to the matching login.
(function () {
  const ROLES = ["customer", "restaurant", "delivery"];
  const TAB_IDS = { customer: "tab-customer", restaurant: "tab-restaurant", delivery: "tab-delivery" };
  const PANEL_IDS = { customer: "panel-customer", restaurant: "panel-restaurant", delivery: "panel-delivery" };

  function getRoleFromUrl() {
    const params = new URLSearchParams(window.location.search);
    const role = (params.get("role") || "").toLowerCase();
    if (role === "restaurant" || role === "delivery") return role;
    return "customer";
  }

  function setActiveTab(activeRole) {
    const tabs = document.querySelectorAll(".signup-tab");
    tabs.forEach((el) => {
      el.classList.remove("border-primary", "text-primary");
      el.classList.add("border-transparent");
      const span = el.querySelector("span");
      if (span) {
        span.classList.remove("text-primary");
        span.classList.add("text-[#8a7260]", "dark:text-gray-400");
      }
    });
    const activeTab = document.getElementById(TAB_IDS[activeRole]);
    if (activeTab) {
      activeTab.classList.remove("border-transparent");
      activeTab.classList.add("border-primary", "text-primary");
      const span = activeTab.querySelector("span");
      if (span) {
        span.classList.remove("text-[#8a7260]", "dark:text-gray-400");
        span.classList.add("text-primary");
      }
    }
  }

  function showPanel(activeRole) {
    document.querySelectorAll(".signup-panel").forEach((el) => el.classList.add("hidden"));
    const panel = document.getElementById(PANEL_IDS[activeRole]);
    if (panel) panel.classList.remove("hidden");

    // Required fields: customer = first/last/phone; restaurant = restaurantName (optional in API); delivery = none extra
    const firstName = document.querySelector("input[name='firstName']");
    const lastName = document.querySelector("input[name='lastName']");
    const phone = document.querySelector("input[name='phone']");
    const restaurantName = document.querySelector("input[name='restaurantName']");
    if (firstName) firstName.required = activeRole === "customer";
    if (lastName) lastName.required = activeRole === "customer";
    if (phone) phone.required = activeRole === "customer";
    if (restaurantName) restaurantName.required = activeRole === "restaurant";
  }

  function setCopy(activeRole) {
    const subline = document.getElementById("signupSubline");
    const btnText = document.getElementById("signupButtonText");
    if (subline) {
      if (activeRole === "customer") subline.textContent = "Sign up as a customer to order food.";
      else if (activeRole === "restaurant") subline.textContent = "Register your restaurant and start reaching more customers.";
      else subline.textContent = "Create your courier account and start delivering.";
    }
    if (btnText) {
      if (activeRole === "customer") btnText.textContent = "Create customer account";
      else if (activeRole === "restaurant") btnText.textContent = "Create partner account";
      else btnText.textContent = "Create rider account";
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("signupForm");
    if (!form) return;

    const activeRole = getRoleFromUrl();
    setActiveTab(activeRole);
    showPanel(activeRole);
    setCopy(activeRole);

    form.addEventListener("submit", function (e) {
      e.preventDefault();

      const emailEl = document.getElementById("signupEmail");
      const pwdEl = document.getElementById("signupPassword");
      const pwdConfirmEl = document.getElementById("signupPasswordConfirm");
      const email = (emailEl && emailEl.value) ? emailEl.value.trim() : "";
      const password = (pwdEl && pwdEl.value) ? pwdEl.value : "";
      const passwordConfirm = (pwdConfirmEl && pwdConfirmEl.value) ? pwdConfirmEl.value : "";

      if (!email || !password) {
        alert("Please fill in email and password.");
        return;
      }
      if (password !== passwordConfirm) {
        alert("Passwords do not match.");
        if (pwdConfirmEl) pwdConfirmEl.focus();
        return;
      }

      const btn = document.getElementById("signupButton");
      if (btn) btn.disabled = true;

      function onFail(msg) {
        alert(msg || "Signup failed. Try again.");
        if (btn) btn.disabled = false;
      }

      if (activeRole === "customer") {
        const firstName = (document.querySelector("input[name='firstName']") && document.querySelector("input[name='firstName']").value) ? document.querySelector("input[name='firstName']").value.trim() : "";
        const lastName = (document.querySelector("input[name='lastName']") && document.querySelector("input[name='lastName']").value) ? document.querySelector("input[name='lastName']").value.trim() : "";
        const phone = (document.querySelector("input[name='phone']") && document.querySelector("input[name='phone']").value) ? document.querySelector("input[name='phone']").value.trim() : "";
        const name = (firstName + " " + lastName).trim() || "Customer";
        fetch("/api/auth/client/signup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password, name, phone, address: "" })
        })
          .then(function (r) {
            if (r.ok) return r.json();
            if (r.status === 409) throw new Error("Email already registered.");
            throw new Error("Signup failed.");
          })
          .then(function () {
            alert("Account created! Redirecting to login.");
            window.location.href = "/login";
          })
          .catch(function (err) {
            onFail(err.message);
          });
        return;
      }

      if (activeRole === "restaurant") {
        const nameEl = document.querySelector("input[name='restaurantName']");
        const name = (nameEl && nameEl.value) ? nameEl.value.trim() : "";
        if (!name) {
          alert("Please enter your restaurant name.");
          if (btn) btn.disabled = false;
          return;
        }
        fetch("/api/auth/restaurant/signup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password, name, description: "", address: "" })
        })
          .then(function (r) {
            if (r.ok) return r.json();
            if (r.status === 409) throw new Error("Email already registered.");
            throw new Error("Signup failed.");
          })
          .then(function () {
            alert("Account created! Redirecting to login.");
            window.location.href = "/login?role=restaurant";
          })
          .catch(function (err) {
            onFail(err.message);
          });
        return;
      }

      if (activeRole === "delivery") {
        fetch("/api/auth/driver/signup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password, vehicleType: "bike" })
        })
          .then(function (r) {
            if (r.ok) return r.json();
            if (r.status === 409) throw new Error("Email already registered.");
            throw new Error("Signup failed.");
          })
          .then(function () {
            alert("Account created! Redirecting to login.");
            window.location.href = "/login?role=delivery";
          })
          .catch(function (err) {
            onFail(err.message);
          });
      }
    });
  });
})();
