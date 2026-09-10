# Backend E-Commerce

Documentazione tecnica dello stato **attuale** del backend e-commerce a microservizi.
Ricostruita dal codice sorgente (branch `claude/jolly-bell-7z44oj`, identico a `main`) e verificata a runtime
avviando l'intero sistema (5 servizi + 5 database PostgreSQL) ed eseguendo test end-to-end.

> **Nota di metodo.** Le funzionalità sono documentate **solo se realmente presenti nel codice**.
> Ciò che è citato come obiettivo ma non implementato è marcato **`PLANNED / NOT IMPLEMENTED`**.
> Il codice sorgente **non è stato modificato** durante l'audit.

---

## Architecture

Sistema a **microservizi** con pattern **database-per-service** e comunicazione **sincrona HTTP** (Spring `RestClient`).
L'`order-servis` agisce da **orchestratore** (saga orchestrata) e coordina gli altri quattro servizi.

```
                         ┌─────────────────┐
                         │   Client / API  │
                         └────────┬────────┘
                                  │ HTTP
                    ┌─────────────▼──────────────┐
                    │        order-servis        │  (orchestratore, 8083)
                    │  OrderController /api/orders│
                    └───┬─────┬──────┬──────┬─────┘
        findUserById()  │     │      │      │  createNotification()
             ┌──────────┘     │      │      └───────────┐
             ▼                ▼      ▼                   ▼
     ┌──────────────┐ ┌─────────────┐ ┌──────────────┐ ┌────────────────────┐
     │ user-servis  │ │product-servis│ │payment-service│ │notification-service│
     │    8081      │ │    8082      │ │    8084       │ │       8085         │
     └──────┬───────┘ └──────┬──────┘ └──────┬────────┘ └─────────┬──────────┘
            │                │               │                    │
         userdb          productdb        paymentdb          notificationdb
         :5432            :5433            :5435              :5436
                          orderdb :5434 (di order-servis)
```

Dipendenze **a runtime** dell'order-servis:
- **user-servis** — validazione esistenza utente (bloccante: 404 → ordine rifiutato);
- **product-servis** — lettura prodotto, verifica disponibilità/stock, decremento/incremento stock (bloccante);
- **payment-service** — creazione pagamento e determinazione esito (bloccante);
- **notification-service** — invio notifica ordine (dovrebbe essere *best-effort*, **ma attualmente è bloccante** — vedi *Known limitations*).

---

## Microservices

| Servizio | Ruolo | Persistenza propria |
|---|---|---|
| **user-servis** | Anagrafica utenti (CRUD) | `userdb` |
| **product-servis** | Catalogo, prezzo, stock, disponibilità | `productdb` |
| **order-servis** | Orchestrazione ordine, righe ordine, totale, stato | `orderdb` |
| **payment-service** | Pagamento simulato ed esito (SUCCESS/FAILED) | `paymentdb` |
| **notification-service** | Notifiche legate a utente/ordine | `notificationdb` |

Ogni servizio è un'applicazione Spring Boot autonoma con il proprio `pom.xml`, `application.properties`,
`GlobalExceptionHandler` e Maven Wrapper.

---

## Technology Stack

