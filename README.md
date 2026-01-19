# LeetCode SRS (MVP)

A Spaced Repetition System for LeetCode problems, built with Spring Boot, PostgreSQL, and Clean Architecture.

## Features
- **Smart Sync**: Automatically pulls your recent LeetCode submissions.
- **SM-2 Algorithm**: Schedules reviews based on performance (Spaced Repetition).
- **Email Reminders**: Daily notifications for due problems.
- **Resilience**: Protects against LeetCode API rate limits.

## Deployment Guide (Render + Neon)

### 1. Database (Neon.tech)
1. Create a new project on [Neon.tech](https://neon.tech).
2. Copy the **Direct Connection String** (e.g., `postgresql://user:pass@ep-xyz.aws.neon.tech/neondb?sslmode=require`).

### 2. Application (Render.com)
1. Create a new **Web Service** on Render.
2. Connect your repository.
3. Select **Docker** as the Runtime.
4. Add the following **Environment Variables**:

| Variable | Value |
|----------|-------|
| `DATABASE_URL` | Your Neon Connection String |
| `MAIL_USERNAME` | Your Gmail Address |
| `MAIL_PASSWORD` | Your App Password (not login password) |
| `APP_SYNC_CRON` | (Optional) Cron expression, e.g. `0 0 */6 * * *` |

5. Deploy!

## API Endpoints
The API is public (no authentication required).

- **Sync User**: `POST /api/admin/sync/{userId}`
- **Create User**: `POST /api/admin/users`
  ```json
  {
    "email": "you@example.com",
    "leetcodeUsername": "your_handle"
  }
  ```
- **Reset Progress**: `POST /api/admin/reset/{userId}`

## Local Development
1. `docker-compose up -d` (Starts Postgres)
2. `mvn spring-boot:run`
