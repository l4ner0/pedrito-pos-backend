# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.5.15 backend for **Pedrito POS** — a multi-tenant point-of-sale system. Java 21, Maven, PostgreSQL 16, Spring Data JPA, Flyway, Spring Security, JWT (jjwt 0.12.6), Lombok, Cloudflare R2 (AWS SDK S3 v2).

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

The project uses `spring-dotenv` (`me.paulschwarz:spring-dotenv:4.0.0`) to load the `.env` file at the project root automatically on every startup, regardless of how the app is launched (terminal or VS Code). Add secrets here — never commit this file. Required variables:

```
CF_R2_ACCOUNT_ID=
CF_R2_ACCESS_KEY=
CF_R2_SECRET_KEY=
CF_R2_BUCKET=
CF_R2_PUBLIC_URL=
```

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
│   ├── config/R2Config.java          ← S3Client bean for Cloudflare R2
│   ├── config/R2Properties.java      ← @ConfigurationProperties(prefix = "cloudflare.r2")
│   ├── domain/BaseEntity.java        ← UUID PK + createdAt (@MappedSuperclass)
│   ├── exception/                    ← GlobalExceptionHandler, ApiError
│   ├── security/                     ← JwtService, JwtAuthFilter
│   └── storage/StorageService.java   ← uploads MultipartFile to R2, returns public URL
└── {module}/
    ├── controllers/   ← @RestController, mapped to /v1/{module}/
    ├── services/      ← @Service, business logic
    ├── repositories/  ← Spring Data JPA interfaces
    ├── domain/        ← @Entity classes (extend BaseEntity)
    └── dto/
        ├── request/   ← inbound records (Bean Validation here)
        └── response/  ← outbound records
```

Implemented modules: `auth` (login, register, refresh, logout), `business` (create + read + update business data and settings), `category` (CRUD), `product` (CRUD + search), `sale` (create, read, cancel, top-products).  
Pending: `analytics`.

The `POST /v1/auth/register` endpoint requires an existing `businessId` UUID. Use `POST /v1/business` first to create the business, then use the returned `id` to register the first user.

Route naming is inconsistent across existing modules: `/v1/categories` (plural) vs `/v1/product` (singular). New modules should pick one convention deliberately.

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

`SecurityConfig` configures stateless JWT auth. Public endpoints: `POST /v1/auth/login`, `POST /v1/auth/register`, `POST /v1/auth/refresh`, `POST /v1/auth/logout`, `POST /v1/business`. All other routes require a valid Bearer token.

`JwtAuthFilter` sets authorities in the format `ROLE_{role}` (e.g. `ROLE_ADMIN`, `ROLE_CAJERO`). Use `@PreAuthorize("hasRole('ADMIN')")` on controller methods that need role enforcement.

CORS is configured in `SecurityConfig` to allow `http://localhost:3000` with credentials.

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
| `ObjectOptimisticLockingFailureException` | 409 Conflict |
| `DataIntegrityViolationException` | 500 Internal Server Error |
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
- Native paginated queries **must** include a separate `countQuery` attribute alongside `value`; without it Spring cannot compute `totalPages`/`totalElements` correctly.

### Pagination

List endpoints that can return many rows use `PagedResponse<T>` from `shared/dto/`. Build it in the service with `PagedResponse.from(page.map(this::toResponse))`. Accept `page` (default **1**) and `size` (default 20) as `@RequestParam` in the controller. The service converts to Spring's 0-based index with `PageRequest.of(page - 1, size)`. `PagedResponse.from()` adds 1 back to `pageResult.getNumber()` so the response also reflects 1-based page numbers.

### Response mapping

Services build DTOs via a private `toResponse(Entity)` method — there is no separate mapper class or layer.

### Controller route ordering

Declare specific path segments before path variables in the same controller to avoid Spring mapping a literal string as a UUID:

```java
@GetMapping("/by-category/{categoryId}")   // must come before /{id}
@GetMapping("/{id}")
```

### Business module endpoints

`POST /v1/business` — public endpoint (no JWT). Creates a business and inserts a `business_settings` row in the same transaction with `printEnabled = false` and all other settings fields null. Returns `201 Created` with the new business. Onboarding flow: call this first, then `POST /v1/auth/register` with the returned `id`.

`GET /v1/business` — returns the business of the authenticated user (scoped by JWT `businessId`).

`PATCH /v1/business` — partial update of business data (`ADMIN` only). Sending `""` for `ruc`, `address`, or `phone` clears the field to `null`; sending `null` leaves it unchanged.

