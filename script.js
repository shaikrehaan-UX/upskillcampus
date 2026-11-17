
// ==================================================
// FOODIEHUB - COMPLETE JAVASCRIPT
// Cart + Registration + Login + Checkout + My Orders
// ==================================================

const API_URL = "http://localhost:8080/api/customers";
const ORDER_API_URL = "http://localhost:8080/api/orders";

// Food IDs must match your MySQL food_items table.
const FOOD_IDS = {
    "chicken biryani": 1,
    "cheese pizza": 2,
    "classic burger": 3,
    "french fries": 4
};

// ==================================================
// CART DATA
// ==================================================

let cart = JSON.parse(localStorage.getItem("foodiehubCart")) || [];
let cartPanelOpen = false;

// Restore food IDs for existing cart items.
cart = cart.map(item => ({
    ...item,
    foodId: item.foodId ||
        FOOD_IDS[String(item.name || "").trim().toLowerCase()] || null
}));

function saveCart() {
    localStorage.setItem("foodiehubCart", JSON.stringify(cart));
    updateCartCount();

    if (cartPanelOpen) {
        renderCart();
    }
}

function updateCartCount() {
    const countElement = document.getElementById("cart-count");

    if (countElement) {
        countElement.textContent = cart.reduce(
            (total, item) => total + Number(item.quantity),
            0
        );
    }
}

function addToCart(name, price, foodId) {
    const existingItem = cart.find(item => item.name === name);

    if (existingItem) {
        existingItem.quantity++;

        if (!existingItem.foodId) {
            existingItem.foodId = foodId ||
                FOOD_IDS[String(name).trim().toLowerCase()] || null;
        }
    } else {
        cart.push({
            name,
            price,
            foodId: foodId ||
                FOOD_IDS[String(name).trim().toLowerCase()] || null,
            quantity: 1
        });
    }

    saveCart();
    showCartNotification(name + " added to your cart!");
}

function increaseQuantity(index) {
    if (!cart[index]) return;

    cart[index].quantity++;
    saveCart();
}

function decreaseQuantity(index) {
    if (!cart[index]) return;

    cart[index].quantity--;

    if (cart[index].quantity <= 0) {
        cart.splice(index, 1);
    }

    saveCart();
}

function removeItem(index) {
    if (!cart[index]) return;

    cart.splice(index, 1);
    saveCart();
}

function getCartTotal() {
    return cart.reduce(
        (total, item) =>
            total + Number(item.price) * Number(item.quantity),
        0
    );
}

function showCartNotification(message) {
    let notification = document.getElementById("cart-notification");

    if (!notification) {
        notification = document.createElement("div");
        notification.id = "cart-notification";
        document.body.appendChild(notification);
    }

    notification.textContent = message;
    notification.style.display = "block";

    clearTimeout(window.cartNotificationTimeout);

    window.cartNotificationTimeout = setTimeout(() => {
        notification.style.display = "none";
    }, 2000);
}

// ==================================================
// HTML ESCAPE FUNCTION
// ==================================================

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

// ==================================================
// CART PANEL DESIGN
// ==================================================

const cartStyles = document.createElement("style");

