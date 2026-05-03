# 🍕 Awesome Pizza API - Quick Start Guide

## Requisiti
- Java 17+
- Maven 3.6+

## Avviare l'Applicazione

```bash
cd awesome-pizza
./mvnw spring-boot:run
```

L'applicazione partirà su **http://localhost:8080**

### H2 Database Console
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (lasciare vuoto)

### API Documentation (Swagger)
```
http://localhost:8080/swagger-ui.html
```

---

## 📡 API Examples (cURL)

### 1️⃣ Create Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "pizzas": [
      {"name": "Margherita", "quantity": 2},
      {"name": "Pepperoni", "quantity": 1},
      {"name": "Hawaiian", "quantity": 1}
    ]
  }'
```

**Response:**
```json
{
  "id": 1,
  "code": "A7F2B9E1",
  "status": "RECEIVED",
  "pizzas": [
    {"id": 1, "name": "Margherita", "quantity": 2},
    {"id": 2, "name": "Pepperoni", "quantity": 1},
    {"id": 3, "name": "Hawaiian", "quantity": 1}
  ],
  "createdAt": "2024-04-28T10:30:00",
  "updatedAt": "2024-04-28T10:30:00"
}
```

---

### 2️⃣ Get Order by Code (Customer)
```bash
curl http://localhost:8080/orders/A7F2B9E1
```

---

### 3️⃣ View All Orders (Pizza Maker Dashboard)
```bash
curl http://localhost:8080/orders
```

---

### 4️⃣ View Order Queue (Only RECEIVED orders)
```bash
curl http://localhost:8080/orders/queue/waiting
```

---

### 5️⃣ Start Order (Pizza Maker takes order)
```bash
curl -X PUT http://localhost:8080/orders/1/start
```
Changes status: `RECEIVED` → `IN_PROGRESS`

---

### 6️⃣ Mark as Ready
```bash
curl -X PUT http://localhost:8080/orders/1/ready
```
Changes status: `IN_PROGRESS` → `READY`

---

### 7️⃣ Complete Order
```bash
curl -X PUT http://localhost:8080/orders/1/complete
```
Changes status: `READY` → `COMPLETED`

---

## 🔄 Full Order Lifecycle

```
1. Customer creates order        → POST /orders
                                    ↓ (Status: RECEIVED)
2. Customer checks status        → GET /orders/{code}

3. Pizza Maker views queue       → GET /orders/queue/waiting
                                    ↓
4. Pizza Maker takes order       → PUT /orders/{id}/start
                                    ↓ (Status: IN_PROGRESS)
5. Pizza Maker marks ready       → PUT /orders/{id}/ready
                                    ↓ (Status: READY)
6. Pizza Maker completes         → PUT /orders/{id}/complete
                                    ↓ (Status: COMPLETED)
7. Customer picks up order
```

---

## 🧪 Run Tests

```bash
./mvnw test
```

### Test Coverage
- **OrderServiceTest**: 13 unit tests using Mockito
- **AwesomePizzaApplicationIntegrationTests**: Spring Boot integration test
- Overall coverage: >80%

---

## 🛠️ Build JAR

```bash
./mvnw clean package -DskipTests
```

Run JAR:
```bash
java -jar target/awesome-pizza-0.0.1-SNAPSHOT.jar
```

---

## 📝 Project Structure

```
src/main/java/it/adesso/awesomepizza/
├── domain/entity/       # JPA Entities (Order, Pizza, OrderStatus)
├── application/
│   ├── dto/            # Data Transfer Objects
│   ├── service/        # Business Logic (OrderService)
│   └── mapper/         # Entity ↔ DTO Mapping
├── infrastructure/
│   ├── controller/     # REST Endpoints
│   ├── repository/     # JPA Repository
│   └── exception/      # Exception Handling
└── AwesomePizzaApplication.java  # Entry Point

src/test/java/
├── AwesomePizzaApplicationTests.java
├── AwesomePizzaApplicationIntegrationTests.java
└── application/service/OrderServiceTest.java
```

---

## 🎯 Technologies

| Component | Version |
|-----------|---------|
| Java | 17+ |
| Spring Boot | 4.0.6 |
| Spring Data JPA | 2025.1.5 |
| H2 Database | 2.4.240 |
| Lombok | Latest |
| JUnit 5 | 6.0.3 |
| Mockito | 5.20.0 |

---

## ❌ Error Handling

### 400: Bad Request (Validation Error)
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"pizzas":[]}'
```

Response:
```json
{
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": [
    {
      "field": "pizzas",
      "message": "At least one pizza must be provided"
    }
  ]
}
```

### 404: Not Found
```bash
curl http://localhost:8080/orders/INVALID
```

Response:
```json
{
  "status": 404,
  "message": "Order not found with code: INVALID"
}
```

---

## 🚀 Production Deployment

To deploy to production:

1. **Database**: Replace H2 with PostgreSQL/MySQL
2. **Security**: Add Spring Security + JWT/OAuth2
3. **Monitoring**: Enable Actuator endpoints
4. **Logging**: Configure centralized logging (ELK)
5. **Docker**: Create Dockerfile for containerization

---

## 📚 Clean Code Principles Applied

✅ Single Responsibility Principle  
✅ Dependency Injection  
✅ Data Transfer Objects (DTOs)  
✅ Mapper Pattern  
✅ Service Layer Pattern  
✅ Global Exception Handling  
✅ Comprehensive Logging  
✅ Input Validation  
✅ Immutability (Builders)  
✅ Unit & Integration Tests  

---

## 👨‍💻 Author

Backend Service created following enterprise-grade Spring Boot best practices.

---

## 📖 Notes

- **No Authentication**: This demo doesn't require authentication
- **Sequential Processing**: Orders are processed FIFO
- **Unique Codes**: Generated as UUID (first 8 characters, uppercase)
- **Database**: H2 in-memory (resets on restart)

Enjoy! 🍕

