# Awesome Pizza API

Backend REST per la gestione ordini di una pizzeria, sviluppato con Spring Boot.

## Stack Tecnologico

- Java 17+
- Spring Boot 3.2.0
- Spring Web
- Spring Data JPA
- Jakarta Validation
- H2 Database (in-memory)
- SpringDoc OpenAPI
- JUnit 5 + Mockito

## Requisiti Coperti

- Ordine pizza senza registrazione utente.
- Creazione ordine con una o piu pizze.
- Codice ordine univoco restituito al cliente.
- Polling stato ordine via API pubblica.
- Gestione coda ordini lato pizzaiolo.
- Avanzamento stato ordine lungo il flusso previsto.
- Annullamento ordine in stati compatibili (vedi flusso sotto).

## Architettura Reale del Progetto

Il codice e organizzato in package applicativi semplici (layered):

- `it.adesso.awesomepizza.controller`  
  Espone gli endpoint REST.
- `it.adesso.awesomepizza.controller.api`  
  Contratto API annotato (OpenAPI + mapping).
- `it.adesso.awesomepizza.service`  
  Logica applicativa e regole di business.
- `it.adesso.awesomepizza.repo`  
  Accesso ai dati tramite Spring Data JPA.
- `it.adesso.awesomepizza.model`  
  Entity JPA, DTO (`OrderDTO` per risposte), `CreateOrderRequest` per `POST /orders`, enum, error model.
- `it.adesso.awesomepizza.exception`  
  Eccezioni custom + gestione errori globale.
- `it.adesso.awesomepizza.configuration`  
  Configurazioni applicative e proprieta custom.
- `it.adesso.awesomepizza.service.mapper`  
  Mapping Entity <-> DTO.

## Modello Dati

### Order (risposte API: `OrderDTO`)
- `id` (Long)
- `code` (String, univoco, generato automaticamente)
- `status` (`RECEIVED`, `IN_PROGRESS`, `READY`, `COMPLETED`, `CANCELLED`)
- `priority` (`LOW`, `NORMAL`, `HIGH`; default `NORMAL` alla creazione; modifica solo tramite endpoint admin)
- `pizzas` (lista di `Pizza`)
- `createdAt`, `updatedAt`

### Creazione ordine (body API: `CreateOrderRequest`)
- solo `pizzas` (lista di `PizzaDTO`); nessun `status`, `code`, `priority` o altri campi gestiti dal server.

### Pizza
- `id` (Long)
- `name` (String)
- `quantity` (Integer)
- relazione many-to-one verso `Order`

## Flusso Stati Ordine

Percorso operativo felice:

`RECEIVED -> IN_PROGRESS -> READY -> COMPLETED`

Annullamento (stato terminale, ordine resta in archivio per storico):

- Da `RECEIVED` o `IN_PROGRESS` verso `CANCELLED` tramite `PUT /orders/{id}/cancel`.
- Non consentito da `READY` o `COMPLETED` (risposta `409 Conflict`).
- Ripetere `PUT .../cancel` su un ordine gia `CANCELLED` e idempotente: risposta `200` senza ulteriori modifiche.

## API

Base path: `http://localhost:8080/api/v1`

### Cliente

- `POST /orders`  
  Crea un ordine.
- `GET /orders/{code}`  
  Recupera lo stato ordine tramite codice.

### Pizzaiolo

- `GET /orders`  
  Lista completa ordini.
- `GET /orders/queue/waiting`  
  Coda ordini in stato `RECEIVED` (ordinati per `priority` decrescente, poi `createdAt` crescente).
- `PUT /orders/{id}/start`  
  Stato `RECEIVED -> IN_PROGRESS`.
- `PUT /orders/{id}/ready`  
  Stato `IN_PROGRESS -> READY`.
- `PUT /orders/{id}/complete`  
  Stato `READY -> COMPLETED`.
- `PUT /orders/{id}/cancel`  
  Stato `RECEIVED` o `IN_PROGRESS -> CANCELLED` (non elimina la riga; aggiorna solo lo stato).
- `PUT /orders/{id}/priority`  
  Aggiorna la priorità (solo ordini `RECEIVED`; richiede header `X-User-Role: ADMIN`).

## Esempi Rapidi

### Crea ordine

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "pizzas": [
      {"name": "Margherita", "quantity": 2},
      {"name": "Diavola", "quantity": 1}
    ]
  }'
```

### Stato ordine per codice

```bash
curl http://localhost:8080/api/v1/orders/ABC12345
```

### Annulla ordine (id pizzaiolo: usa l'id numerico)

```bash
curl -X PUT http://localhost:8080/api/v1/orders/1/cancel
```

## Configurazione

In `awesome-pizza/src/main/resources/application.yaml`:

- Database: `jdbc:h2:mem:testdb`
- H2 console: `http://localhost:8080/h2-console`
- OpenAPI docs: `http://localhost:8080/api/docs`
- Swagger UI: `http://localhost:8080/swagger`
- Proprieta custom:
  - `awesome-pizza.max-pizzas-per-order`
  - `awesome-pizza.max-quantity-per-pizza`

## Container Docker

Nel root del repository e presente `Dockerfile` (multi-stage build).

Build immagine:

```bash
docker build -t awesome-pizza:local .
```

Run container:

```bash
docker run --rm -p 8080:8080 awesome-pizza:local
```

## Kubernetes / GKE

Nel root e presente la struttura `k8s/` in stile Kustomize:

- `k8s/base` (Deployment, Service, ConfigMap)
- `k8s/overlays/dev`
- `k8s/overlays/test`
- `k8s/overlays/support`
- `k8s/overlays/release`
- `k8s/overlays/prod`

Ogni overlay imposta:

- namespace dedicato;
- suffix nome risorsa;
- tag immagine ambiente-specifico;
- `SPRING_PROFILES_ACTIVE` coerente con l'ambiente;
- numero repliche.

Render manifest:

```bash
kubectl kustomize k8s/overlays/dev
```

Apply su cluster:

```bash
kubectl apply -k k8s/overlays/dev
```

## Validazioni

Il progetto applica:

- validazione sintattica (`@NotEmpty`, `@NotBlank`, `@Positive`);
- validazione di dominio nel service (max pizze ordine, max quantita per pizza, transizioni stato valide).

## Test

Test presenti in `awesome-pizza/src/test/java`:

- `service/OrderServiceTest` (unit test service)
- `controller/OrderControllerTest` (test controller con MockMvc, incluso annullamento e errori HTTP)
- test di bootstrap contesto Spring (`AwesomePizzaApplicationTests`, `AwesomePizzaApplicationIntegrationTests`)

Esecuzione:

```bash
cd awesome-pizza
./mvnw test
```

## Note Attuali

- Nessuna autenticazione/registrazione utente (coerente con traccia iniziale).
- Database in-memory (utile per iterazione 1 e sviluppo locale).
- Gli annullamenti usano lo stato `CANCELLED` (soft cancel), non `DELETE` sulla risorsa.
- Il vincolo "un solo ordine IN_PROGRESS alla volta" non e ancora enforceato a livello di business.