cartStyles.textContent = `
#cart-panel {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.55);
    z-index: 2000;
    justify-content: flex-end;
}

#cart-panel.active {
    display: flex;
}

.cart-content {
    background: #fff;
    width: 100%;
    max-width: 460px;
    height: 100%;
    padding: 25px;
    overflow-y: auto;
    box-shadow: -5px 0 25px rgba(0,0,0,0.15);
}

.cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
}

.cart-header h2 {
    font-size: 23px;
    color: #222;
}

#close-cart {
    background: none;
    border: none;
    font-size: 28px;
    cursor: pointer;
    color: #555;
}

.cart-empty {
    text-align: center;
    padding: 65px 10px;
    color: #777;
}

.cart-empty .empty-icon {
    font-size: 65px;
    margin-bottom: 15px;
}

.cart-item {
    display: flex;
    align-items: center;
    gap: 13px;
    padding: 18px 0;
    border-bottom: 1px solid #eee;
}

.cart-item-info {
    flex: 1;
    min-width: 0;
}

.cart-item-info h3 {
    font-size: 15px;
    margin-bottom: 6px;
    color: #222;
}

.cart-item-info p {
    color: #ff5722;
    font-weight: 600;
    font-size: 14px;
}

.quantity-controls {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 9px;
}

.quantity-controls button {
    border: none;
    background: #fff0e9;
    color: #ff5722;
    width: 27px;
    height: 27px;
    border-radius: 5px;
    cursor: pointer;
    font-weight: bold;
}

.quantity-controls span {
    font-weight: 600;
}

.remove-item {
    border: none;
    background: transparent;
    color: #d93025;
    cursor: pointer;
    font-size: 13px;
    padding: 8px;
}

.cart-summary {
    margin-top: 25px;
    padding-top: 20px;
    border-top: 1px solid #eee;
}

.cart-total {
    display: flex;
    justify-content: space-between;
    font-size: 19px;
    font-weight: 700;
    margin-bottom: 20px;
}

.cart-total span:last-child {
    color: #ff5722;
}

.checkout-btn {
    width: 100%;
    padding: 15px;
    background: #ff5722;
    border: none;
    border-radius: 8px;
    color: white;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
}

.checkout-btn:hover {
    background: #e64a19;
}

.checkout-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

#cart-notification {
    display: none;
    position: fixed;
    bottom: 25px;
    right: 25px;
    background: #222;
    color: white;
    padding: 14px 22px;
    border-radius: 8px;
    z-index: 3000;
    box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}

@media(max-width: 500px) {
    .cart-content {
        max-width: 100%;
    }
}
`;

document.head.appendChild(cartStyles);

// ==================================================
// CREATE CART PANEL
// ==================================================

const cartPanel = document.createElement("div");
cartPanel.id = "cart-panel";

cartPanel.innerHTML = `
    <div class="cart-content">
        <div class="cart-header">
            <h2>🛒 Your Cart</h2>
            <button id="close-cart" aria-label="Close cart">&times;</button>
        </div>

        <div id="cart-items"></div>
        <div id="cart-summary"></div>
    </div>
`;

document.body.appendChild(cartPanel);

function renderCart() {
    const cartItems = document.getElementById("cart-items");
    const cartSummary = document.getElementById("cart-summary");

    if (!cartItems || !cartSummary) return;

    if (cart.length === 0) {
        cartItems.innerHTML = `
            <div class="cart-empty">
                <div class="empty-icon">🛍️</div>
                <h3>Your cart is empty!</h3>
                <p>Add some delicious food to get started.</p>
            </div>
        `;

        cartSummary.innerHTML = "";
        return;
    }

    cartItems.innerHTML = cart.map((item, index) => `
        <div class="cart-item">
            <div class="cart-item-info">
                <h3>${escapeHTML(item.name)}</h3>
                <p>₹${(
                    Number(item.price) * Number(item.quantity)
                ).toFixed(2)}</p>

                <div class="quantity-controls">
                    <button data-action="decrease"
                        data-index="${index}">−</button>

                    <span>${item.quantity}</span>

                    <button data-action="increase"
                        data-index="${index}">+</button>
                </div>
            </div>

            <button class="remove-item"
                data-action="remove"
                data-index="${index}">
                Remove
            </button>
        </div>
    `).join("");

    cartSummary.innerHTML = `
        <div class="cart-summary">
            <div class="cart-total">
                <span>Total Amount</span>
                <span>₹${getCartTotal().toFixed(2)}</span>
            </div>

            <button class="checkout-btn" id="checkout-button">
                Proceed to Checkout
            </button>
        </div>
    `;
}

function openCart() {
    cartPanel.classList.add("active");
    cartPanelOpen = true;
    renderCart();
}

