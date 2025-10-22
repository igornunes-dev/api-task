# Task Manager - Spring Boot Backend

[![Java](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Overview

This is the backend of the **Task Manager** application, a secure and scalable RESTful API built with **Spring Boot** and **Java 21**. It manages authentication, task operations, and category organization with robust access control using **JWT**.

It integrates seamlessly with the Angular frontend and follows best practices for modern backend development, including layered architecture, database versioning, and containerized deployment.

---

## Table of Contents

* [Features](#features)
* [Technologies](#technologies)
* [Architecture](#architecture)
* [Getting Started](#getting-started)

  * [Installation](#installation)
  * [Running with Docker](#running-with-docker)
* [Usage](#usage)
* [License](#license)
* [Contact](#contact)

---

## Features

* **JWT Authentication & Spring Security**
  Stateless authentication with role-based access and secure route protection.

* **Task and Category Management**
  Full CRUD support with filtering, pagination, and user ownership enforcement.

* **DTO Mapping with MapStruct**
  Clean separation of domain models and exposed data structures.

* **Pagination Support**
  Efficient handling of large task datasets using pageable API endpoints for listing and filtering.

* **Database Migrations with Flyway**
  Version-controlled schema updates for consistent environments.

* **CORS Configuration**
  Allows integration with external frontend domains (e.g., Angular app).

* **Gmail & RabbitMQ Integration**
  Automated email notifications for new user registrations and upcoming tasks.
  Uses Gmail API for sending emails.
  RabbitMQ acts as a messaging broker to queue and process email tasks asynchronously.
  Ensures reliable delivery and decouples email sending from main task processing.

* **Unit & Integration Testing**
  Implemented tests with JUnit and Mockito to ensure code reliability and coverage.

---

## Technologies

| Layer            | Technology                  |
| ---------------- | --------------------------- |
| Language         | Java 21                     |
| Framework        | Spring Boot 3+              |
| Security         | Spring Security + JWT       |
| ORM              | Spring Data JPA             |
| Mapping          | MapStruct                   |
| Database         | PostgreSQL                  |
| Migrations       | Flyway                      |
| Containerization | Docker, Docker Compose      |
| Build Tool       | Maven                       |
| Email            | Gmail API                   |
| Testing          | JUnit, Mockito              |
---

## Architecture

The backend follows a clean, layered architecture:

```
src/
└── main/
    └── java/
        └── com.example.apitask/
            ├── config/            # Global application configuration (CORS, Swagger, etc.)
            ├── controllers/       # REST controllers (define HTTP endpoints)
            ├── dtos/              # Data Transfer Objects used for requests/responses
            ├── enums/             # Enumerations for domain constraints and constants
            ├── exceptions/        # Custom exception classes and handlers
            ├── helpers/           # Utility classes and common helpers
            ├── infra.security/    # Spring Security configuration and JWT authentication
            ├── mappers/           # MapStruct mappers for converting between entities and DTOs
            ├── models/            # JPA entities (domain models)
            ├── repositories/      # Spring Data JPA repositories (data access layer)
            ├── seed/              # Initial data loading and seeding logic
            └── services/          # Business logic and service layer

```

---

## Getting Started

### Prerequisites

Make sure you have the following installed:

* [Java 21+](https://adoptium.net/)
* [Maven](https://maven.apache.org/)
* [Docker & Docker Compose](https://docs.docker.com/compose/)

---

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/yourusername/task-manager-backend.git
cd task-manager-backend
```

2. **Build the project**

```bash
./mvnw clean install
```

---

### Running with Docker

You can run the backend and database using Docker Compose:

```bash
docker-compose up --build
```

* The backend will be available at `http://localhost:8080`
* PostgreSQL will run in a separate container with persistent volume.

To stop the containers:

```bash
docker-compose down
```

> Ensure your `application.properties` (or `application.yml`) is configured for containerized PostgreSQL.

---

## Usage

* Register a new user and authenticate to receive a JWT token.
* Use the token to access secured endpoints (e.g., create, update, and delete tasks).
* Each task belongs to a user and can be organized into categories.
* Supports pagination, filtering, and sorting on task lists.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.

---

## Contact

**Igor Nunes**
📧 [igornunesle@gmail.com](mailto:igornunesle@gmail.com)

---
