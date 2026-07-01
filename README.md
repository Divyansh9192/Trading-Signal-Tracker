# Trading Signal Tracking Application

Spring Boot backend for creating and tracking trading signals against Binance public ticker prices.

## Features

- Create, list, fetch, delete, and check status for trading signals.
- Validates BUY and SELL price relationships plus time rules.
- Fetches live prices from Binance public API.
- Persists signal status and realized ROI with PostgreSQL through Spring Data JPA.
- Keeps `TARGET_HIT`, `STOPLOSS_HIT`, and `EXPIRED` states final.
- Periodically evaluates open signals with a Spring scheduled task.
- Provides Swagger/OpenAPI documentation through springdoc.

## Requirements

- Java 21
- Maven
- PostgreSQL

## Database Setup

Create a local PostgreSQL database:

```sql
CREATE DATABASE zuvomo_assessment;
```

By default the application uses:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zuvomo_assessment
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

Override these environment variables if your local credentials are different. Hibernate creates and updates the `trading_signals` table automatically for local runs.

## Run

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Test

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

- `POST /api/signals`
- `GET /api/signals`
- `GET /api/signals/{id}`
- `GET /api/signals/{id}/status`
- `DELETE /api/signals/{id}`

Example create request:

```json
{
  "symbol": "BTCUSDT",
  "direction": "BUY",
  "entryPrice": 60000,
  "stopLoss": 59000,
  "targetPrice": 62000,
  "entryTime": "2026-07-01T08:00:00Z",
  "expiryTime": "2026-07-02T08:00:00Z"
}
```

## Architecture

The project uses a layered structure:

- `domain`: JPA entity and enums for signal direction and status.
- `dto`: request and response contracts.
- `repository`: Spring Data JPA access for persisted signals.
- `service`: validation, ROI calculation, status transition rules, and scheduled evaluation.
- `market`: Binance public API integration through Spring `RestClient`.
- `web`: REST controller and global exception handler.

Business flow:

1. API requests are validated with Jakarta Bean Validation and domain-specific checks.
2. Valid signals are persisted as `OPEN`.
3. Reads, status checks, and the scheduler evaluate open signals.
4. If a signal is expired, it becomes `EXPIRED` before any live price check.
5. Otherwise Binance live price is fetched and BUY/SELL target or stop-loss rules are applied.
6. Final states are never changed again, and realized ROI is stored only when target or stop-loss is hit.