function closeCart() {
    cartPanel.classList.remove("active");
    cartPanelOpen = false;
}

cartPanel.addEventListener("click", function(event) {
    if (event.target === cartPanel) {
        closeCart();
    }
});

document.getElementById("close-cart")
    .addEventListener("click", closeCart);

cartPanel.addEventListener("click", function(event) {
    const button = event.target.closest("button[data-action]");

    if (!button) return;

    const index = Number(button.dataset.index);
    const action = button.dataset.action;

    if (action === "increase") increaseQuantity(index);
    if (action === "decrease") decreaseQuantity(index);
    if (action === "remove") removeItem(index);
});

// ==================================================
// CUSTOMER AUTHENTICATION
// ==================================================

const authModal = document.getElementById("auth-modal");
const loginButton = document.getElementById("login-button");
const closeAuthButton = document.getElementById("close-auth");

const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");

const authTitle = document.getElementById("auth-title");
const authSubtitle = document.getElementById("auth-subtitle");
const authMessage = document.getElementById("auth-message");

function showAuthMessage(message, success = false) {
    if (!authMessage) return;

    authMessage.textContent = message;
    authMessage.style.color = success ? "green" : "#d93025";
}

function openLoginModal() {
    if (!authModal) return;

    authModal.style.display = "flex";
    showLoginForm();
}

function closeLoginModal() {
    if (!authModal) return;

    authModal.style.display = "none";
    showAuthMessage("");
}

function showLoginForm() {
    if (!loginForm || !registerForm) return;

    loginForm.style.display = "block";
    registerForm.style.display = "none";

    if (authTitle) authTitle.textContent = "Welcome Back!";
    if (authSubtitle) {
        authSubtitle.textContent = "Login to your FoodieHub account";
    }

    showAuthMessage("");
}

function showRegisterForm() {
    if (!loginForm || !registerForm) return;

    loginForm.style.display = "none";
    registerForm.style.display = "block";

    if (authTitle) authTitle.textContent = "Create Account";
    if (authSubtitle) authSubtitle.textContent = "Join FoodieHub today!";

    showAuthMessage("");
}

if (loginButton) {
    loginButton.addEventListener("click", function() {
        if (sessionStorage.getItem("foodiehubUser")) {
            alert(
                "You are logged in as " +
                sessionStorage.getItem("foodiehubUser")
            );
        } else {
            openLoginModal();
        }
    });
}

if (closeAuthButton) {
    closeAuthButton.addEventListener("click", closeLoginModal);
}

if (authModal) {
    authModal.addEventListener("click", function(event) {
        if (event.target === authModal) {
            closeLoginModal();
        }
    });
}

document.addEventListener("keydown", function(event) {
    if (event.key === "Escape") {
        closeLoginModal();
        closeCart();
    }
});

const showRegisterButton = document.getElementById("show-register");
const showLoginButton = document.getElementById("show-login");

if (showRegisterButton) {
    showRegisterButton.addEventListener("click", function(event) {
        event.preventDefault();
        showRegisterForm();
    });
}

if (showLoginButton) {
    showLoginButton.addEventListener("click", function(event) {
        event.preventDefault();
        showLoginForm();
    });
}

// ==================================================
// CUSTOMER REGISTRATION
// ==================================================

