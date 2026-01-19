# Deploying LeetCode SRS to the Cloud

We will deploy this application using **Render** (for the backend) and **Neon** (for the PostgreSQL database). Both offer excellent free tiers.

## Prerequisites
1.  **GitHub Account**: Your code must be pushed to a public or private GitHub repository.
2.  **Render Account**: Sign up at [render.com](https://render.com).
3.  **Neon Account**: Sign up at [neon.tech](https://neon.tech).

---

## Step 1: Push Code to GitHub
If you haven't already, push your current code to GitHub.
```bash
git init
git add .
git commit -m "Initial commit"
# Create a new repo on GitHub, then run the commands shown there, e.g.:
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/leetcode-srs.git
git push -u origin main
```

---

## Step 2: Set up the Database (Neon)
1.  Log in to **Neon Console**.
2.  Click **"New Project"**.
3.  Name it `leetcode-srs-db`.
4.  Copy the **Connection String**. It will look like:
    `postgres://alex:AbC123_@ep-cool-frog-123456.us-east-2.aws.neon.tech/neondb?sslmode=require`
5.  **Save these details** separately:
    *   **Host**: `ep-cool-frog-123456.us-east-2.aws.neon.tech`
    *   **Database**: `neondb`
    *   **Username**: `alex`
    *   **Password**: `AbC123_`

---

## Step 3: Deploy Backend (Render)
1.  Log in to **Render Dashboard**.
2.  Click **"New +"** -> **"Web Service"**.
3.  Connect your GitHub repository.
4.  **Configure the Service**:
    *   **Name**: `leetcode-srs-api`
    *   **Runtime**: **Docker** (This is crucial! Do not select Java).
    *   **Instance Type**: **Free**.
5.  **Environment Variables**:
    Scroll down to "Environment Variables" and add these:

    | Key | Value (Example) | Description |
    | :--- | :--- | :--- |
    | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<HOST>/<DB>?sslmode=require` | Replace `<HOST>` and `<DB>` from Neon. Example: `jdbc:postgresql://ep-cool-frog.../neondb?sslmode=require` |
    | `SPRING_DATASOURCE_USERNAME` | `<USERNAME>` | Your Neon username. |
    | `SPRING_DATASOURCE_PASSWORD` | `<PASSWORD>` | Your Neon password. |
    | `MAIL_USERNAME` | `your_email@gmail.com` | Your real Gmail address. |
    | `MAIL_PASSWORD` | `abcd efgh ijkl mnop` | Your App Password. |
    | `PORT` | `8080` | Explicitly tell Render the port. |

    **Note on JDBC URL**: Make sure to add `jdbc:` at the start. It should look like `jdbc:postgresql://...` NOT `postgres://...`.

6.  Click **"Create Web Service"**.

---

## Step 4: Verify Deployment
1.  Render will start building your Docker image (checking out code, downloading Maven dependencies, compiling). This may take 5-10 minutes.
2.  Watch the logs for "Started LeetCodeSRSApplication".
3.  Once live, Render will give you a public URL (e.g., `https://leetcode-srs-api.onrender.com`).
4.  **Test it**:
    *   Open Postman.
    *   Replace `http://localhost:8080` with your new Render URL.
    *   Try hitting `GET /actuator/health`.

## Troubleshooting
*   **Database Connection Failed**: Double check the `SPRING_DATASOURCE_URL` format. It must be `jdbc:postgresql://...` and include `?sslmode=require` for Neon.
*   **Build Failures**: Check the Render logs. Usually due to Maven wrapper issues. If `mvnw` permission denied, `chmod +x mvnw` in your repo before pushing helps (though our Dockerfile uses a global maven image so this shouldn't be an issue).
