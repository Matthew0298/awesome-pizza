# 🏗️ Awesome Pizza - Architecture Guide

## Architettura Generale

Questo progetto segue una **3-layer clean architecture** con separazione netta tra i layer di presentazione, business logic e data access.

```
┌─────────────────────────────────────────────────┐
│            REST API Layer (Controller)           │ ← HTTP Requests/Responses
├─────────────────────────────────────────────────┤
│                                                  │
│  OrderController                                 │
│  - POST   /orders                               │
│  - GET    /orders/{code}                        │
│  - GET    /orders                               │
│  - GET    /orders/queue/waiting                │
│  - PUT    /orders/{id}/start                   │
│  - PUT    /orders/{id}/ready                   │
│  - PUT    /orders/{id}/complete                │
│                                                  │
├─────────────────────────────────────────────────┤
│          Application Layer (Service)             │ ← Business Logic
├─────────────────────────────────────────────────┤
│                                                  │
│  OrderService                                    │
│  - createOrder()                                │
│  - getOrderByCode()                             │
│  - getOrderById()                               │
│  - getAllOrders()                               │
│  - getOrderQueue()                              │
│  - startOrder()                                 │
│  - markAsReady()                                │
│  - completeOrder()                              │
│                                                  │
├─────────────────────────────────────────────────┤
│      Infrastructure Layer (Repository)          │ ← Data Persistence
├─────────────────────────────────────────────────┤
│                                                  │
│  OrderRepository (Spring Data JPA)             │
│  H2 Database                                    │
│                                                  │
├─────────────────────────────────────────────────┤
│           Domain Layer (Entities)                │ ← Business Objects
├─────────────────────────────────────────────────┤
│                                                  │
│  Order (Entity)                                 │
│  Pizza (Entity)                                 │
│  OrderStatus (Enum)                            │
│                                                  │
└─────────────────────────────────────────────────┘
```

---

## 🗂️ Directory Structure

```java
src/main/java/it/adesso/awesomepizza/
│
├── AwesomePizzaApplication.java         // Entry Point (@SpringBootApplication)
│
├── domain/                               // Business Domain
│   └── entity/
│       ├── Order.java                  // JPA Entity (Order aggregate root)
│       ├── Pizza.java                  // JPA Entity (belongs to Order)
│       └── OrderStatus.java            // Enum: RECEIVED, IN_PROGRESS, READY, COMPLETED
│
├── application/                          // Application Layer (Orchestration)
│   ├── dto/                             // Data Transfer Objects
│   │   ├── OrderDTO.java               // Order DTO (API contract)
│   │   ├── PizzaDTO.java               // Pizza DTO
│   │   └── ErrorResponse.java          // Error DTO
│   │
│   ├── mapper/                          // Entity ↔ DTO Conversion
│   │   └── OrderMapper.java            // Maps between domain entities and DTOs
│   │
│   └── service/                         // Business Logic
│       └── OrderService.java           // Core service (transactional operations)
│
└── infrastructure/                      // Technical Implementation
    ├── controller/                      // REST API Endpoints
    │   └── OrderController.java        // HTTP request handlers
    │
    ├── repository/                      // Data Access Layer
    │   └── OrderRepository.java        // Spring Data JPA repository
    │
    └── exception/                       // Error Handling
        ├── OrderNotFoundException.java // Custom exception
        └── GlobalExceptionHandler.java // @ControllerAdvice for global error handling

src/test/java/it/adesso/awesomepizza/
├── AwesomePizzaApplicationTests.java
├── AwesomePizzaApplicationIntegrationTests.java
└── application/service/
    └── OrderServiceTest.java            // 13 unit tests with Mockito

src/main/resources/
└── application.yaml                     // Configuration
```

---

## 📊 Data Model

### Order Entity
```java
@Entity
public class Order {
    Long id;                              // Primary Key
    String code;                          // Unique order code (UUID substring)
    OrderStatus status;                   // Current status
    List<Pizza> pizzas;                  // One-to-Many relationship
    LocalDateTime createdAt;             // Creation timestamp
    LocalDateTime updatedAt;             // Last update timestamp
}
```

### Pizza Entity
```java
@Entity
public class Pizza {
    Long id;                              // Primary Key
    String name;                          // Pizza type
    Integer quantity;                     // How many pizzas
    Order order;                          // Many-to-One relationship
}
```

### OrderStatus Enum
```java
public enum OrderStatus {
    RECEIVED,      // Order created, waiting in queue
    IN_PROGRESS,   // Pizza maker took the order
    READY,         // Order is prepared
    COMPLETED      // Customer picked up
}
```

---

## 🔄 Data Flow

### 1. Create Order Flow
```
POST /orders with OrderDTO
    ↓
OrderController.createOrder()
    ↓
OrderService.createOrder()
    ↓ (Create Order + Pizza entities)
OrderRepository.save(order)
    ↓ (Persist to H2)
OrderMapper.toDTO(savedOrder)
    ↓
Response 201 Created with OrderDTO
```

### 2. Status Update Flow
```
PUT /orders/{id}/start
    ↓
OrderController.startOrder(id)
    ↓
OrderService.startOrder(id)
    ↓ (Validate current status)
order.setStatus(IN_PROGRESS)
    ↓
OrderRepository.save(order)
    ↓
OrderMapper.toDTO(updatedOrder)
    ↓
Response 200 OK with updated OrderDTO
```

---

## 🎯 Design Patterns Used

### 1. **Layered Architecture**
Separazione logica tra controller, service e repository.

### 2. **Data Transfer Object (DTO) Pattern**
DTOs separano le API contracts dalle entità di dominio, permettendo:
- Validazione al confine dell'API
- Evoluzione indipendente del dominio
- Protezione dei dati sensibili