if (registerForm) {
    registerForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const fullName =
            document.getElementById("register-name").value.trim();

        const email =
            document.getElementById("register-email").value.trim();

        const password =
            document.getElementById("register-password").value;

        const phone =
            document.getElementById("register-phone").value.trim();

        const address =
            document.getElementById("register-address").value.trim();

        showAuthMessage("Registering your account...", true);

        try {
            const response = await fetch(`${API_URL}/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    fullName,
                    email,
                    password,
                    phone,
                    address
                })
            });

            const result = await response.text();

            if (
                response.ok &&
                result.toLowerCase().includes("successfully")
            ) {
                showAuthMessage(result, true);
                registerForm.reset();

                document.getElementById("login-email").value = email;

                setTimeout(function() {
                    showLoginForm();

                    showAuthMessage(
                        "Registration successful! Please login.",
                        true
                    );
                }, 1200);

            } else {
                showAuthMessage(result || "Registration failed.");
            }

        } catch (error) {
            console.error("Registration error:", error);

            showAuthMessage(
                "Cannot connect to the server. Check whether the backend is running."
            );
        }
    });
}

// ==================================================
// CUSTOMER LOGIN
// ==================================================

if (loginForm) {
    loginForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const email =
            document.getElementById("login-email").value.trim();

        const password =
            document.getElementById("login-password").value;

        showAuthMessage("Logging in...", true);

        try {
            const response = await fetch(`${API_URL}/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email,
                    password
                })
            });

            const result = await response.text();

            if (response.ok && result.startsWith("Welcome,")) {
                showAuthMessage(result, true);

                sessionStorage.setItem(
                    "foodiehubUser",
                    result.replace("Welcome,", "")
                        .replace("!", "")
                        .trim()
                );

                // Required for checkout and My Orders.
                sessionStorage.setItem("foodiehubEmail", email);

                if (loginButton) {
                    loginButton.textContent = "My Account";
                }

                loginForm.reset();

                setTimeout(closeLoginModal, 1200);

            } else {
                showAuthMessage(
                    result || "Login failed. Please try again."
                );
            }

        } catch (error) {
            console.error("Login error:", error);

            showAuthMessage(
                "Cannot connect to the server. Check whether the backend is running."
            );
        }
    });
}

// ==================================================
// CHECKOUT AND ORDER PLACEMENT
// ==================================================

cartPanel.addEventListener("click", async function(event) {
    if (event.target.id !== "checkout-button") return;

    if (cart.length === 0) return;

    const email = sessionStorage.getItem("foodiehubEmail");

    if (!email) {
        alert("Please login before placing your order.");
        openLoginModal();
        return;
    }

    const missingFood = cart.find(item => !item.foodId);

    if (missingFood) {
        alert(
            missingFood.name +
            " is not linked to a database food ID. Please check the menu."
        );
        return;
    }

    const deliveryAddress = prompt("Enter your delivery address:");

    if (deliveryAddress === null) return;

    if (!deliveryAddress.trim()) {
        alert("Please enter a delivery address.");
        return;
    }

    const checkoutButton =
        document.getElementById("checkout-button");

    if (checkoutButton) {
        checkoutButton.disabled = true;
        checkoutButton.textContent = "Placing Order...";
    }

    try {
        const response = await fetch(`${ORDER_API_URL}/place`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                deliveryAddress: deliveryAddress.trim(),
                items: cart.map(item => ({
                    foodId: Number(item.foodId),
                    quantity: Number(item.quantity)
                }))
            })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(
                result.message || "Order could not be placed."
            );
        }

        alert(
            "Order placed successfully!\n\n" +
            "Order ID: " + result.orderId + "\n" +
            "Total Amount: ₹" + result.totalAmount + "\n" +
            "Status: " + result.orderStatus
        );

        cart = [];
        saveCart();
        closeCart();

    } catch (error) {
        console.error("Order placement error:", error);

        alert(
            error.message ||
            "Unable to place your order. Please try again."
        );

    } finally {
        const button = document.getElementById("checkout-button");

        if (button) {
            button.disabled = false;
            button.textContent = "Proceed to Checkout";
        }
    }
});

// ==================================================
// SEARCH FUNCTIONALITY
// ==================================================

function searchFood() {
    const searchInput =
        document.getElementById("search-input") ||
        document.querySelector(".search-box input");

    if (!searchInput) return;

    const searchValue = searchInput.value.trim().toLowerCase();

    document.querySelectorAll(
        ".food-card, .restaurant-card"
    ).forEach(card => {
        card.style.display =
            card.textContent.toLowerCase().includes(searchValue)
                ? ""
                : "none";
    });
}

