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

## Troubleshooting & Common Errors

**Application fails to start / "Communication link failure"**
* **Cause:** The Spring Boot backend cannot connect to MySQL.
* **Fix:** Ensure the MySQL service is currently running on your machine. Verify that you ran the `database-setup.sql` script to create the `vehicle_log` database, and that your `DB_USERNAME` and `DB_PASSWORD` match your local MySQL credentials.

**"Web server failed to start. Port 8080 was already in use."**
* **Cause:** Another application (or a previously crashed instance of this application) is already using port 8080.
* **Fix:** Stop the other service using port 8080, or change the default Spring Boot port by passing a new port in your environment variables (e.g., `SERVER_PORT=8081 .\mvnw javafx:run`). 

**"UnsupportedClassVersionError" during Maven build**
* **Cause:** Your system is using an older version of Java instead of the required Java 21.
* **Fix:** Verify your Java version by running `java -version` and `javac -version`. Ensure your `JAVA_HOME` environment variable is pointing to your Java 21+ JDK installation.

**File attachments fail to save / "Access Denied"**
* **Cause:** The application lacks permission to write to the designated storage directory, or the directory path is invalid.
* **Fix:** Ensure your user account has write permissions for the `data/documents` folder (or the path defined in your `STORAGE_DIRECTORY` variable). If running as an administrator is not an option, change the `STORAGE_DIRECTORY` to a user-accessible folder like Documents.

**"Maximum upload size exceeded" when attaching documents**
* **Cause:** The attached file exceeds Spring Boot's default multipart file size limit (usually 1MB).
* **Fix:** If you haven't already overridden this in your `application.properties`, you can increase the limit by adding `spring.servlet.multipart.max-file-size=10MB` and `spring.servlet.multipart.max-request-size=10MB` to your configuration.
