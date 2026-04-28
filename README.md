# 🍕 Awesome Pizza API

Production-ready backend service for managing pizza orders with a clean layered architecture.

## 🚀 Tech Stack

* **Java 17+** - Modern Java version
* **Spring Boot 4.0.6** - Latest stable release
* **Spring Data JPA** - ORM and data access layer
* **H2 Database** - In-memory database (development)
* **Jakarta Validation** - Input validation
* **Lombok** - Reduce boilerplate code
* **JUnit 5 & Mockito** - Unit testing framework
* **SpringDoc OpenAPI** - API documentation

## 🏗️ Architecture

This project follows **clean layered architecture** with clear separation of concerns:

```
├── controller/          # REST API endpoints
├── service/            # Business logic
├── repository/         # Data access layer
├── domain/
│   └── entity/         # JPA entities
├── dto/                # Data Transfer Objects
├── mapper/             # Entity ↔ DTO mapping
└── exception/          # Exception handling
```

## 📦 Features

### ✅ Core Features
* Create pizza orders without authentication
* Track order status via unique code
* Manage order queue for pizza makers
* Complete order lifecycle management
* Database persistence with JPA/Hibernate

### ✅ Technical Features
* RESTful API (no frontend)
* Input validation (Jakarta Validation)
* Global exception handling (ControllerAdvice)
* Comprehensive logging
* Unit tests with >80% coverage
* H2 database console for debugging
* OpenAPI/Swagger documentation

## 📡 API Endpoints

### 🛍️ Customer Endpoints

#### Create Order
**POST** `/orders`

Creates a new pizza order.

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "pizzas": [
      {"name": "Margherita", "quantity": 2},
      {"name": "Pepperoni", "quantity": 1}
    ]
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "code": "A7F2B9E1",
  "status": "RECEIVED",
  "pizzas": [
    {"id": 1, "name": "Margherita", "quantity": 2},
    {"id": 2, "name": "Pepperoni", "quantity": 1}
  ],
  "createdAt": "2024-04-28T10:30:00",
  "updatedAt": "2024-04-28T10:30:00"
}
```

#### Get Order Status by Code
**GET** `/orders/{code}`

Retrieve order status using the unique code provided at order creation.

```bash
curl http://localhost:8080/orders/A7F2B9E1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "code": "A7F2B9E1",
  "status": "IN_PROGRESS",
  "pizzas": [
    {"id": 1, "name": "Margherita", "quantity": 2},
    {"id": 2, "name": "Pepperoni", "quantity": 1}
  ],
  "createdAt": "2024-04-28T10:30:00",
  "updatedAt": "2024-04-28T10:32:00"
}
```

### 👨‍💼 Pizza Maker (Admin) Endpoints

#### View All Orders
**GET** `/orders`

List all orders in the system (sorted by newest first).

```bash
curl http://localhost:8080/orders
```

**Response (200 OK):**
```json
[
  {
    "id": 2,
    "code": "C3X5K8M2",
    "status": "RECEIVED",
    "pizzas": [{"id": 3, "name": "Margherita", "quantity": 1}],
    "createdAt": "2024-04-28T10:35:00",
    "updatedAt": "2024-04-28T10:35:00"
  },
  {
    "id": 1,
    "code": "A7F2B9E1",
    "status": "COMPLETED",
    "pizzas": [{"id": 1, "name": "Margherita", "quantity": 2}],
    "createdAt": "2024-04-28T10:30:00",
    "updatedAt": "2024-04-28T10:40:00"
  }
]
```

#### View Order Queue
**GET** `/orders/queue/waiting`

Get the queue of waiting orders (RECEIVED status only).

```bash
curl http://localhost:8080/orders/queue/waiting
```

#### Start Order (IN_PROGRESS)
**PUT** `/orders/{id}/start`

Start preparing an order (change status from RECEIVED to IN_PROGRESS).

```bash
curl -X PUT http://localhost:8080/orders/1/start \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "id": 1,
  "code": "A7F2B9E1",
  "status": "IN_PROGRESS",
  ...
}
```

#### Mark Order as Ready
**PUT** `/orders/{id}/ready`

Mark order as ready for pickup (change status from IN_PROGRESS to READY).

```bash
curl -X PUT http://localhost:8080/orders/1/ready \
  -H "Content-Type: application/json"
```

#### Complete Order
**PUT** `/orders/{id}/complete`

Mark order as completed (change status from READY to COMPLETED).

```bash
curl -X PUT http://localhost:8080/orders/1/complete \
  -H "Content-Type: application/json"
```

## 🔄 Order Status Flow

```
RECEIVED → IN_PROGRESS → READY → COMPLETED
```

* **RECEIVED**: Order created and waiting in queue
* **IN_PROGRESS**: Pizza maker has taken the order and started preparing
* **READY**: Order is ready for pickup
* **COMPLETED**: Order has been picked up

## ▶️ Run Project

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build and Run

```bash
cd awesome-pizza

# Build the project
./mvnw clean build