const searchButton = document.getElementById("search-button");

if (searchButton) {
    searchButton.addEventListener("click", searchFood);
}

const searchInput = document.getElementById("search-input");

if (searchInput) {
    searchInput.addEventListener("keydown", function(event) {
        if (event.key === "Enter") {
            searchFood();
        }
    });
}

// ==================================================
// ADD FOOD BUTTONS
// ==================================================

document.querySelectorAll(".food-card").forEach(card => {
    const button = card.querySelector(".add-btn");

    if (!button) return;

    button.addEventListener("click", function() {
        const name =
            card.querySelector("h3")?.textContent.trim();

        const priceText =
            card.querySelector(".price")?.textContent || "";

        const price = Number(
            priceText.replace(/[^\d.]/g, "")
        );

        if (name && Number.isFinite(price) && price > 0) {
            const foodId =
                Number(card.dataset.foodId) ||
                FOOD_IDS[name.toLowerCase()] ||
                null;

            addToCart(name, price, foodId);
        }
    });
});

// ==================================================
// CART NAVIGATION BUTTON
// ==================================================

const cartButton = document.getElementById("cart-button");

if (cartButton) {
    cartButton.addEventListener("click", openCart);
}

// ==================================================
// RESTAURANT MENU BUTTONS
// ==================================================

document.querySelectorAll(".view-menu-btn").forEach(button => {
    button.addEventListener("click", function() {
        document.getElementById("menu")?.scrollIntoView({
            behavior: "smooth"
        });
    });
});

// ==================================================
// MY ORDERS - STYLING
// ==================================================

const myOrdersButton =
    document.getElementById("my-orders-button");

const myOrdersSection =
    document.getElementById("my-orders-section");

const myOrdersList =
    document.getElementById("my-orders-list");

const ordersStyle = document.createElement("style");

ordersStyle.textContent = `
#my-orders-section {
    padding: 50px 20px;
}

#my-orders-list {
    max-width: 900px;
    margin: 25px auto;
}

.my-order-card {
    background: white;
    border: 1px solid #eee;
    border-radius: 12px;
    padding: 22px;
    margin-bottom: 20px;
    box-shadow: 0 5px 18px rgba(0,0,0,0.06);
}

.my-order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    border-bottom: 1px solid #eee;
    padding-bottom: 15px;
}

.my-order-status {
    padding: 7px 14px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
}

.my-order-item {
    display: flex;
    justify-content: space-between;
    gap: 15px;
    padding: 10px 0;
    color: #555;
}

.my-order-details {
    color: #777;
    font-size: 13px;
    margin-top: 8px;
}

.my-order-total {
    color: #ff5722;
    font-size: 20px;
    font-weight: 700;
}

.my-orders-message {
    text-align: center;
    padding: 35px;
    background: white;
    border-radius: 12px;
    color: #777;
}

#retry-orders-button {
    margin-top: 15px;
    padding: 10px 20px;
    border: none;
    background: #ff5722;
    color: white;
    border-radius: 7px;
    cursor: pointer;
}
`;

document.head.appendChild(ordersStyle);

// ==================================================
// FORMAT ORDER DATE
// ==================================================

