# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=NotificationServiceApplicationTests

# Package without tests
./mvnw package -DskipTests
```

## Architecture

Spring Boot 4.1.1 microservice (Java 21) in an e-commerce system. Runs on port **8085**, connects to PostgreSQL on `localhost:5436` (db: `notificationdb`).

Request/response flow: `NotificationController` → `NotificationService` → `NotificationRepository` (JpaRepository)

DTOs separate API contract from persistence:
- `NotificationDTOInput` — input validation via `@NotNull`/`@NotBlank` (userId, orderId, tipo, messaggio)
- `NotificationDTOOutput` — response including generated fields (id, stato, dataCreazione)

The `Notification` entity uses UUID primary keys. `stato` is always set to `"PENDING"` on creation by the service layer; it is not exposed as an input field. `dataCreazione` is set to `LocalDateTime.now()` at creation time and not updated on PUT.

## REST API

Base URL: `http://localhost:8085/api/notifications`

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| POST | `/api/notifications` | Create notification | 201 Created |
| GET | `/api/notifications` | Get all notifications | 200 OK |
| GET | `/api/notifications/{id}` | Get by UUID | 200 OK |
| PUT | `/api/notifications/{id}` | Update (userId, orderId, tipo, messaggio) | 200 OK |
| DELETE | `/api/notifications/{id}` | Delete | 204 No Content |

## Key notes

- `stato` and `dataCreazione` are managed by the service, never from input
- `getNotificationById`, `updateNotification`, `deleteNotification` throw `RuntimeException` if id not found (no global exception handler — returns 500)
- Schema is managed by Hibernate with `ddl-auto=update`
- Package name has a typo: `servise` (not `service`) — do not rename without updating all references