### 3. **Mapper Pattern**
`OrderMapper` gestisce la conversione tra:
- Entity → DTO (per risposte API)
- DTO → Entity (per richieste API)

### 4. **Repository Pattern**
`OrderRepository` estende `JpaRepository<Order, Long>`:
- Query methods: `findByCode()`, `findByStatus()`
- Astrae i dettagli del database
- Facilita i test (mock repository)

### 5. **Service Layer Pattern**
`OrderService` contiene tutta la business logic:
- Validazione dello stato
- Transazioni
- Logging
- Orchestrazione tra repository e mapper

### 6. **Global Exception Handler (ControllerAdvice)**
`GlobalExceptionHandler` centralizza la gestione degli errori:
- `OrderNotFoundException` → 404
- `MethodArgumentNotValidException` → 400 con dettagli validazione
- `Exception` generico → 500

---

## 🔐 Validation & Security

### Input Validation (Jakarta Validation)
```java
@Valid
@NotEmpty(message = "At least one pizza must be provided")
private List<PizzaDTO> pizzas;

@NotBlank(message = "Pizza name cannot be blank")
private String name;

@Positive(message = "Quantity must be greater than 0")
private Integer quantity;
```

### Error Response Format
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

---

## 📝 Transaction Management

### Service Layer Transactionality
```java
@Service
@Transactional  // All methods are transactional
public class OrderService {
    
    @Transactional(readOnly = true)
    public OrderDTO getOrderByCode(String code) { ... }
    
    public OrderDTO createOrder(OrderDTO orderDTO) { ... }  // Write transaction
}
```

**Benefici:**
- Atomicità: Tutte le operazioni si completano o nessuna
- Isolamento: Nessun dirty read
- Rollback automatico su exception

---

## 🧪 Testing Strategy

### Unit Tests (Mockito)
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private OrderMapper orderMapper;
    @InjectMocks private OrderService orderService;
    
    @Test
    void testCreateOrder() { ... }  // 13 test methods
}
```

**Coverage:**
- CRUD operations
- Status transitions
- Error scenarios
- Business rule validations

### Integration Tests
```java
@SpringBootTest
class AwesomePizzaApplicationIntegrationTests {
    @Test
    void contextLoads() { ... }  // Application context loads
}
```

---

## 🌐 REST API Principles

### RESTful Conventions
| Method | Endpoint | Action |
|--------|----------|--------|
| POST | /orders | Create |
| GET | /orders/{code} | Read by code |
| GET | /orders | List all |
| GET | /orders/queue/waiting | List by status |
| PUT | /orders/{id}/start | Update status |
| PUT | /orders/{id}/ready | Update status |
| PUT | /orders/{id}/complete | Update status |

### HTTP Status Codes
- `201 Created` - POST successful
- `200 OK` - GET/PUT successful
- `400 Bad Request` - Validation error
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected error

---

## 📊 Database Schema

### Auto-generated by Hibernate (DDL-Auto: create-drop)

```sql
CREATE TABLE orders (
    id BIGINT IDENTITY PRIMARY KEY,
    code VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE pizza (
    id BIGINT IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    order_id BIGINT NOT NULL FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_order_code ON orders(code);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at DESC);
```

---

## 🔧 Configuration (application.yaml)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop              # Auto create schema
    show-sql: false                      # Don't log SQL
  datasource:
    url: jdbc:h2:mem:testdb             # In-memory H2
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    it.adesso.awesomepizza: DEBUG      # Application logs
```

---

## 🚀 Extension Points

### Aggiungi Funzionalità:

1. **Authentication/Authorization**
   ```java
   @PreAuthorize("hasRole('ADMIN')")
   public void startOrder(Long id) { ... }
   ```

2. **Caching**
   ```java
   @Cacheable("orders")
   public OrderDTO getOrderByCode(String code) { ... }
   ```

3. **Async Processing**
   ```java
   @Async
   public CompletableFuture<OrderDTO> createOrderAsync(OrderDTO dto) { ... }
   ```

4. **Event Publishing**
   ```java
   @Transactional
   public OrderDTO completeOrder(Long id) {
       Order order = ...;
       applicationEventPublisher.publishEvent(new OrderCompletedEvent(order));
   }
   ```

5. **Custom Validations**
   ```java
   @Component
   public class PizzaQuantityValidator implements ConstraintValidator<...> { }
   ```

---

## 📚 Dependencies

| Dependency | Purpose |
|-----------|---------|
| spring-boot-starter-web | REST API support |
| spring-boot-starter-data-jpa | ORM & Repository pattern |
| spring-boot-starter-validation | Input validation |
| h2 | In-memory database |
| lombok | Reduce boilerplate |
| spring-boot-starter-test | JUnit, Mockito, Spring Test |
| springdoc-openapi | Swagger/OpenAPI documentation |

---

## 🎓 Clean Code Principles Applied

✅ **Single Responsibility**: Ogni classe ha una sola responsabilità  
✅ **Open/Closed**: Aperto per estensione, chiuso per modifica  
✅ **Liskov Substitution**: Repository estende JpaRepository  
✅ **Interface Segregation**: Request/Response types ben definiti  
✅ **Dependency Inversion**: Iniezione di dipendenze via constructor  
✅ **DRY (Don't Repeat Yourself)**: Mapper centralizzato, globale exception handler  
✅ **YAGNI**: Solo funzionalità richieste  
✅ **Meaningful Names**: Nomi chiari e autodocumentanti  
✅ **Small Functions**: Metodi corti e mirati  
✅ **Error Handling**: Eccezioni specifiche, non null checks  

---

## 📖 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [REST API Best Practices](https://restfulapi.net/)

---

Enjoy building awesome pizza orders! 🍕

