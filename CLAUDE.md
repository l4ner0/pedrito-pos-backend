# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.5.15 backend for **Pedrito POS** — a multi-tenant point-of-sale system. Java 21, Maven, PostgreSQL 16, Spring Data JPA, Flyway, Spring Security, JWT (jjwt 0.12.6), Lombok.

## Commands

```bash
# Start PostgreSQL (required before running the app)
docker compose up -d

# Run the application
./mvnw spring-boot:run          # Linux/Mac
mvnw.cmd spring-boot:run        # Windows

# Build (skip tests)
./mvnw package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthServiceTest

# Run a single test method
./mvnw test -Dtest=AuthServiceTest#loginShouldReturnToken
```

## Database

PostgreSQL via Docker Compose (`localhost:5432`, db/user/password: `pedritopos` / `pedritopos` / `pedritopos123`).

Schema is managed exclusively by **Flyway** (`ddl-auto: validate`). Migration files live in `src/main/resources/db/migration/` following the `V{n}__{description}.sql` naming convention. Never change `ddl-auto` to `create` or `update`.

The VS Code launch config reads from a `.env` file at the project root for local overrides.

## Architecture

### Module structure

The codebase is organised into feature modules, each with the same internal layout:

```
com.pedritopos
├── backend/BackendApplication.java   ← entry point (legacy package, keep as-is)
├── shared/
│   ├── config/SecurityConfig.java    ← Spring Security + JWT filter wiring
│   ├── domain/BaseEntity.java        ← UUID PK + createdAt (@MappedSuperclass)
│   └── security/                     ← JwtService, JwtAuthFilter
└── {module}/
    ├── controllers/   ← @RestController, mapped to /v1/{module}/
    ├── services/      ← @Service, business logic
    ├── repositories/  ← Spring Data JPA interfaces
    ├── domain/        ← @Entity classes (extend BaseEntity)
    └── dto/
        ├── request/   ← inbound records (Bean Validation here)
        └── response/  ← outbound records
```

Currently implemented: `auth` (login, register).
Pending: `catalog`, `sales`, `analytics`, `settings` (folder stubs already created).

### Multi-tenancy

Every entity and every query must be scoped by `business_id`. The JWT payload carries `userId`, `businessId`, and `role` — extract `businessId` from the security context (via `JwtAuthFilter`, which stores the `userId` as principal) when needed in services.

### Security

`SecurityConfig` configures stateless JWT auth. Public endpoints: `POST /v1/auth/login` and `POST /v1/auth/register`. All other routes require a valid Bearer token.

`JwtAuthFilter` validates the token and sets a `UsernamePasswordAuthenticationToken` with authorities in the format `ROLE_{role}` (e.g. `ROLE_ADMIN`, `ROLE_CAJERO`).

Use `@PreAuthorize("hasRole('ADMIN')")` on controller methods that need role enforcement.

### JWT configuration

Properties read by `JwtService` must be top-level in `application.yml` (not nested under `spring:`):

```yaml
jwt:
  secret: "<min-32-char secret>"
  expiration-ms: 28800000   # 8 hours
```

### Key domain facts (from V1 migration)

- Roles: `ADMIN`, `CAJERO`
- Payment methods: `Efectivo`, `Tarjeta`, `Yape`
- Products have `version` (optimistic locking), `low_stock_threshold`, and a partial index on `active = true`
- `sale_items` stores a snapshot of `product_name` and `unit_price` at the time of sale (denormalised intentionally)
- All timestamps are `TIMESTAMPTZ` stored as `Instant` in Java