# Run the application
./mvnw spring-boot:run
```

The application will start on **http://localhost:8080**

### Access H2 Console

Open your browser and navigate to:

```
http://localhost:8080/h2-console
```

**Connection details:**
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User Name**: `sa`
- **Password**: (leave empty)

### Access API Documentation (Swagger)

```
http://localhost:8080/swagger-ui.html
```

## 🧪 Run Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

### Test Coverage

* **Unit Tests**: `OrderServiceTest.java` - Service layer tests with Mockito
* **Integration Tests**: `AwesomePizzaApplicationIntegrationTests.java` - End-to-end API tests

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/it/adesso/awesome_pizza/
│   │   ├── AwesomePizzaApplication.java      # Spring Boot entry point
│   │   ├── application/
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── PizzaDTO.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── mapper/
│   │   │   │   └── OrderMapper.java           # Entity ↔ DTO mapping
│   │   │   └── service/
│   │   │       └── OrderService.java          # Business logic
│   │   ├── domain/
│   │   │   └── entity/
│   │   │       ├── Order.java                 # Order entity
│   │   │       ├── Pizza.java                 # Pizza entity
│   │   │       └── OrderStatus.java           # Status enum
│   │   └── infrastructure/
│   │       ├── controller/
│   │       │   └── OrderController.java       # REST endpoints
│   │       ├── exception/
│   │       │   ├── OrderNotFoundException.java
│   │       │   └── GlobalExceptionHandler.java
│   │       └── repository/
│   │           └── OrderRepository.java       # Data access
│   └── resources/
│       └── application.yaml                   # Configuration
└── test/
    └── java/it/adesso/awesome_pizza/
        ├── AwesomePizzaApplicationIntegrationTests.java
        └── application/service/
            └── OrderServiceTest.java
```

## 🎯 Clean Code Principles Applied

1. **Single Responsibility Principle**: Each class has one reason to change
2. **Dependency Injection**: Loose coupling via constructor injection
3. **DTOs**: Separation between API and data models
4. **Mapper Pattern**: Clear entity-to-DTO transformation
5. **Service Layer**: Business logic isolated from HTTP concerns
6. **Exception Handling**: Centralized via ControllerAdvice
7. **Logging**: Comprehensive logging for debugging
8. **Validation**: Input validation at DTO level
9. **Immutability**: DTOs use builders for cleaner instantiation
10. **Testing**: Unit and integration tests with high coverage

## 📝 Example Workflow

### 1. Customer Creates Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "pizzas": [{"name": "Margherita", "quantity": 2}]
  }'
```
Returns: `{"code": "ABC123", "status": "RECEIVED"}`

### 2. Customer Checks Status
```bash
curl http://localhost:8080/orders/ABC123
```

### 3. Pizza Maker Views Queue
```bash
curl http://localhost:8080/orders/queue/waiting
```

### 4. Pizza Maker Starts Order
```bash
curl -X PUT http://localhost:8080/orders/1/start
```

### 5. Pizza Maker Marks Ready
```bash
curl -X PUT http://localhost:8080/orders/1/ready
```

### 6. Pizza Maker Completes Order
```bash
curl -X PUT http://localhost:8080/orders/1/complete
```

## ⚠️ Error Handling

The API returns appropriate HTTP status codes and detailed error messages:

### 400 Bad Request - Validation Error
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-04-28T10:30:00",
  "path": "/orders",
  "fieldErrors": [
    {
      "field": "pizzas",
      "message": "At least one pizza must be provided"
    }
  ]
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Order not found with code: INVALID",
  "timestamp": "2024-04-28T10:30:00",
  "path": "/orders/INVALID"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2024-04-28T10:30:00",
  "path": "/orders"
}
```

## 🧠 Design Decisions

### Why H2 Database?
- In-memory database perfect for development
- No external dependencies
- Data resets on restart (clean state)
- H2 console for easy debugging

### Why DTOs?
- Decouple API contracts from domain models
- Validation at entry point
- Flexibility for future changes

### Why Layered Architecture?
- Clear separation of concerns
- Easy to test (mock dependencies)
- Maintainability and scalability

### Why Logging?
- Debug issues in production
- Track business events
- Monitor system health

## 📖 Notes

* **No Authentication**: This is a demo API without auth. In production, add OAuth2/JWT
* **Development Use**: H2 is in-memory. For production, use PostgreSQL or MySQL
* **Queue Processing**: Orders are processed sequentially (FIFO)
* **Unique Codes**: Generated as UUID first 8 characters (uppercase)

## 🚀 Production Deployment

To deploy to production:

1. Replace H2 with PostgreSQL/MySQL in `pom.xml`
2. Add authentication/authorization (Spring Security)
3. Configure external database in `application.yaml`
4. Enable HTTPS
5. Add rate limiting and API key validation
6. Set up monitoring and alerting
7. Add database migration tool (Flyway/Liquibase)

## 📄 License

This project is provided as-is for educational and commercial purposes.

## 👨‍💻 Author

Created as a production-ready Spring Boot template demonstrating clean architecture principles.
