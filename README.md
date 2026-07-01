# Trading Signal Tracking Application

A Spring Boot 3 backend that lets users create, track, and evaluate trading signals against live Binance market prices. The service stores signals in PostgreSQL, exposes REST APIs for signal management, and automatically updates signal status when a target price, stop-loss, or expiry condition is reached.

## Table of Contents

- [Trading Signal Tracking Application](#trading-signal-tracking-application)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Tech Stack](#tech-stack)
  - [Setup Instructions](#setup-instructions)
    - [Prerequisites](#prerequisites)
    - [Clone and configure](#clone-and-configure)
  - [Database Setup](#database-setup)
    - [Option 1: Local PostgreSQL](#option-1-local-postgresql)
    - [Option 2: Docker Compose](#option-2-docker-compose)
  - [How to Run the Application](#how-to-run-the-application)
    - [Run locally](#run-locally)
    - [Build the JAR](#build-the-jar)
  - [API Documentation](#api-documentation)
  - [Core Endpoints](#core-endpoints)
    - [Example create request](#example-create-request)
    - [Example success response](#example-success-response)
    - [Validation and error behavior](#validation-and-error-behavior)
  - [Architecture Explanation](#architecture-explanation)
    - [1. Project structure](#1-project-structure)
    - [2. Business logic flow](#2-business-logic-flow)
    - [3. External API integration](#3-external-api-integration)
  - [Testing](#testing)

## Overview

This project provides an API for managing trading signals with the following capabilities:

- Create BUY and SELL trading signals.
- Validate signal price rules and timing constraints before persistence.
- Persist signals in PostgreSQL using Spring Data JPA.
- Evaluate live market price data through the Binance public ticker API.
- Mark signals as `OPEN`, `TARGET_HIT`, `STOPLOSS_HIT`, or `EXPIRED`.
- Calculate real-time and realized ROI percentages.
- Re-evaluate open signals on demand and through a scheduled background job.
- Publish OpenAPI documentation through Swagger UI.

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Validation
- Flyway
- PostgreSQL
- springdoc OpenAPI / Swagger UI
- Maven
- Docker and Docker Compose

## Setup Instructions

### Prerequisites

Make sure the following tools are installed:

- Java 21
- Maven 3.9+ or use the included Maven Wrapper
- PostgreSQL 16+ or Docker Desktop

### Clone and configure

```bash
git clone https://github.com/Divyansh9192/Trading-Signal-Tracker
cd assessment
```

The application reads database configuration from environment variables:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

If these are not provided, the app falls back to the values defined in `src/main/resources/application.yaml`.

## Database Setup

This application uses PostgreSQL and Flyway migrations.

### Option 1: Local PostgreSQL

1. Create a database:

```sql
CREATE DATABASE zuvomo_assessment;
```

2. Create or reuse a PostgreSQL user with access to that database.

3. Set environment variables before starting the app.

PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/zuvomo_assessment"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your-password"
```

macOS/Linux:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zuvomo_assessment
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your-password
```

4. Start the application. Flyway will automatically apply `V1__Create_trading_signal_table.sql` and create the `trading_signals` table plus indexes.

### Option 2: Docker Compose

The repository includes a `docker-compose.yml` file that starts:

- PostgreSQL on port `5432`
- The Spring Boot application on port `8081`

Start everything with:

```bash
docker compose up --build
```

Important note:

- The current application code uses PostgreSQL.

Docker Compose PostgreSQL settings:

```text
Database: tradingdb
Username: postgres
Password: password
```

## How to Run the Application

### Run locally

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8081
```

### Build the JAR

```bash
./mvnw clean package
```

Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

## API Documentation

This project includes Swagger/OpenAPI documentation through `springdoc-openapi`.

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Postman collection:

- No Postman collection is currently included in the repository.
- Swagger UI can be used to inspect and test all available endpoints interactively.

## Core Endpoints

Base path:

```text
/api/signals
```

Available endpoints:

- `POST /api/signals` - Create a trading signal
- `GET /api/signals` - List all signals
- `GET /api/signals/{id}` - Get a signal by ID
- `GET /api/signals/{id}/status` - Evaluate and return the current signal status
- `DELETE /api/signals/{id}` - Delete a signal

### Example create request

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

### Example success response

```json
{
  "id": 1,
  "symbol": "BTCUSDT",
  "direction": "BUY",
  "entryPrice": 60000,
  "stopLoss": 59000,
  "targetPrice": 62000,
  "entryTime": "2026-07-01T08:00:00Z",
  "expiryTime": "2026-07-02T08:00:00Z",
  "createdAt": "2026-07-01T08:01:12.412345Z",
  "status": "OPEN",
  "realizedRoi": null
}
```

### Validation and error behavior

- Bean validation rejects malformed request payloads with `400 Bad Request`.
- Domain validation rejects invalid BUY/SELL price relationships and expired time windows with `400 Bad Request`.
- Missing signal IDs return `404 Not Found`.
- Binance connectivity or price lookup failures return `502 Bad Gateway`.

## Architecture Explanation

This section provides the required one-page overview of the project structure, business logic flow, and external API integration.

### 1. Project structure

The codebase follows a layered Spring Boot architecture:

- `controller`  
  Exposes REST endpoints for creating, listing, retrieving, deleting, and evaluating trading signals.

- `dto`  
  Defines request and response contracts such as `CreateSignalRequest`, `SignalResponse`, and `SignalStatusResponse`.

- `entity`  
  Contains JPA persistence models and enums, including `TradingSignal`, `Direction`, and `SignalStatus`.

- `repository`  
  Contains the Spring Data JPA repository used for database access.

- `service`  
  Holds the core business logic:
  - `SignalValidator` enforces trading rules and time constraints.
  - `SignalService` orchestrates create/read/delete operations and signal evaluation.
  - `RoiCalculator` computes percentage ROI for BUY and SELL scenarios.

- `market`  
  Defines the market-price abstraction and its Binance-backed implementation.

- `exception`  
  Centralizes API error handling and consistent HTTP responses.

- `config`  
  Hosts application-level beans such as the shared `Clock`.

- `src/main/resources/db/migration`  
  Stores Flyway SQL migrations for schema management.

### 2. Business logic flow

The main business flow is:

1. A client sends a `POST /api/signals` request with symbol, direction, pricing, and timing data.
2. `CreateSignalRequest` is validated with Jakarta Bean Validation annotations.
3. `SignalValidator` applies business-specific rules:
   - BUY signals must have `stopLoss < entryPrice < targetPrice`
   - SELL signals must have `targetPrice < entryPrice < stopLoss`
   - `expiryTime` must be after `entryTime`
   - `entryTime` cannot be more than 24 hours in the past
4. A valid signal is persisted in PostgreSQL with initial status `OPEN`.
5. When a signal is fetched through `GET /api/signals`, `GET /api/signals/{id}`, or `GET /api/signals/{id}/status`, the service evaluates whether the signal should stay open or transition to a final state.
6. The evaluation logic works in this order:
   - If the signal is already final, it is returned unchanged.
   - If the current time is past `expiryTime`, the signal becomes `EXPIRED`.
   - Otherwise, the current market price is fetched from Binance.
   - For BUY signals, the signal becomes `TARGET_HIT` when current price is greater than or equal to target price, and `STOPLOSS_HIT` when current price is less than or equal to stop-loss.
   - For SELL signals, the signal becomes `TARGET_HIT` when current price is less than or equal to target price, and `STOPLOSS_HIT` when current price is greater than or equal to stop-loss.
7. If the signal reaches a final trading outcome, realized ROI is stored in the database.
8. A scheduled task runs every 60 seconds by default and re-evaluates all `OPEN` signals in the background.

### 3. External API integration

The application integrates with the Binance public market data API through `BinancePriceClient`.

- Client interface: `MarketPriceClient`
- Implementation: `market.impl.BinancePriceClient`
- External endpoint used: `GET /api/v3/ticker/price?symbol={SYMBOL}`
- Default Binance base URL: `https://api.binance.com`

Integration behavior:

- The service requests the latest ticker price for a trading pair such as `BTCUSDT`.
- The returned price is converted into `BigDecimal` for accurate financial calculations.
- If Binance returns an empty payload, malformed data, or a network/client error occurs, the app throws `ExternalPriceException`.
- API consumers receive a `502 Bad Gateway` response for those external dependency failures.
- During scheduled evaluations, transient Binance failures are ignored so one failed poll does not stop future background evaluations.

## Testing

Run the automated test suite with:

macOS/Linux:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```
