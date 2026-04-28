# 🍕 Awesome Pizza API

Backend service for managing pizza orders.

## 🚀 Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* H2 Database
* JUnit & Mockito

## 📦 Features

* Create pizza orders without authentication
* Track order status via unique code
* Manage order queue for pizza makers

## 📡 API Endpoints

### Create Order

POST /orders

Example:

```json
{
  "pizzas": [
    { "name": "Margherita", "quantity": 2 }
  ]
}
```

### Get Order Status

GET /orders/{code}

### List Orders

GET /orders

### Start Order

PUT /orders/{id}/start

### Complete Order

PUT /orders/{id}/complete

## ▶️ Run Project

```bash
./mvnw spring-boot:run
```

H2 console:
http://localhost:8080/h2-console

## 🧪 Run Tests

```bash
./mvnw test
```

## 🧠 Notes

* No authentication required
* Orders processed sequentially
* Designed with clean architecture principles
