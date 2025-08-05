# Task Manager Backend

This is the backend REST API for the **Task Manager** application, built with **Spring Boot**, **Java 21**, and secured using **Spring Security** with **JWT** authentication. It manages users, tasks, and categories, exposing RESTful endpoints consumed by the Angular frontend.

---

## Features

- RESTful API with CRUD operations for users, tasks, and categories  
- Secure authentication and authorization using JWT tokens  
- Password hashing and role-based access control  
- Input validation with Spring Validation  
- Object mapping with MapStruct for DTOs and entities  
- Database migrations managed by Flyway  
- Cross-Origin Resource Sharing (CORS) configured for frontend integration  
- Comprehensive unit and integration tests  

---

## Technology Stack

- **Java 21**  
- **Spring Boot 3+**  
- **Spring Security** with JWT  
- **Spring Data JPA** with PostgreSQL  
- **MapStruct** for DTO mapping  
- **Flyway** for database migrations  
- **Docker & Docker Compose** for containerized deployment  
- **Lombok** for boilerplate code reduction  

## Running with Docker

The backend API and PostgreSQL database can be easily deployed using Docker and Docker Compose to create isolated, reproducible environments.

### Prerequisites

* Ensure **Docker** and **Docker Compose** are installed and running on your machine.
* Verify installation by running:

```bash
docker --version
docker-compose --version
```

### Starting the Application

1. Navigate to the project root directory containing the `docker-compose.yml` file.

2. Build and start the containers using:

```bash
docker-compose up --build
```

This command will:

* Build the backend Docker image from your source code.
* Pull and run the official PostgreSQL Docker image.
* Set up networking between containers and configure environment variables.

3. Once running, the backend API will be accessible at:

```
http://localhost:8080
```

### Stopping the Application

To stop and remove the containers, run:

```bash
docker-compose down
```

---

## CORS Configuration

The backend allows requests from the frontend domain by configuring CORS in the security config, enabling seamless API integration.

---

## Contact

For questions or contributions, contact: [igornunesle@gmail.com](mailto:igornunesle@gmail.com)

---

Feel free to ask if you want me to generate full example configs, security setup, or more!
