# 🏡 Airbnb Clone  

> A **production-ready** Airbnb-like platform built with **Spring Boot, PostgreSQL, JWT Security, and Stripe Payments**.  
Designed with **scalability, concurrency safety, ACID compliance, and clean architecture** in mind.  

---

## ✨ Features  

- 🔐 **Authentication & Security** – JWT-based authentication with role-based access (`USER`, `GUEST`, `HOTEL_MANAGER`)  
- 🏨 **Hotel & Room Management** – Manage inventory, availability & surge pricing (Used **Decorator pattern** for the Pricing) 
- 📅 **Booking System** – Safe booking flow with **pessimistic locking** + `@Transactional` to ensure **atomicity & prevent race conditions**  
- 💳 **Stripe Integration** – Secure checkout & webhook handling for payment confirmations  
- ⏰ **Scheduled Cron Jobs** – Every 1 hour, cron jobs update room prices & apply surge factors dynamically  
- 🛡 **ACID Properties** – Transactions guarantee **consistency, isolation, and durability** across concurrent bookings  
- 👨‍💻 **SOLID Principles** – Codebase structured for maintainability and scalability  
- 📑 **API Documentation** – Swagger UI available at `/api/v1/swagger-ui/index.html`  
- ⚡ **Scalable Architecture** – Layered design (`Controller → Service → Repository`) with DTO mapping  

---

## 🛠 Tech Stack  

![Java](https://img.shields.io/badge/Java-17-blue)  
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen)  
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-yellowgreen)  
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)  
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-orange)  
![Stripe](https://img.shields.io/badge/Stripe-Payments-purple)  
![Swagger](https://img.shields.io/badge/Swagger-API%20Docs-green)  
![Maven](https://img.shields.io/badge/Maven-Build%20Tool-red)  

---

## System Designs 🏗️

![Screenshot](designs/Screenshot%202025-08-31%20200951.png)

![Screenshot](designs/Screenshot%202025-08-31%20201028.png)

![Screenshot](designs/Screenshot%202025-08-31%20201103.png)

![Screenshot](designs/Screenshot%202025-08-31%20201121.png)

![Screenshot](designs/Screenshot%202025-08-31%20201136.png)

---

## 🟢Swagger OpenAPI definition
http://localhost:8080/api/v1/swagger-ui/index.html#/
