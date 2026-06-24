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

**Flyway checksum mismatch in development** — if a migration file is modified after being applied, fix it by removing the record and re-running:

```bash
docker exec pedritopos-db psql -U pedritopos -d pedritopos \
  -c "DELETE FROM flyway_schema_history WHERE version = '<N>'; DROP TABLE IF EXISTS <table> CASCADE;"
```

## Architecture

### Module structure

```
com.pedritopos
├── PedritoPosApplication.java        ← entry point
├── shared/
│   ├── config/SecurityConfig.java    ← Spring Security + JWT filter wiring
│   ├── domain/BaseEntity.java        ← UUID PK + createdAt (@MappedSuperclass)
│   ├── exception/                    ← GlobalExceptionHandler, ApiError
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

Implemented modules: `auth` (login, register, refresh, logout), `category` (CRUD), `product` (CRUD + search).  
Pending: `sales`, `analytics`, `settings`.

### Multi-tenancy

Every entity and every query must be scoped by `business_id`. The JWT payload carries `userId`, `businessId`, and `role`. In controllers, extract `businessId` from the `Authentication` details:

```java
private UUID getBusinessId(Authentication authentication) {
    Claims claims = (Claims) authentication.getDetails();
    return UUID.fromString(claims.get("businessId", String.class));
}
```

`JwtAuthFilter` stores the full `Claims` object in `authentication.getDetails()` and the `userId` string as the principal.

### Security

`SecurityConfig` configures stateless JWT auth. Public endpoints: `POST /v1/auth/login`, `POST /v1/auth/register`, `POST /v1/auth/refresh`, `POST /v1/auth/logout`. All other routes require a valid Bearer token.

`JwtAuthFilter` sets authorities in the format `ROLE_{role}` (e.g. `ROLE_ADMIN`, `ROLE_CAJERO`). Use `@PreAuthorize("hasRole('ADMIN')")` on controller methods that need role enforcement.

### JWT configuration

Properties must be top-level in `application.yml` (not nested under `spring:`):

```yaml
jwt:
  secret: "<min-32-char secret>"
  expiration-ms: 900000            # 15 minutes (access token)
  refresh-expiration-ms: 86400000  # 24 hours (refresh token)
```

**Token rotation:** `RefreshTokenService.create()` revokes all existing tokens for the user before issuing a new one. Every login and every refresh call rotates the refresh token.

### Error handling

`GlobalExceptionHandler` maps exceptions to HTTP status codes:

| Exception | Status |
|---|---|
| `RuntimeException` | 400 Bad Request |
| `MethodArgumentNotValidException` | 422 Unprocessable Entity |
| `Exception` (catch-all) | 500 Internal Server Error |

Throw `RuntimeException` with a Spanish message for business rule violations. The response body is `ApiError { status, message, timestamp }`.

### Soft delete

Entities with an `active` boolean use soft delete: set `active = false` rather than deleting. Repositories always filter by `active = true`. The `findActive` helper in services also checks this before update/delete operations.

### PUT vs PATCH

Modules that support partial updates use two separate request DTOs:

- `XRequest` — for `POST`, all required fields carry `@NotNull` / `@NotBlank`.
- `XPatchRequest` — for `PATCH`, all fields are nullable (no `@NotNull`/`@NotBlank`). Bean Validation annotations like `@DecimalMin` and `@Min` are kept because they skip `null` values automatically.

In the service `patch` method, each field is applied only when non-null. Exception: sending `sku` as `""` explicitly clears it (sets to `null` in DB), while sending `null` leaves it unchanged.

### Splitting services for specialized queries

When a query has different performance characteristics from the main CRUD, extract it into a dedicated `XQueryService`. See `product`:

- `ProductService` — CRUD operations.
- `ProductQueryService` — `findByCategory(businessId, categoryId)`: uses a JPQL query that benefits from the existing `idx_products_business` partial index; no JOIN needed.

The controller injects both services independently.

### Repository query conventions

- **JPQL** for queries that can use entity field names and JPA-managed types (UUID comparisons, exact matches).
- **Native query** (`nativeQuery = true`) only when using PostgreSQL-specific features unavailable in JPQL, such as `ILIKE` for case-insensitive fuzzy search.
- Optional filter params in native queries use `(:param IS NULL OR col ILIKE '%' || :param || '%')` so a `null` param skips the condition entirely — pass `null` (not empty string) from the service to activate this.

### Controller route ordering

Declare specific path segments before path variables in the same controller to avoid Spring mapping a literal string as a UUID:

```java
@GetMapping("/by-category/{categoryId}")   // must come before /{id}
@GetMapping("/{id}")
```

### Key domain facts (from V1 migration)

- Roles: `ADMIN`, `CAJERO`
- Payment methods: `Efectivo`, `Tarjeta`, `Yape`
- Products: `version` column maps to `@Version` (optimistic locking), `low_stock_threshold` default 8, partial index `WHERE active = true`. The response DTO includes a computed `lowStock` boolean (`stock < lowStockThreshold`).
- `sale_items` stores a snapshot of `product_name` and `unit_price` at the time of sale (denormalised intentionally).
- All timestamps are `TIMESTAMPTZ` stored as `Instant` in Java.