- **Java 21**
- **Spring Boot 4.1.1** (`spring-boot-starter-webmvc`, `-data-jpa`, `-validation`)
- **Hibernate / JPA** (`ddl-auto=update`)
- **PostgreSQL 16** (un database per servizio)
- **Spring `RestClient`** per la comunicazione inter-servizio
- **Bean Validation** (Jakarta) per la validazione dei DTO
- **Lombok** (`@Data`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`)
- **Maven** (con wrapper `mvnw`)

---

## Service Ports

| Servizio | Porta HTTP | DB (host:porta/db) | Utente DB |
|---|---|---|---|
| user-servis | **8081** | localhost:5432/userdb | useradmin |
| product-servis | **8082** | localhost:5433/productdb | productadmin |
| order-servis | **8083** | localhost:5434/orderdb | orderadmin |
| payment-service | **8084** | localhost:5435/paymentdb | paymentadmin |
| notification-service | **8085** | localhost:5436/notificationdb | notificationadmin |

> Le credenziali dei DB sono in chiaro nei file `application.properties` (vedi *Known limitations / Security*).

---

## Database Architecture

**Database-per-service**: nessuna foreign key attraversa i confini di servizio; i riferimenti cross-service
(`userId`, `productId`, `orderId`) sono UUID "logici" non vincolati da FK. Schema generato da Hibernate.

| DB | Tabelle | Chiavi / vincoli rilevati |
|---|---|---|
| userdb | `users` | PK `id`; **UNIQUE `email`** |
| productdb | `products` | PK `id` |
| orderdb | `orders`, `order_items` | PK su entrambe; **FK** `order_items.order_id → orders.id` |
| paymentdb | `payments` | PK `id` |
| notificationdb | `notifications` | PK `id` |

Osservazioni sugli schemi (verificate via `information_schema`):
- `orders.stato` e `payments.stato` sono **NULLABLE** (colonne enum senza `nullable=false`); `notifications.stato` è `NOT NULL`.
- Nessun indice esplicito su `order_items.order_id` (FK) né su `notifications.user_id` / `order_id` (impatto sulle query per-utente future).
- Tipi monetari `numeric(10,2)` (BigDecimal) su `prezzo`, `totale`, `importo`.

---

## Entities

### User (user-servis → `users`)
`id: UUID` (PK), `nome` (NN), `cognome` (NN), `email` (NN, UNIQUE), `indirizzo` (nullable).
**Nessuna password, nessun ruolo, nessun account.**

### Product (product-servis → `products`)
`id: UUID` (PK), `nome` (NN), `descrizione` (NN), `prezzo: BigDecimal(10,2)` (NN),
`quantita: Integer` (NN), `categoria` (NN), `disponibile: Boolean` (NN, derivato da `quantita > 0`).

### Order + OrderItem (order-servis → `orders`, `order_items`)
- **Order**: `id: UUID`, `userId: UUID` (NN), `totale: BigDecimal(10,2)` (NN),
  `stato: OrderStatus` (enum STRING, nullable), `dataCreazione: LocalDateTime` (NN),
  `items: List<OrderItem>` (`@OneToMany`, cascade ALL, orphanRemoval).
- **OrderItem**: `id: UUID`, `productId: UUID` (NN), `quantita: Integer` (NN),
  `prezzo: BigDecimal(10,2)` (NN, *snapshot* del prezzo al momento dell'ordine), `order` (`@ManyToOne`).
- **OrderStatus**: `CREATED, PAID, CANCELLED, FAILED` — *`CANCELLED` definito ma mai usato*.

### Payment (payment-service → `payments`)
`id: UUID`, `orderId: UUID` (NN), `importo: BigDecimal(10,2)` (NN),
`metodoPagamento: String` (NN), `stato: PaymentStatus` (enum STRING, nullable), `dataCreazione` (NN).
**PaymentStatus**: `PENDING, SUCCESS, FAILED`.

### Notification (notification-service → `notifications`)
`id: UUID`, `userId: UUID` (NN), `orderId: UUID` (NN), `tipo: String` (NN),
`messaggio: String` (NN), `stato: String` (NN, sempre `"PENDING"` alla creazione), `dataCreazione` (NN).

---

## DTOs

Separazione netta **Input/Output** per ogni risorsa.

| DTO Input (validazione) | DTO Output |
|---|---|
| `UserDTOInput` — `nome` `@NotBlank`, `cognome` `@NotBlank`, `email` `@NotBlank @Email`, `indirizzo` | `UserDTOOutput` (senza dati sensibili — non esistono password) |
| `ProductDTOInput` — `nome/descrizione/categoria` `@NotBlank`, `prezzo` `@NotNull @DecimalMin(0.0)`, `quantita` `@NotNull @Min(0)` | `ProductDTOOutput`, `ProductAvailabilityDTO` |
| `StockUpdateDTO` — `quantita` `@NotNull @Min(1)` | — |
| `PaymentDTOInput` — `orderId` `@NotNull`, `importo` `@NotNull @DecimalMin(0.01)`, `metodoPagamento` `@NotBlank` | `PaymentDTOOutput` |
| `NotificationDTOInput` — `userId/orderId` `@NotNull`, `tipo/messaggio` `@NotBlank` | `NotificationDTOOutput` |
| `OrderDTOInput` — `userId` `@NotNull`, `metodoPagamento` `@NotBlank`, `items` `@NotEmpty` di `@Valid OrderItemDTOInput` | `OrderDTOOutput`, `OrderItemDTOOutput` |
| `OrderItemDTOInput` — `productId` `@NotNull`, `quantita` `@NotNull @Min(1)` | — |

L'order-servis contiene copie locali dei DTO degli altri servizi (`UserDTOOutput`, `ProductDTOOutput`,
`PaymentDTOInput/Output`, `NotificationDTOInput/Output`, `StockUpdateDTO`) usate dai suoi client REST.
In `PaymentDTOOutput` dell'order-servis, `stato` è `String` (deserializzazione dell'enum del payment-service).

---

## REST APIs

Base URL locale: `http://localhost:<porta>`. `{id}` è un UUID.

### user-servis (8081) — `UserController`
| Metodo | URL | Success | Errori principali |
|---|---|---|---|
| POST | `/api/users` | 201 | 400 (validazione), 409 (email duplicata) |
| GET | `/api/users` | 200 | — |
| GET | `/api/users/{id}` | 200 | 404, 400 (UUID) |
| PUT | `/api/users/{id}` | 200 | 400, 404, 409 |
| DELETE | `/api/users/{id}` | 204 | 404 |

### product-servis (8082) — `ProductControler`
| Metodo | URL | Success | Errori principali |
|---|---|---|---|
| POST | `/api/products` | 201 | 400 |
| GET | `/api/products` | 200 | — |
| GET | `/api/products/{id}` | 200 | 404 |
| GET | `/api/products/{id}/availability` | 200 | 404 |
| PUT | `/api/products/{id}` | 200 | 400, 404 |
| PATCH | `/api/products/{id}/stock/decrease` | 200 | 400 (`@Min(1)`), 404, **409 (stock insufficiente)** |
| PATCH | `/api/products/{id}/stock/increase` | 200 | 400, 404 |
| DELETE | `/api/products/{id}` | 204 | 404 |

### payment-service (8084) — `PaymentController`
| Metodo | URL | Success | Note |
|---|---|---|---|
| POST | `/api/payments` | 201 | esito **SUCCESS** se importo ≤ 10000, **FAILED** se > 10000 |
| GET | `/api/payments` | 200 | — |
| GET | `/api/payments/{id}` | 200 | 404 |
| PUT | `/api/payments/{id}` | 200 | ricalcola l'esito |
| DELETE | `/api/payments/{id}` | 204 | 404 |

### notification-service (8085) — `NotificationController`
| Metodo | URL | Success | Note |
|---|---|---|---|
| POST | `/api/notifications` | 201 | `stato` impostato a `"PENDING"` dal servizio |
| GET | `/api/notifications` | 200 | — |
| GET | `/api/notifications/{id}` | 200 | 404 |
| PUT | `/api/notifications/{id}` | 200 | non aggiorna `stato`/`dataCreazione` |
| DELETE | `/api/notifications/{id}` | 204 | 404 |

### order-servis (8083) — `OrderController` + controller di test
| Metodo | URL | Success | Note |
|---|---|---|---|
| POST | `/api/orders` | 201 | orchestrazione completa (vedi *Order flow*) |
| GET | `/api/orders` | 200 | — |
| GET | `/api/orders/{id}` | 200 | 404 |
| DELETE | `/api/orders/{id}` | 204 | 404 |
| GET | `/api/test/user/{id}` | 200 | client di test → user-servis |
| GET | `/api/test/product/{id}` | 200 | client di test → product-servis |
| PATCH | `/api/test/product/{id}/stock/decrease` | 200 | client di test → product-servis |
| POST | `/api/test/payment` | 201 | client di test → payment-service |
| POST | `/api/test/notification` | 201 | client di test → notification-service |

**Totale: 32 endpoint su 9 controller.**

---

## User / UserAccount / Guest model

Stato **attuale**:
- Esiste **solo** l'entità `User` (dati anagrafici, `email` UNIQUE). Nessuna credenziale.
- **`UserAccount`** — `PLANNED / NOT IMPLEMENTED` (nessuna classe, nessuna relazione OneToOne).
- **Distinzione USER / ADMIN / guest** — `PLANNED / NOT IMPLEMENTED` (nessun ruolo, nessuna autenticazione).
- **Guest checkout** — `PLANNED / NOT IMPLEMENTED`.
- "Le password non sono mai esposte nei DTO di output": **vero in modo banale**, perché non esistono password nel modello.
- Implicazione del vincolo **UNIQUE `email`**: un eventuale flusso guest ricorrente con la stessa email
  fallirebbe con 409; oggi non c'è flusso guest, quindi è un vincolo da valutare *quando* si implementerà.

---

## Product management

CRUD completo. `disponibile` è **derivato**: impostato a `quantita > 0` in creazione/aggiornamento e ad ogni
variazione di stock. Endpoint dedicati `PATCH .../stock/decrease` e `.../stock/increase` con `StockUpdateDTO`
(`@Min(1)`). `decrease` rifiuta con **409** se `quantita > stock`. `increase` imposta sempre `disponibile=true`.

---

## Order flow

Flusso di `POST /api/orders` ricostruito da `OrderService.createOrder` (metodo `@Transactional`):

1. **Validazione utente** — `userClient.findUserById(userId)`; se 404 → l'ordine viene rifiutato (404).
2. Creazione `Order` in stato **CREATED**, `totale = 0`.
3. Per ogni item: `productClient.findProductById(productId)`;
   - se non `disponibile` → **409** `InsufficientStockException`;
   - se `quantita richiesta > stock` → **409**;
   - crea `OrderItem` con `prezzo` *snapshot*; accumula il totale (`prezzo * quantita`).
4. Salvataggio ordine **CREATED** con totale calcolato.
5. Costruzione richiesta pagamento e chiamata `paymentClient.createPayment(...)`.
6. **Esito pagamento**:
   - **SUCCESS** → per ogni item `productClient.decreaseStock(...)`; stato → **PAID**;
     poi `notificationClient.createNotification(ORDER_PAID)`.
     In caso di eccezione in questo blocco: **compensazione** con `increaseStock(...)` sugli item già scalati,
     stato → **FAILED**, notifica `ORDER_FAILED`.
   - **FAILED** (importo > 10000) → stato → **FAILED**, notifica `ORDER_FAILED` (nessun decremento stock).
7. Salvataggio stato finale e ritorno `OrderDTOOutput`.

> **Attenzione**: sia il ramo SUCCESS (dopo compensazione) sia il ramo FAILED chiamano il notification-service
> in modo **bloccante**; se il notification-service è irraggiungibile l'intero ordine termina con **500** e rollback
> (vedi *Known limitations* e il *report di audit*).

---

## codOrder

**`PLANNED / NOT IMPLEMENTED`.** Non esiste alcun campo `codOrder` né generazione di codici tipo `AODS-000001-M`,
nessuna sequence PostgreSQL dedicata. L'unico identificatore d'ordine è l'`id` UUID generato da Hibernate.

---

## Shipping

**`PLANNED / NOT IMPLEMENTED`.** Non esiste entità `Shipping`, nessuna relazione `Order ↔ Shipping` OneToOne,
nessuno "shipping di default dallo User Service" né shipping personalizzato.

---

## Payment flow

Pagamento **simulato** da `SimulatedPaymentProcessor` (`PaymentProcessor`):
`importo > 10000.00 → FAILED`, altrimenti `SUCCESS` (soglia esatta 10000 → SUCCESS).
`createPayment` persiste il `Payment` con l'esito e `dataCreazione`. `updatePayment` ricalcola l'esito.
Nessun gateway reale, nessuna gestione webhook, nessuna idempotenza.

---

## Stock management

Lo stock è **posseduto dal product-servis**. L'order-servis lo modifica **solo dopo un pagamento SUCCESS**,
chiamando `decreaseStock` via HTTP per ogni riga. `disponibile` è aggiornato di conseguenza.

---

## Compensation logic

Presente una compensazione **best-effort** nel ramo SUCCESS: se dopo alcuni `decreaseStock` avviene un'eccezione,
il codice ri-incrementa (`increaseStock`) gli item già scalati e porta l'ordine a **FAILED**.
Limiti: la compensazione stessa è sincrona e non protetta da retry; se il product-servis è irraggiungibile
durante la compensazione, lo stock resta incoerente. Inoltre il ritentativo di notifica nel blocco di
compensazione può sollevare una seconda eccezione non gestita (vedi *Known limitations*).

---

## Notifications

`NotificationService.createNotification` salva la notifica con `stato="PENDING"` e `dataCreazione=now()`.
Campi conservati: `userId`, `orderId`, `tipo`, `messaggio`, `stato`, `dataCreazione` — **tutti presenti**.
Recupero: `GET /api/notifications` (tutte) e `GET /api/notifications/{id}`.
- **`GET /api/notifications/user/{userId}`** e **`GET /api/notifications/me`**: `PLANNED / NOT IMPLEMENTED`
  (mancano un metodo repository derivato `findByUserId` e i relativi indici su `user_id`).
- Le notifiche **dovrebbero** essere *best-effort* rispetto all'ordine, **ma oggi non lo sono** (vedi limitazioni).

---

## Cancellation request flow

**`PLANNED / NOT IMPLEMENTED`.** Non esiste logica di richiesta di cancellazione, né gestione di richieste
duplicate. `OrderStatus.CANCELLED` è definito ma **mai** utilizzato. L'unica rimozione è `DELETE /api/orders/{id}`
(hard delete, senza transizione di stato).

---

## Exception handling

Ogni servizio ha un `@RestControllerAdvice` (`GlobalExceptionHandler`) che uniforma le risposte d'errore:

```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "...", "path": "/api/..." }
```
(la validazione aggiunge una mappa `errors` campo→messaggio).

| Situazione | Eccezione | HTTP |
|---|---|---|
| Risorsa non trovata | `ResourceNotFoundException` | 404 |
| Validazione `@Valid` | `MethodArgumentNotValidException` | 400 |
| JSON malformato | `HttpMessageNotReadableException` | 400 |
| Tipo path errato (UUID) | `MethodArgumentTypeMismatchException` | 400 |
| Metodo non supportato | `HttpRequestMethodNotSupportedException` | 405 |
| Content-Type non supportato | `HttpMediaTypeNotSupportedException` | 415 |
| Endpoint inesistente | `NoResourceFoundException` | 404 |
| Vincolo DB | `DataIntegrityViolationException` | 409 |
| Stock insufficiente | `InsufficientStockException` | 409 (product-servis, order-servis) |
| Errore DB | `DataAccessException` | 503 |
| Fallback | `Exception` | 500 |

> product-servis e order-servis gestiscono anche `InsufficientStockException`. user/payment/notification **non** la usano.

---

## Docker/PostgreSQL

- **Nessun** `docker-compose.yml`/`Dockerfile` è presente nel repository (`PLANNED / NOT IMPLEMENTED`).
- Ogni servizio si aspetta un PostgreSQL dedicato su una porta diversa (5432–5436).
- Durante l'audit i 5 database sono stati forniti con istanze PostgreSQL 16 native, riproducendo esattamente
  nomi DB, utenti, password e porte dei file `application.properties` (nessuna modifica al codice/config).

---

## How to run the project

Prerequisiti: **Java 21**, **Maven** (o il wrapper `mvnw`), **5 istanze PostgreSQL** con i DB/credenziali attesi.

1. Avviare i database (una istanza per servizio) su 5432–5436 con i nomi/credenziali dei rispettivi
   `application.properties` (es. `userdb`/`useradmin`/`userpassword`, ecc.).
2. Compilare e avviare ogni servizio:
   ```bash
   cd user-servis        && ./mvnw spring-boot:run     # 8081
   cd product-servis     && ./mvnw spring-boot:run     # 8082
   cd order-servis       && ./mvnw spring-boot:run     # 8083
   cd payment-service    && ./mvnw spring-boot:run     # 8084
   cd notification-service && ./mvnw spring-boot:run   # 8085
   ```
   In alternativa: `./mvnw -DskipTests package` e `java -jar target/<servizio>-0.0.1-SNAPSHOT.jar`.
3. Ordine di avvio consigliato: user, product, payment, notification, poi order.

---

## How to test the APIs

Esempio di ordine valido (sostituire gli UUID con risorse reali):

```bash
# 1) crea utente e prodotto
curl -s -H 'Content-Type: application/json' \
  -d '{"nome":"Anna","cognome":"Neri","email":"anna@example.com"}' http://localhost:8081/api/users
curl -s -H 'Content-Type: application/json' \
  -d '{"nome":"Mouse","descrizione":"ottico","prezzo":25.00,"quantita":50,"categoria":"IT"}' \
  http://localhost:8082/api/products

# 2) crea ordine
curl -s -H 'Content-Type: application/json' -d '{
  "userId":"<UUID_UTENTE>","metodoPagamento":"CARD",
  "items":[{"productId":"<UUID_PRODOTTO>","quantita":2}]
}' http://localhost:8083/api/orders
```

Durante l'audit sono stati eseguiti **90 test HTTP automatici** (100% PASS con dati univoci) più
**6 scenari mirati** su order-servis (compresi i test di resilienza con notification-service spento).
Vedi `BACKEND_ECOMMERCE_AUDIT_REPORT.pdf`.

---

## Known limitations

**BUG ATTUALI**
- **Notification-service non disponibile → ordine in 500 e rollback** (invece di ordine `PAID`/`FAILED` con notifica *best-effort*). Dimostrato a runtime. La notifica è chiamata in modo bloccante e, nel ramo di compensazione, un secondo tentativo di notifica solleva un'eccezione non gestita. *(Severità: HIGH)*
- **Ordine con pagamento fallito restituisce `201 Created`** (con `stato=FAILED`) invece di un codice più appropriato. *(MEDIUM)*
- **CLAUDE.md del notification-service errato** (dichiara 500 per id inesistente; il comportamento reale è 404). *(MEDIUM, documentazione)*

**DEBITO TECNICO**
- Chiamate HTTP esterne dentro un metodo `@Transactional` lungo (connessione DB tenuta durante l'I/O di rete).
- `RestClient` senza **timeout/retry/circuit breaker**.
- Nessuna **idempotenza** su creazione ordine/pagamento (rischio duplicati su retry).
- `orders.stato`/`payments.stato` **nullable**; mancano indici su FK/`user_id`.
- Solo test `contextLoads()` (nessun test di logica di business).
- Refusi diffusi (nomi package/classi: `servis`, `servise`, `ProductControler`; messaggi: "non trtovato", "consetita/presnti").
- Codice ridondante (`setStato(FAILED)` duplicato) e `OrderStatus.CANCELLED` inutilizzato.
- `metodoPagamento` è una `String` libera (nessun enum/whitelist).

**SICUREZZA**
- **Nessuna autenticazione/autorizzazione** su alcun endpoint (nessuna Spring Security/JWT/ruoli) → IDOR su tutte le risorse per-id.
- **Credenziali DB in chiaro** nei `application.properties` versionati.
- CORS configurato solo su user-servis (origine `http://localhost:3000`), assente sugli altri servizi.

---

## Future roadmap

`PLANNED / NOT IMPLEMENTED` (in ordine di priorità suggerito):
1. **Resilienza notifiche best-effort** (try/catch + logging; poi coda asincrona).
2. **Timeout/retry/circuit breaker** (Resilience4j) e spostamento delle chiamate HTTP fuori dalla transazione.
3. **Idempotency key** su ordini e pagamenti.
4. **Spring Security + JWT**, ruoli **USER/ADMIN**, endpoint `/me`.
5. **UserAccount** (OneToOne con User) e **flusso guest**.
6. **Shipping** (OneToOne con Order, default da user-servis) e **codOrder** (`AODS-000001-M`) con sequence.
7. **Cancellation request flow** con gestione richieste duplicate e uso di `OrderStatus.CANCELLED`.
8. **GET /api/notifications/user/{userId}** e `/me` (con indice su `user_id`).
9. **Docker Compose** (+ eventuale Kubernetes), **Eureka**, **API Gateway**.
10. **Kafka/RabbitMQ** per eventi asincroni, **distributed tracing**, **ELK**, **metrics/Actuator**.

---

*Documento generato durante un audit tecnico automatizzato. Nessuna riga di codice sorgente o configurazione applicativa è stata modificata.*
