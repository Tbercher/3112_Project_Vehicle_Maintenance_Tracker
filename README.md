# Digital Vehicle Maintenance Log

A local desktop application for keeping a centralized history of vehicle service events. The user interface is JavaFX, the application/backend layer is Spring Boot, and records are persisted in MySQL with JPA.

## Features

- Log service date, mileage, and description
- Add multiple vehicles and switch between their separate service histories
- Delete a vehicle and all of its maintenance records with confirmation
- Edit or permanently delete records with confirmation
- Attach PDF, image, or Word documents to a record
- Copy attachments into `data/documents`
- View history in a sortable table
- Open attached documents with the operating system default viewer
- REST API under `/api/maintenance`

## Prerequisites

- Java 21 or newer
- Maven 3.9 or newer
- MySQL 8.0 or newer, installed locally

## Run locally

1. Install MySQL Server 8.0 or newer, then run `database-setup.sql` (located in project root) in MySQL Workbench or the MySQL command line. This creates the database:

   ```sql
   CREATE DATABASE IF NOT EXISTS vehicle_log;
   ```

   By default, the application connects as the MySQL `root` user with password `Password`. Make sure that user exists and has access to `vehicle_log`. If you use different credentials, set `DB_USERNAME` and `DB_PASSWORD` before starting the application.

2. Start the JavaFX application in your terminal:

   ```powershell
   .\mvnw javafx:run
   ```

   MySQL must be running before the application starts. The desktop application also starts the REST API at `http://localhost:8080`.

The application creates its tables automatically on first launch. Override `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, or `STORAGE_DIRECTORY` for another local setup.

## API examples

- `GET /api/maintenance`
- `POST /api/maintenance` with `{ "vehicleId": 1, "serviceDate": "2026-09-08", "mileage": 42500, "description": "Oil change" }`
- `PUT /api/maintenance/{id}`
- `DELETE /api/maintenance/{id}`
- `POST /api/maintenance/documents` as multipart field `file`
- `DELETE /api/vehicles/{id}`

Example request:

```powershell
curl.exe -X POST http://localhost:8080/api/maintenance `
   -H "Content-Type: application/json" `
   -d '{"vehicleId":1,"serviceDate":"2026-09-08","mileage":42500,"description":"Oil change"}'
```

To use a different database or document location, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, or `STORAGE_DIRECTORY` before starting the application.
