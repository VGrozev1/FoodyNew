// Unified login: one page for Customer, Restaurant, Courier. Role from ?role= in URL.
document.addEventListener("DOMContentLoaded", function () {
  const params = new URLSearchParams(window.location.search);
  const role = (params.get("role") || "customer").toLowerCase();
  const validRole = ["customer", "restaurant", "delivery"].includes(role) ? role : "customer";

  const loginForm = document.getElementById("loginForm");
  const loginHeadline = document.getElementById("loginHeadline");
  const tabCustomer = document.getElementById("tab-customer");
  const tabRestaurant = document.getElementById("tab-restaurant");
  const tabDelivery = document.getElementById("tab-delivery");

  var currentRole = validRole;

  function setActiveTab(activeRole) {
    currentRole = activeRole;
    var tabs = [
      { el: tabCustomer, role: "customer" },
      { el: tabRestaurant, role: "restaurant" },
      { el: tabDelivery, role: "delivery" }
    ];
    tabs.forEach(function (t) {
      if (!t.el) return;
      t.el.classList.remove("border-primary", "text-primary");
      t.el.classList.add("border-transparent");
      t.el.querySelector("span").classList.remove("text-primary");
      t.el.querySelector("span").classList.add("text-[#8a7260]", "dark:text-gray-400");
      if (t.role === activeRole) {
        t.el.classList.remove("border-transparent");
        t.el.classList.add("border-primary", "text-primary");
        t.el.querySelector("span").classList.remove("text-[#8a7260]", "dark:text-gray-400");
        t.el.querySelector("span").classList.add("text-primary");
      }
    });
    if (loginHeadline) {
      if (activeRole === "restaurant") loginHeadline.textContent = "Partner login – access your dashboard";
      else if (activeRole === "delivery") loginHeadline.textContent = "Driver login – start your shift";
      else loginHeadline.textContent = "Log in to order food";
    }
  }

  setActiveTab(validRole);

  if (loginForm) {
    loginForm.addEventListener("submit", function (event) {
      event.preventDefault();

      var email = (document.getElementById("email") && document.getElementById("email").value) || "";
      var password = (document.getElementById("password") && document.getElementById("password").value) || "";

      if (!email || !password) {
        alert("Please enter email and password.");
        return;
      }

      var endpoint, redirectUrl;
      if (currentRole === "restaurant") {
        endpoint = "/api/auth/restaurant/login";
        redirectUrl = "/myRestaurant";
      } else if (currentRole === "delivery") {
        endpoint = "/api/auth/driver/login";
        redirectUrl = "/myDelivery";
      } else {
        endpoint = "/api/auth/client/login";
        redirectUrl = "/restaurants";
      }

      fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email, password: password })
      })
        .then(function (response) {
          if (response.ok) return response.json();
          if (response.status === 401) throw new Error("Invalid email or password.");
          throw new Error("Login failed.");
        })
        .then(function (data) {
          if (data && data.id != null) {
            try {
              if (currentRole === "customer") localStorage.setItem("foodyClientId", String(data.id));
              else if (currentRole === "restaurant") localStorage.setItem("foodyRestaurantId", String(data.id));
              else if (currentRole === "delivery") localStorage.setItem("foodyDriverId", String(data.id));
            } catch (e) {}
          }
          window.location.href = redirectUrl;
        })
        .catch(function (error) {
          alert(error.message || "Something went wrong.");
        });
    });
  }
});
