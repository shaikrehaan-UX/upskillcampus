# FoodieHub – Food Delivery Application

## Project Overview
FoodieHub is a full-stack food delivery web application developed using HTML, CSS, JavaScript, Java Spring Boot, and MySQL.

It allows customers to browse food items, add items to their cart, place orders, and track order status. Administrators can manage restaurants, food items, and customer orders.

## Features

### Customer Module
- Customer registration and login
- Browse food items
- Search food items
- Add items to cart
- Place food orders
- View order history
- Track order status

### Admin Module
- Admin login
- View customer orders
- Update order status
- Add, edit, and delete restaurants
- Add, edit, and delete food items
- Manage food delivery operations through the dashboard

## Technologies Used

- Frontend: HTML, CSS, JavaScript
- Backend: Java Spring Boot
- Database: MySQL
- Database Connectivity: JDBC
- Security: Spring Security and BCrypt
- Build Tool: Maven
- Version Control: Git and GitHub

## Project Structure

```text
FoodDeliveryApplication/
├── index.html
├── admin.html
├── style.css
├── script.js
├── README.md
├── .gitignore
└── foodiehub-backend/
    ├── pom.xml
    └── src/
```

## Requirements
- Java JDK
- Apache Maven
- MySQL Server
- Visual Studio Code
- Git

## How to Run

1. Clone this repository.
2. Create a MySQL database named `foodiehub`.
3. Configure the database environment variables:
   - FOODIEHUB_DB_URL
   - FOODIEHUB_DB_USER
   - FOODIEHUB_DB_PASSWORD
4. Open the `foodiehub-backend` directory.
5. Run the Spring Boot application:

```bash
mvn spring-boot:run
```

6. Open `index.html` using VS Code Live Server.

## Developer

Rehaan Shaik

Full Stack Development Intern
