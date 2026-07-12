# Transaction Management System

A full-stack application for viewing and adding transactions stored in a CSV file.

* **Backend:** Java 17 and Spring Boot
* **Frontend:** React 19, TypeScript 6, and Vite 8
* **Storage:** CSV file

## Contents

* [Features](#features)
* [Prerequisites](#prerequisites)
* [Setup](#setup)
* [Configuration](#configuration)
* [Run the application](#run-the-application)
* [API](#api)
* [CSV format](#csv-format)
* [Testing](#testing)
* [Design](#design)
* [Limitations](#limitations)
* [AI usage](#ai-usage)

## Features

* View all stored transactions
* Add transactions through a modal form
* Randomly assign `Pending`, `Settled`, or `Failed`
* Display status-specific colors
* Validate required fields and positive amounts
* Handle loading, empty, error, and retry states
* Validate the CSV during backend startup

## Prerequisites

* Java 17 or newer
* [Node.js](https://nodejs.org/) `20.19+`, `22.13+`, or `24+`
* [Git](https://git-scm.com/downloads)

The project includes the Maven Wrapper, so Maven does not need to be installed separately.

Exact frontend dependency versions are defined in [`tms-ui/package.json`](tms-ui/package.json) and locked in [`tms-ui/package-lock.json`](tms-ui/package-lock.json).

Check the installations:

```bash
java -version
node --version
npm --version
git --version
```

## Setup

Clone the repository:

```bash
git clone git@github.com:oprica9/transaction-management-system.git
cd transaction-management-system
```

Install frontend dependencies:

```bash
npm --prefix tms-ui install
```

Backend dependencies are downloaded automatically when the backend starts.

## Configuration

The application includes local defaults, so no configuration files need to be changed.

| Setting                          | Environment variable  | Default                   |
| -------------------------------- | --------------------- | ------------------------- |
| CSV file                         | `TMS_CSV_PATH`        | `./data/transactions.csv` |
| Allowed frontend origin          | `TMS_ALLOWED_ORIGINS` | `http://localhost:5173`   |
| Backend URL used by the frontend | `VITE_API_BASE_URL`   | `http://localhost:8080`   |

The sample data is available in [`data/transactions.csv`](data/transactions.csv).

The backend will:

* create missing parent directories;
* create the CSV when it does not exist;
* add the required header when the file is empty;
* reject an existing invalid CSV without changing it.

Because the application is run from the repository root, the default CSV path points to:

```text
transaction-management-system/data/transactions.csv
```

### Override backend settings

```bash
TMS_CSV_PATH=/tmp/transactions.csv \
TMS_ALLOWED_ORIGINS=http://localhost:5173 \
./tms-api/mvnw -f tms-api/pom.xml spring-boot:run
```

### Override the frontend API URL

```bash
VITE_API_BASE_URL=http://localhost:8080 \
npm --prefix tms-ui run dev
```

Do not include `/transactions` in `VITE_API_BASE_URL`.

## Run the application

Run the backend and frontend in separate terminals from the repository root.

### Backend

```bash
./tms-api/mvnw -f tms-api/pom.xml spring-boot:run
```

On Windows:

```powershell
tms-api\mvnw.cmd -f tms-api\pom.xml spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### Frontend

```bash
npm --prefix tms-ui run dev
```

Open the URL printed by Vite, usually:

```text
http://localhost:5173
```

## API

Base URL:

```text
http://localhost:8080
```

### Get all transactions

```http
GET /transactions
```

```bash
curl http://localhost:8080/transactions
```

Example response:

```json
[
  {
    "transactionDate": "2025-03-01",
    "accountNumber": "7289-3445-1121",
    "accountHolderName": "Maria Johnson",
    "amount": 150.00,
    "status": "Settled"
  }
]
```

Returns `200 OK`. When no transactions exist, it returns `[]`.

### Add a transaction

```http
POST /transactions
Content-Type: application/json
```

```bash
curl --json '{
  "transactionDate": "2026-12-07",
  "accountNumber": "ACCOUNT-123",
  "accountHolderName": "Test Holder",
  "amount": 1000.00
}' http://localhost:8080/transactions
```

Example response:

```json
{
  "transactionDate": "2026-12-07",
  "accountNumber": "ACCOUNT-123",
  "accountHolderName": "Test Holder",
  "amount": 1000.00,
  "status": "Pending"
}
```

Returns `201 Created`. The backend assigns the status randomly.

### Request fields

| Field               | Rule                           |
| ------------------- | ------------------------------ |
| `transactionDate`   | Required, format `YYYY-MM-DD`  |
| `accountNumber`     | Required and nonblank          |
| `accountHolderName` | Required and nonblank          |
| `amount`            | Required and greater than zero |

### Errors

The backend returns Spring `ProblemDetail` responses.

```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "code": "VALIDATION_FAILED",
  "errors": {
    "accountNumber": [
      "must not be blank"
    ]
  }
}
```

| Code                          | Meaning                      |
| ----------------------------- | ---------------------------- |
| `VALIDATION_FAILED`           | Invalid request fields       |
| `MALFORMED_REQUEST`           | Invalid JSON or value format |
| `INVALID_TRANSACTION_DATA`    | Invalid CSV data             |
| `TRANSACTION_STORAGE_FAILURE` | CSV read or write failed     |
| `INTERNAL_ERROR`              | Unexpected server error      |

## CSV format

Required header:

```text
Transaction Date,Account Number,Account Holder Name,Amount,Status
```

Rules:

* date uses `YYYY-MM-DD`;
* account number and holder name must not be blank;
* amount must be greater than zero;
* status must be `Pending`, `Settled`, or `Failed`.

New transactions are appended to the file. Fields containing commas are quoted automatically.

```csv
2026-12-07,1234-5678-9101,"Holder, Test",1000.00,Pending
```

## Testing

### Backend

```bash
./tms-api/mvnw -f tms-api/pom.xml test
```

On Windows:

```powershell
tms-api\mvnw.cmd -f tms-api\pom.xml test
```

The tests cover CSV handling, validation, status assignment, service behavior, controller responses, and API errors.

### Frontend

```bash
npm --prefix tms-ui run build
```

This checks TypeScript and creates a production build.

### Manual check

1. Start both applications.
2. Confirm that the table loads.
3. Check form validation.
4. Add a valid transaction.
5. Refresh and confirm that it is still present.
6. Stop the backend and check the frontend error and retry state.

## Design

### Backend

```text
Controller → Service → Repository → CSV
```

* The controller handles HTTP requests.
* The service creates transactions and assigns statuses.
* The domain model protects required rules.
* The repository reads and writes the CSV.
* The exception handler creates safe API errors.

The repository is thread-safe inside one running backend instance.

### Frontend

```text
TransactionsPage → useTransactions → transactionApi
```

* `TransactionsPage` controls what is displayed.
* `useTransactions` manages data and request state.
* `transactionApi` handles HTTP communication.
* Smaller components handle the table, form, modal, badge, and errors.

Local React state is enough because the application contains one small feature.

## Limitations

* CSV storage is not suitable for large or multi-instance systems.
* Transactions do not have a persistent ID.
* Currency is not part of the data model.
* Authentication is intentionally not included.

## AI usage

AI tools were used to review the code, suggest edge cases and refactoring ideas, and improve the documentation.

All suggestions were reviewed before use. Backend behavior was checked with automated tests, and the complete application flow was checked against the task requirements.

