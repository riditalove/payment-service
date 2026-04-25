# Payment Service - Architecture & System Design

## Overview

`payment-service` is a Spring Boot service that:

- Creates and stores payments in PostgreSQL.
- Encrypts incoming card numbers before persistence.
- Publishes a `payment.created`-style event to RabbitMQ after payment creation.
- Exposes APIs to read a payment and update payment status.

The service follows a layered design:

- `controller` -> `service` -> `repository` -> `entity`

---

## High-Level Architecture

```text
Client
  |
  v
PaymentController (/payments)
  |
  v
PaymentService
  |---> CardEncryptionService -> EncryptionServiceClient -> POST /encrypt
  |                                              (base URL configurable)
  |
  |---> PaymentRepository (Spring Data JPA) -> PostgreSQL
  |
  '---> PaymentEventPublisher -> RabbitTemplate -> payments.exchange
                                                routing key: payments.created
                                                queue: payments.queue
```

---

## Package Structure

```text
com.example.payment
  PaymentApplication
  paymet
    config/
    controller/
    dto/
    entity/
    exception/
    messaging/
    repository/
    service/
    util/
```

Note: the current package name is `paymet` (as implemented in code).

---

## Core Components

### 1) API Layer (`controller`)

- `PaymentController`
  - `POST /payments` - create a payment
  - `GET /payments/{id}` - fetch a payment
  - `PATCH /payments/{id}/status` - update payment status
- `EncryptionController`
  - `POST /encrypt`
  - `POST /decrypt`

### 2) Service Layer (`service`)

- `PaymentService`
  - Validates request payload.
  - Normalizes currency (uppercase, 3 letters).
  - Encrypts card number before saving.
  - Saves payment via JPA repository.
  - Publishes `PaymentCreatedEvent` after save.
- `CardEncryptionService`
  - Delegates to `EncryptionServiceClient` (HTTP call to `/encrypt`).
- `AesEncryptionService`
  - Local AES encrypt/decrypt logic for `/encrypt` and `/decrypt`.
- `EncryptionServiceClient`
  - Calls external (or same-service) encryption endpoint.
  - Maps remote failures to proper HTTP errors.

### 3) Persistence Layer (`repository`, `entity`)

- `PaymentRepository` extends `JpaRepository<Payment, UUID>`.
- `Payment` entity fields:
  - `id` (UUID)
  - `amount`
  - `currency`
  - `encryptedCardNumber`
  - `status` (`PENDING`, `SUCCESS`, `FAILED`)
  - `createdAt` (`@PrePersist` timestamp)

### 4) Messaging Layer (`config`, `messaging`)

- `RabbitMQTopologyConfiguration`
  - Exchange: `payments.exchange` (direct)
  - Queue: `payments.queue`
  - Routing key: `payments.created`
- `RabbitMQTemplateConfiguration`
  - Configures `RabbitTemplate`.
- `PaymentEventPublisher`
  - Serializes event payload to JSON and publishes to RabbitMQ.

### 5) Error Handling (`exception`, `dto`)

- `GlobalExceptionHandler` returns structured `ApiError`.
- Handles:
  - `ResponseStatusException`
  - `IllegalArgumentException`
  - malformed body errors
  - generic unexpected exceptions

---

## Request and Event Flows

## Flow A: Create Payment (`POST /payments`)

1. Request arrives at `PaymentController`.
2. `PaymentService.createPayment()` validates input.
3. Card is encrypted through `CardEncryptionService`.
4. Payment is saved in PostgreSQL with status `PENDING`.
5. `PaymentEventPublisher` publishes `{ "paymentId": "<uuid>" }` to RabbitMQ.
6. Response returns created payment payload.

## Flow B: Processor Updates Status

1. `processor-service` consumes from `payments.queue`.
2. It processes event and calls:
   - `PATCH /payments/{id}/status`
3. `payment-service` updates status (typically `SUCCESS` or `FAILED`).

---

## External Integrations

- **PostgreSQL** for payment persistence.
- **RabbitMQ** for async event delivery.
- **Processor Service** consumes payment events and updates status.

---

## Configuration

Current defaults in `src/main/resources/application.properties`:

- Service port: `8082`
- PostgreSQL:
  - `jdbc:postgresql://localhost:5432/payment`
- RabbitMQ:
  - host `localhost`
  - port `5678`
  - username/password `guest`/`guest`
- Encryption client base URL:
  - `http://localhost:${server.port}`

If you run RabbitMQ on standard port `5672`, update both service configs accordingly.

---

## Running the Service

From repository root:

```bash
mvn -pl payment-service spring-boot:run
```

From `payment-service` directory:

```bash
mvn spring-boot:run
```

---

## Example API Calls

Create payment:

```bash
curl --location 'http://localhost:8082/payments' \
--header 'Content-Type: application/json' \
--data '{
  "amount": 19.99,
  "currency": "USD",
  "cardNumber": "4111111111111111"
}'
```

Get payment:

```bash
curl --location 'http://localhost:8082/payments/{paymentId}'
```

Update status:

```bash
curl --location --request PATCH 'http://localhost:8082/payments/{paymentId}/status' \
--header 'Content-Type: application/json' \
--data '{
  "status": "SUCCESS"
}'
```

---

## Design Notes

- **Synchronous + asynchronous blend**:
  - synchronous HTTP API for client operations,
  - asynchronous RabbitMQ events for downstream processing.
- **Security**:
  - card number is stored encrypted (`encryptedCardNumber`), not plain text.
- **Resilience**:
  - encryption HTTP failures and downstream errors are surfaced with controlled responses.
- **Extensibility**:
  - event-driven integration allows additional consumers without changing payment creation API.

