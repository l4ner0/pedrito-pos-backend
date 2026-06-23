# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.5.15 backend for "Pedrito POS" — a point-of-sale system. Java 21, Maven, PostgreSQL 16, Spring Data JPA, Bean Validation, Lombok.

## Commands

```bash
# Start the PostgreSQL database (required before running the app)
docker compose up -d

# Build (skip tests)
./mvnw package -DskipTests

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=YourTestClassName

# Run a single test method
./mvnw test -Dtest=YourTestClassName#methodName
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## Database

The database runs via Docker Compose:
- Host: `localhost:5432`
- Database: `pedritopos`
- User: `pedritopos`
- Password: `pedritopos123`

The VS Code launch config (`launch.json`) reads from a `.env` file at the project root — use it to override datasource properties for local dev if needed.

## Architecture

Base package: `com.pedritopos.backend`

The project follows a standard layered Spring Boot structure. As features are added, organize them as:
- `controller/` — REST controllers (`@RestController`)
- `service/` — business logic (`@Service`)
- `repository/` — Spring Data JPA repositories (`@Repository`)
- `entity/` / `model/` — JPA entities (`@Entity`)
- `dto/` — request/response DTOs (use Bean Validation annotations here)

Lombok is configured for both compile and test annotation processing — use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, etc. freely on entities and DTOs.

`application.yaml` is the config file (not `application.properties`). Add datasource and JPA config there.
