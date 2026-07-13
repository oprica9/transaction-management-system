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
* Validate required fields, transaction dates, and amounts
* Prevent future transaction dates
* Allow a maximum of two decimal places for amounts
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
git clone https://github.com/oprica9/transaction-management-system.git
cd transaction-management-system
```

Install the frontend dependencies:

```bash
npm --prefix tms-ui ci
```

Backend dependencies are downloaded automatically when the backend starts.

## Configuration

The application includes default settings, so no configuration files need to be changed before running it.

| Setting                          | Environment variable       | Default                             |
| -------------------------------- | -------------------------- | ----------------------------------- |
| CSV file                         | `TMS_CSV_PATH`             | `${user.home}/tms/transactions.csv` |
| Allowed frontend origin          | `TMS_CORS_ALLOWED_ORIGINS` | `http://localhost:5173`             |
| Backend URL used by the frontend | `VITE_API_BASE_URL`        | `http://localhost:8080`             |

### Default CSV location

When `TMS_CSV_PATH` is not set, the backend stores transactions in the current user's home directory.

Linux and macOS:

```text
~/tms/transactions.csv
```

Windows:

```text
%USERPROFILE%\tms\transactions.csv
```

The backend will:

* create missing parent directories;
* create the CSV file when it does not exist;
* add the required header when the file is empty;
* reject an existing invalid CSV without changing it.

### Run with the sample CSV

A sample CSV is included at [`data/transactions.csv`](data/transactions.csv).

Run the following commands from the repository root.

Linux or macOS:

```bash
TMS_CSV_PATH="$PWD/data/transactions.csv" \
./tms-api/mvnw -f tms-api/pom.xml spring-boot:run
```

Windows PowerShell:

```powershell
$env:TMS_CSV_PATH = Join-Path $PWD "data\transactions.csv"
tms-api\mvnw.cmd -f tms-api\pom.xml spring-boot:run
```

Windows Command Prompt:

```cmd
set "TMS_CSV_PATH=%CD%\data\transactions.csv"
tms-api\mvnw.cmd -f tms-api\pom.xml spring-boot:run
```

### Override backend settings

Linux or macOS:

```bash
TMS_CSV_PATH=/tmp/transactions.csv \
TMS_CORS_ALLOWED_ORIGINS=http://localhost:5173 \
./tms-api/mvnw -f tms-api/pom.xml spring-boot:run
```

Windows PowerShell:

```powershell
$env:TMS_CSV_PATH = "C:\temp\transactions.csv"
$env:TMS_CORS_ALLOWED_ORIGINS = "http://localhost:5173"

tms-api\mvnw.cmd -f tms-api\pom.xml spring-boot:run
```

### Override the frontend API URL

Linux or macOS:

```bash
VITE_API_BASE_URL=http://localhost:8080 \
npm --prefix tms-ui run dev
```

Windows PowerShell:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm --prefix tms-ui run dev
```

Do not include `/transactions` in `VITE_API_BASE_URL`.

## Run the application

Run the backend and frontend in separate terminals from the repository root.

### Backend

Linux or macOS:

```bash
./tms-api/mvnw -f tms-api/pom.xml spring-boot:run
```

Windows:

```powershell
tms-api\mvnw.cmd -f tms-api\pom.xml spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### Frontend

The command is the same on all supported operating systems:

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

Example:

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

Returns `200 OK`.

When no transactions exist, the endpoint returns:

```json
[]
```

### Add a transaction

```http
POST /transactions
Content-Type: application/json
```

Example:

```bash
curl --json '{
  "transactionDate": "2026-07-07",
  "accountNumber": "ACCOUNT-123",
  "accountHolderName": "Test Holder",
  "amount": 1000.00
}' http://localhost:8080/transactions
```

Example response:

```json
{
  "transactionDate": "2026-07-07",
  "accountNumber": "ACCOUNT-123",
  "accountHolderName": "Test Holder",
  "amount": 1000.00,
  "status": "Pending"
}
```

Returns `201 Created`.

The backend assigns the status randomly.

### Request fields

| Field               | Rule                                                    |
| ------------------- | ------------------------------------------------------- |
| `transactionDate`   | Required, format `YYYY-MM-DD`, cannot be in the future  |
| `accountNumber`     | Required and nonblank                                   |
| `accountHolderName` | Required and nonblank                                   |
| `amount`            | Required, greater than zero, maximum two decimal places |

### Errors

The backend returns Spring `ProblemDetail` responses.

Example:

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

Main error codes:

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

* the date must use `YYYY-MM-DD` and cannot be in the future;
* the account number must not be blank;
* the account holder name must not be blank;
* the amount must be greater than zero and have at most two decimal places;
* the status must be `Pending`, `Settled`, or `Failed`.

New transactions are appended to the end of the file.

Fields containing commas are quoted automatically:

```csv
2026-07-07,1234-5678-9101,"Holder, Test",1000.00,Pending
```

## Testing

### Backend tests

Linux or macOS:

```bash
./tms-api/mvnw -f tms-api/pom.xml test
```

Windows:

```powershell
tms-api\mvnw.cmd -f tms-api\pom.xml test
```

The tests cover:

* domain validation;
* CSV initialization, parsing, and writing;
* invalid CSV handling;
* transaction status assignment;
* service behavior;
* controller responses;
* API error responses.

### Frontend build check

```bash
npm --prefix tms-ui run build
```

This checks the TypeScript code and creates a production build.

### Manual check

1. Start the backend and frontend.
2. Confirm that the transaction table loads.
3. Open the Add Transaction modal.
4. Check the required-field validation.
5. Confirm that future dates are rejected.
6. Confirm that amounts with more than two decimal places are rejected.
7. Add a valid transaction.
8. Confirm that it appears in the table.
9. Refresh the page and confirm that the transaction remains stored.
10. Stop the backend and check the frontend error and retry state.

## Design

### Backend

```text
Controller → Service → Repository → CSV
```

* The controller handles HTTP requests.
* The service creates transactions and assigns statuses.
* The domain model protects the transaction rules.
* The repository reads and writes the CSV.
* The exception handler creates safe API error responses.

The CSV repository uses a read-write lock and is thread-safe inside one running backend instance.

The lock does not coordinate with:

* another backend instance;
* another application;
* a user manually editing the file.

### Frontend

```text
TransactionsPage → useTransactions → transactionApi
```

* `TransactionsPage` controls what is displayed.
* `useTransactions` manages transaction data and request state.
* `transactionApi` handles HTTP communication.
* Smaller components handle the table, form, modal, status badge, and errors.

Local React state is sufficient because the application contains one small feature.

## Limitations

* CSV storage is not suitable for large or multi-instance systems.
* Transactions do not have a persistent ID.
* Currency is not part of the data model.
* Authentication is intentionally not included.

## AI usage

AI tools were used to review the code, suggest edge cases and refactoring ideas, and improve the documentation.

All suggestions were reviewed before use. Backend behavior was checked with automated tests, and the complete application flow was checked against the task requirements.
