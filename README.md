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

1. Install MySQL Server 8.0 or newer, then run `database-setup.sql` in MySQL Workbench or the MySQL command line. This creates the database and credentials expected by the application:

   ```sql
   CREATE DATABASE IF NOT EXISTS vehicle_log;
   ```

   The application is configured for the MySQL `root` user with password `Password`. If you use different credentials, set `DB_USERNAME` and `DB_PASSWORD` before starting the application.

2. Start the JavaFX application:

   ```powershell
   .\mvnw javafx:run
   ```

The application creates its tables automatically on first launch. Override `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, or `STORAGE_DIRECTORY` for another local setup.

## API examples

- `GET /api/maintenance`
- `POST /api/maintenance` with `{ "vehicleId": 1, "serviceDate": "2026-09-08", "mileage": 42500, "description": "Oil change" }`
- `PUT /api/maintenance/{id}`
- `DELETE /api/maintenance/{id}`
- `POST /api/maintenance/documents` as multipart field `file`
- `DELETE /api/vehicles/{id}`
