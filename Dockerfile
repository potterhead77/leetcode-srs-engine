# --- Stage 1: Build the Application ---
# We use a Maven image to compile the code inside Render
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the JAR file (skipping tests speeds up deployment)
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
# We use a lightweight JRE image just to run the app
# Switched from Alpine to standard JRE to avoid SSL/DNS issues with JDBC drivers
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the JAR file from the 'build' stage above
COPY --from=build /app/target/*.jar app.jar

# Render sets the PORT env variable automatically
ENV PORT=8080
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]