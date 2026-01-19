# LeetCode SRS (MVP)

A Spaced Repetition System for LeetCode problems, built with Spring Boot, PostgreSQL, and Clean Architecture.

## Features
- **Smart Sync**: Automatically pulls your recent LeetCode submissions.
- **SM-2 Algorithm**: Schedules reviews based on performance (Spaced Repetition).
- **Email Reminders**: Daily notifications for due problems (via Gmail).
- **Resilience**: Protects against LeetCode API rate limits using Circuit Breakers.

## API Endpoints
The API is public (no authentication required). A **Postman Collection** (`leetcode_srs.postman_collection.json`) is included in the root directory for easy testing.

### User Management
- **Create User**: `POST /api/admin/users`
  ```json
  {
    "email": "you@example.com",
    "leetcodeUsername": "your_handle"
  }
  ```
- **Sync User**: `POST /api/admin/sync/{userId}`
  - Fetches submissions and creates study items.

### Spaced Repetition
- **Review Item**: `POST /api/reviews`
  - Submits a qualty rating (0-5) to update the item's schedule.
  ```json
  {
    "studyItemId": 1,
    "quality": 5
  }
  ```
- **Reset Progress**: `POST /api/admin/reset/{userId}`

### System
- **Trigger Reminders**: `POST /api/admin/reminders`
  - Manually fires the daily email job.

## Local Development Guide

### 1. Prerequisites
- **Java 21**
- **Docker Desktop** (for PostgreSQL)
- **Maven**
- **VS Code** (Recommended) or IntelliJ

### 2. Start Local Database
Use Docker Compose to spin up the PostgreSQL database:
```bash
docker-compose up -d
```
This starts a database at `localhost:5432`.
*   **User**: `postgres`
*   **Password**: `password`

### 3. Configure Environment
**Important**: The application defaults to a Cloud Database configuration. To run locally, you **MUST** override the following environment variables.

#### Recommended: VS Code (`.vscode/launch.json`)
Add this `env` block to your run configuration:
```json
"env": {
    "DATABASE_URL": "jdbc:postgresql://localhost:5432/leetcode_srs",
    "DB_USERNAME": "postgres",
    "DB_PASSWORD": "password",
    "MAIL_USERNAME": "your_email@gmail.com",
    "MAIL_PASSWORD": "your_app_password"
}
```

#### Alternative: PowerShell / Terminal
```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/leetcode_srs"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="password"
$env:MAIL_USERNAME="your_email@gmail.com"
$env:MAIL_PASSWORD="your_app_password"

mvn spring-boot:run
```

### 4. Run the Application
```bash
mvn spring-boot:run
```
The server will start on port **8080**.

### 5. Verify
*   **Health Check**: `GET http://localhost:8080/actuator/health`

---
*For cloud deployment instructions (Render + Neon), refer to the [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md).*