`GET /v1/business/settings` — returns settings for the authenticated user's business.

`PATCH /v1/business/settings` — partial update of settings (`ADMIN` only). Consumes `multipart/form-data`. Text fields: `yapeNumber`, `yapeQrUrl`, `yapeAccountHolder`, `printEnabled`, `ticketFooter` — all `@RequestParam(required = false)`. Optional file field: `file` (image only); when present it is uploaded to Cloudflare R2 and the resulting URL overwrites `yapeQrUrl`, ignoring the `yapeQrUrl` text param. Sending `""` for any string field clears it to `null`; sending `null` leaves it unchanged.

### Key domain facts (from V1 migration)

- `businesses` table columns: `id`, `name` (VARCHAR 150, NOT NULL), `ruc` (VARCHAR 20), `address` (VARCHAR 255), `phone` (VARCHAR 20, added V9), `created_at`.
- `business_settings` table columns: `id`, `business_id` (UNIQUE FK), `yape_number` (VARCHAR 15), `yape_qr_url` (VARCHAR 500), `yape_account_holder` (VARCHAR 150, added V10), `print_enabled` (BOOLEAN NOT NULL), `ticket_footer` (VARCHAR 255). All string fields are nullable; `print_enabled` defaults to `false` when created via API.
- `users` table columns: `id`, `business_id` (FK), `username` (VARCHAR 50, nullable, added V3), `email` (VARCHAR 150, UNIQUE), `password_hash`, `full_name` (VARCHAR 150), `avatar_url` (VARCHAR 500, nullable), `role` (CHECK `ADMIN`/`CAJERO`, default `CAJERO`), `active` (default true), `created_at`.
- Category `name` is always stored lowercase (`request.name().toLowerCase()` on create and update). DB enforces `UNIQUE (business_id, name)`.
- Roles: `ADMIN`, `CAJERO`
- Payment methods: `EFECTIVO`, `YAPE` (stored uppercase; DB CHECK constraint updated in V7 migration).
- Products: `version` column maps to `@Version` (optimistic locking), `low_stock_threshold` default 8, partial index `WHERE active = true`. The response DTO includes a computed `lowStock` boolean (`stock < lowStockThreshold`). Field `logo_url` (nullable, added V5). DB enforces `UNIQUE (business_id, sku)`.
- `sales` table columns include `subtotal`, `discount_amount` (default 0), `total`, `amount_received` (nullable), `change_given` (nullable). Index on `(business_id, created_at)`.
- `sale_items` stores a snapshot of `product_name` and `unit_price` at the time of sale (denormalised intentionally). Also stores `quantity` and `line_total`. `SaleItem` does **not** extend `BaseEntity` because `sale_items` has no `created_at` column — it declares its own `@Id @GeneratedValue`.
- Sales are immutable once created except for cancellation. `status` is `ACTIVE` (default) or `CANCELLED` (V8 migration); cancellation restores product stock and requires `ADMIN` role. `ticket_code` is generated from the PostgreSQL sequence `sale_ticket_seq` (created V6), formatted as `#%04d` (e.g. `#0001`).
- Creating a sale decrements product stock inside the same `@Transactional` boundary; optimistic locking on `Product.version` protects against concurrent updates.
- All timestamps are `TIMESTAMPTZ` stored as `Instant` in Java.
- Multipart file uploads (e.g. Yape QR image) are capped at **5MB** (`spring.servlet.multipart.max-file-size` and `max-request-size`).

### Sale module endpoints

`POST /v1/sale` — create a sale. Both `ADMIN` and `CAJERO` can create. `userId` is taken from the JWT principal (not the request body). For `EFECTIVO`, `amountReceived` is required and must be `>= total`; `changeGiven` is computed server-side. For `YAPE`, `amountReceived` and `changeGiven` are `null`.

`GET /v1/sale` — paginated list. Optional filters: `ticketCode` (fuzzy), `paymentMethod`, `status`, `from` and `to` as `LocalDateTime` ISO-8601 without timezone (e.g. `2026-06-28T04:59:59.999`). The controller converts to `Instant` via `ZoneOffset.UTC`.

`GET /v1/sale/{id}` — sale detail with items.

`PATCH /v1/sale/{id}/cancel` — cancel a sale (`ADMIN` only). Restores product stock for all active items.

`GET /v1/sale/top-products` — paginated product list ordered by `total_sold DESC`, then `created_at DESC`. All active products are included (zero-sold appear at end). Returns all product fields plus `totalSold` (computed `lowStock` boolean included). This route is declared before `/{id}` in the controller to avoid routing conflicts.