function formatOrderDate(dateValue) {
    const date = new Date(dateValue);

    if (isNaN(date.getTime())) {
        return "Date unavailable";
    }

    return date.toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

// ==================================================
// LOAD CUSTOMER ORDERS
// ==================================================

async function loadMyOrders() {
    const email = sessionStorage.getItem("foodiehubEmail");

    if (!email) {
        alert("Please login to view your orders.");
        openLoginModal();
        return;
    }

    if (!myOrdersSection || !myOrdersList) {
        alert("My Orders section was not found in index.html.");
        return;
    }

    myOrdersSection.style.display = "block";

    myOrdersList.innerHTML = `
        <p class="my-orders-message">
            Loading your orders...
        </p>
    `;

    myOrdersSection.scrollIntoView({
        behavior: "smooth"
    });

    try {
        const response = await fetch(
            `${ORDER_API_URL}/my-orders?email=${encodeURIComponent(email)}`
        );

        const orders = await response.json();

        if (!response.ok) {
            throw new Error(
                orders.message || "Unable to load orders."
            );
        }

        if (!Array.isArray(orders) || orders.length === 0) {
            myOrdersList.innerHTML = `
                <div class="my-orders-message">
                    <h2>📦 No Orders Yet!</h2>
                    <p>Your previous orders will appear here.</p>
                </div>
            `;

            return;
        }

        myOrdersList.innerHTML = orders.map(order => {
            const itemsHTML = (order.items || []).map(item => `
                <div class="my-order-item">
                    <span>
                        ${escapeHTML(item.foodName)}
                        × ${Number(item.quantity)}
                    </span>

                    <strong>
                        ₹${(
                            Number(item.itemPrice) *
                            Number(item.quantity)
                        ).toFixed(2)}
                    </strong>
                </div>
            `).join("");

            const status = String(order.orderStatus || "Pending");
            const normalizedStatus = status.toLowerCase();

            let statusColor = "#fff3cd";
            let textColor = "#856404";

            if (normalizedStatus.includes("deliver")) {
                statusColor = "#e8f5e9";
                textColor = "#2e7d32";
            } else if (normalizedStatus.includes("cancel")) {
                statusColor = "#ffebee";
                textColor = "#c62828";
            } else if (normalizedStatus.includes("prepar")) {
                statusColor = "#e3f2fd";
                textColor = "#1565c0";
            }

            return `
                <div class="my-order-card">

                    <div class="my-order-header">
                        <div>
                            <h3>
                                Order #${escapeHTML(order.orderId)}
                            </h3>

                            <p class="my-order-details">
                                ${escapeHTML(
                                    formatOrderDate(order.orderDate)
                                )}
                            </p>
                        </div>

                        <span class="my-order-status"
                            style="background:${statusColor};
                                   color:${textColor};">
                            ${escapeHTML(status)}
                        </span>
                    </div>

                    <div style="padding:15px 0;">
                        ${itemsHTML}
                    </div>

                    <div class="my-order-header">
                        <div class="my-order-details">
                            📍 ${escapeHTML(order.deliveryAddress)}
                        </div>

                        <div class="my-order-total">
                            ₹${Number(order.totalAmount).toFixed(2)}
                        </div>
                    </div>

                </div>
            `;
        }).join("");

    } catch (error) {
        console.error("My Orders Error:", error);

        myOrdersList.innerHTML = `
            <div class="my-orders-message">
                <h3>Unable to load your orders</h3>
                <p>${escapeHTML(error.message)}</p>

                <button id="retry-orders-button">
                    Try Again
                </button>
            </div>
        `;

        document.getElementById("retry-orders-button")
            ?.addEventListener("click", loadMyOrders);
    }
}

// ==================================================
// MY ORDERS BUTTON CLICK
// ==================================================

if (myOrdersButton) {
    myOrdersButton.addEventListener("click", loadMyOrders);
}

// ==================================================
// RESTORE LOGIN DISPLAY
// ==================================================

const savedUser = sessionStorage.getItem("foodiehubUser");

if (savedUser && loginButton) {
    loginButton.textContent = "My Account";
}

// ==================================================
// INITIALIZE FOODIEHUB
// ==================================================

saveCart();
updateCartCount();

console.log("FoodieHub loaded successfully!");

// Automatically refresh My Orders every 10 seconds
setInterval(() => {
    const ordersSection = document.getElementById("my-orders-section");
    const customerEmail = sessionStorage.getItem("foodiehubEmail");

    if (
        ordersSection &&
        ordersSection.style.display !== "none" &&
        customerEmail
    ) {
        loadMyOrders();
    }
}, 10000);
