# Support Ticket System

A Spring Boot-based support ticket management backend for tracking customer issues, assigning work, updating priorities, and reviewing ticket activity.

## Overview

This project provides a REST API for a helpdesk / support center, including:

- User registration and login
- JWT-based authentication and role-based authorization
- Ticket creation, search, retrieval, assignment, and status changes
- Priority and category management
- Comments and ticket history tracking
- File attachments for tickets
- Admin controls for managing users and ticket history
- OpenAPI/Swagger documentation

## Tech Stack

- Java 21
- Spring Boot 4.0.7
- Spring Web / JPA / Security / Validation / Actuator
- PostgreSQL
- Flyway migrations
- JWT resource server auth
- SpringDoc OpenAPI
- Maven
- Docker Compose

## Project Structure

```text
src/
  main/
    java/com/portfolio/support/
      admin/
      attachment/
      audit/
      auth/
      config/
      notification/
      security/
      ticket/
      user/
    resources/
      application.yml
      db/migration/
  test/
compose.yaml
Dockerfile
pom.xml
uploads/
```

## Features

### Authentication and authorization

- Register a new user
- Login and receive a JWT
- Role-based access for regular users and administrators
- Protected endpoints using Spring Security

### Ticket lifecycle

- Create tickets with subject, description, category, priority, and status
- Filter tickets by status, priority, category, date range, and search text
- Retrieve ticket details
- Change status, assign owners, and update priority
- Add comments
- View ticket comment history

### Admin capabilities

- View all users
- Change user roles
- Enable or disable user accounts
- View ticket history and audit logs

### Attachments

- Upload files to a ticket
- Store attachments in the local upload directory

## Prerequisites

Before running the project, make sure you have:

- Java 21+
- Maven 3.9+
- Docker and Docker Compose
- PostgreSQL (or use the provided Docker service)

## Environment Variables

The application reads configuration from environment variables with defaults defined in `application.yml` and `.env`.

Example values:

```env
POSTGRES_DB=support_ticket
POSTGRES_USER=support
POSTGRES_PASSWORD=support
JWT_SECRET=change-this-development-secret-at-least-32-bytes
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=Admin123!
```

The app also supports:

```env
DB_URL=jdbc:postgresql://localhost:5433/support_ticket
DB_USERNAME=support
DB_PASSWORD=support
SERVER_PORT=8080
STORAGE_LOCATION=./uploads
```

## Running the Project

### 1) Start PostgreSQL with Docker

```bash
docker compose up -d postgres
```

### 2) Run the application

```bash
mvn spring-boot:run
```

Or package and run the JAR:

```bash
mvn clean package
java -jar target/*-exec.jar
```

The server will start on:

```text
http://localhost:8080
```

## Swagger / API Docs

Once the app is running, the API documentation is available at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Default Admin User

The bootstrap admin account is created using the environment variables:

- Email: `admin@example.com`
- Password: `Admin123!`

> Change these values before using the app in a non-local environment.

## Docker Build

You can build a Docker image for the application with:

```bash
docker build -t support-ticket-system .
```

Then run it:

```bash
docker run -p 8080:8080 --env-file .env support-ticket-system
```

## Useful Maven Commands

```bash
mvn clean test
mvn clean package
mvn spring-boot:run
```

## Notes

- Uploaded files are stored under the `uploads/` directory.
- Flyway runs database migrations automatically on startup.
- The application uses `hibernate.ddl-auto: validate`, so schema changes should be handled through migrations.

## License

This project is intended as a portfolio-ready backend application and is not currently configured with a formal OSS license.
