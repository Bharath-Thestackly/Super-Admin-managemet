# Super Admin License Management Service

A Spring Boot microservice for managing software licenses, their lifecycle states, and tenant assignment. It exposes a REST API for creating, updating, activating, suspending, renewing, querying, and revoking licenses.

## Project overview

This service is built with:

- Java 21
- Spring Boot 3.4.5
- Spring Web / REST APIs
- Spring Data JPA
- PostgreSQL
- Flyway database migrations
- Spring Security + OAuth2 Resource Server
- SpringDoc OpenAPI / Swagger UI
- JUnit 5 + MockMvc for tests

The application name is `Super-Admin-managemet` and the main package is:

- `com.enterprise.superadmin`
- `com.enterprise.superadmin.license_management_service`

## Architecture and modules

Main application entry point:

- `src/main/java/com/enterprise/superadmin/SuperAdminApplication.java`

Core packages:

- `controller/` – REST endpoints
- `service/` – business logic and license state transitions
- `repository/` – JPA repositories
- `entity/` – persistent domain models
- `dto/` – request/response models
- `enum/` – status and type values
- `exception/` – global exception handling
- `integration/` – external service clients for tenant/organization lookup
- `config/` – security, OpenAPI and REST client config
- `db/migration/` – Flyway SQL scripts

## Database model

The service uses PostgreSQL and Flyway.

### Main table: `licenses`

Fields include:

- `id` (UUID)
- `license_key` (unique)
- `license_plan`
- `license_type`
- `activation_date`
- `expiry_date`
- `status` (`PENDING`, `ACTIVE`, `SUSPENDED`, `EXPIRED`)
- `created_at`, `updated_at`
- `created_by`, `updated_by`
- `is_deleted`, `deleted_at`, `deleted_by`

### Assignment table: `license_assignments`

Fields include:

- `id` (UUID)
- `license_id`
- `tenant_id`
- `assigned_at`
- `assigned_by`
- `revoked_at`

## Security and auth

The application uses Spring Security with JWT validation via OAuth2 Resource Server.

Public endpoints:

- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/actuator/health` (if exposed through actuator configuration)

All other endpoints require authentication.

Request headers used by some APIs:

- `X-Actor-Id` (optional but supported for lifecycle actions)

## Environment configuration

Default configuration is defined in `src/main/resources/application.properties`.

Required environment variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/cloud_platform
DB_USERNAME=postgres
DB_PASSWORD=root
JWT_ISSUER_URI=http://localhost:8080/realms/enterprise
SERVER_PORT=8083
ORGANIZATION_SERVICE_URL=http://localhost:8081
TENANT_SERVICE_URL=http://localhost:8082
```

### Default port

- Server default: `8083`

### Swagger UI

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI docs: `http://localhost:8083/v3/api-docs`

## Run the project

### Prerequisites

- JDK 21+
- PostgreSQL database running
- Maven installed or use the wrapper `./mvnw`

### Build

```bash
./mvnw clean install
```

### Run locally

```bash
./mvnw spring-boot:run
```

## API base URL

All endpoints below are under the base path:

```text
/api/v1/licenses
```

## License status values

- `PENDING`
- `ACTIVE`
- `SUSPENDED`
- `EXPIRED`

## License type values

- `SUBSCRIPTION`
- `PERPETUAL`
- `TRIAL`
- `ENTERPRISE`

## Endpoint reference

### 1) Create license

```http
POST /api/v1/licenses
```

Creates a new license.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Request body:

```json
{
  "licensePlan": "PREMIUM",
  "licenseType": "SUBSCRIPTION",
  "activationDate": "2026-09-10",
  "expiryDate": "2027-09-10"
}
```

Response: `201 Created`

Example response:

```json
{
  "id": "7d6e2ac5-e4c4-4376-b4d2-2d3a0ac4ce0d",
  "licenseKey": "LIC-TEST123",
  "licensePlan": "PREMIUM",
  "licenseType": "SUBSCRIPTION",
  "activationDate": "2026-09-10",
  "expiryDate": "2027-09-10",
  "status": "PENDING",
  "createdAt": "2026-09-10T10:00:00",
  "updatedAt": "2026-09-10T10:00:00"
}
```

---

### 2) List licenses

```http
GET /api/v1/licenses
```

Gets all non-deleted licenses.

Optional query params:

- `plan` – license plan filter
- `status` – `PENDING`, `ACTIVE`, `SUSPENDED`, `EXPIRED`

Examples:

```http
GET /api/v1/licenses
GET /api/v1/licenses?plan=PREMIUM
GET /api/v1/licenses?status=ACTIVE
GET /api/v1/licenses?plan=PREMIUM&status=ACTIVE
```

Response: `200 OK`

---

### 3) Get license by ID

```http
GET /api/v1/licenses/{licenseId}
```

Returns a single license by UUID.

Example:

```http
GET /api/v1/licenses/7d6e2ac5-e4c4-4376-b4d2-2d3a0ac4ce0d
```

Response: `200 OK`

---

### 4) Get license by key

```http
GET /api/v1/licenses/key/{licenseKey}
```

Example:

```http
GET /api/v1/licenses/key/LIC-TEST123
```

Response: `200 OK`

---

### 5) Update license

```http
PUT /api/v1/licenses/{licenseId}
```

Updates the license plan, type, activation date and expiry date.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Request body:

```json
{
  "licensePlan": "STANDARD",
  "licenseType": "ENTERPRISE",
  "activationDate": "2026-09-10",
  "expiryDate": "2027-03-10"
}
```

Response: `200 OK`

---

### 6) Activate license

```http
PATCH /api/v1/licenses/{licenseId}/activate
```

Activates a license.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Response: `200 OK`

---

### 7) Reactivate license

```http
PATCH /api/v1/licenses/{licenseId}/reactivate
```

Reactivates a suspended or inactive license if it is valid and not expired.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Response: `200 OK`

---

### 8) Suspend license

```http
PATCH /api/v1/licenses/{licenseId}/suspend
```

Suspends the license.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Response: `200 OK`

---

### 9) Renew license

```http
PATCH /api/v1/licenses/{licenseId}/renew
```

Updates the expiry date of an existing license.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Request body:

```json
{
  "newExpiryDate": "2027-09-15"
}
```

Response: `200 OK`

---

### 10) Get license status

```http
GET /api/v1/licenses/{licenseId}/status
```

Returns current license status summary.

Example response:

```json
{
  "licenseId": "7d6e2ac5-e4c4-4376-b4d2-2d3a0ac4ce0d",
  "licenseKey": "LIC-TEST123",
  "status": "ACTIVE",
  "activationDate": "2026-09-10",
  "expiryDate": "2027-09-10",
  "expired": false
}
```

Response: `200 OK`

---

### 11) Assign license to tenant

```http
POST /api/v1/licenses/{licenseId}/assign
```

Assigns a license to a tenant.

Request body:

```json
{
  "tenantId": "e6f82a99-041f-46d0-b8d6-b0bbd6a1d39c",
  "assignedBy": "6f0a18d6-7d7b-4eef-b7ab-6796882b5eb8"
}
```

Response: `200 OK`

Example response:

```json
{
  "id": "7d6e2ac5-e4c4-4376-b4d2-2d3a0ac4ce0d",
  "licenseKey": "LIC-TEST123",
  "licensePlan": "PREMIUM",
  "licenseType": "SUBSCRIPTION",
  "activationDate": "2026-09-10",
  "expiryDate": "2027-09-10",
  "status": "ACTIVE",
  "createdAt": "2026-09-10T10:00:00",
  "updatedAt": "2026-09-10T10:00:00"
}
```

---

### 12) Revoke license assignment

```http
DELETE /api/v1/licenses/{licenseId}/assign/{tenantId}
```

Revokes a tenant assignment for a given license.

Request headers:

- `X-Actor-Id: <uuid>` (optional)

Response: `204 No Content`

## Error handling

The project includes a global exception handler for cases like:

- license not found
- invalid state transitions
- expired license access or activation
- already assigned license
- validation failures

Common HTTP status codes include:

- `200 OK`
- `201 Created`
- `204 No Content`
- `400 Bad Request`
- `404 Not Found`
- `409 Conflict`
- `500 Internal Server Error`

## Test suite

The project includes tests under `src/test/java` for:

- controller validation and endpoint routes
- repository queries
- service lifecycle logic

Key test classes:

- `src/test/java/com/enterprise/superadmin/controller/LicenseControllerTest.java`
- `src/test/java/com/enterprise/superadmin/controller/LicenseAssigmentControllerTest.java`
- `src/test/java/com/enterprise/superadmin/license_management_service/service/LicenseServiceTest.java`
- `src/test/java/com/enterprise/superadmin/license_management_service/service/LicenseAssignmentServiceTest.java`
- `src/test/java/com/enterprise/superadmin/repository/LicenseRepositoryTest.java`

Run tests:

```bash
./mvnw test
```

## Example curl commands

### Create license

```bash
curl -X POST http://localhost:8083/api/v1/licenses \
  -H "Content-Type: application/json" \
  -H "X-Actor-Id: 11111111-1111-1111-1111-111111111111" \
  -d '{
    "licensePlan": "PREMIUM",
    "licenseType": "SUBSCRIPTION",
    "activationDate": "2026-09-10",
    "expiryDate": "2027-09-10"
  }'
```

### Get all licenses

```bash
curl http://localhost:8083/api/v1/licenses
```

### Assign license

```bash
curl -X POST http://localhost:8083/api/v1/licenses/{licenseId}/assign \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "e6f82a99-041f-46d0-b8d6-b0bbd6a1d39c",
    "assignedBy": "6f0a18d6-7d7b-4eef-b7ab-6796882b5eb8"
  }'
```

## Notes

- The service is built to manage licensing workflows for a multi-tenant platform.
- License lifecycle transitions are validated before changes are saved.
- Assignment operations are tracked with timestamps and actor metadata.
- Flyway runs automatically on startup and applies SQL scripts under `src/main/resources/db/migration`.

## Summary

This application provides a complete license management API centered around:

- license creation and validation
- plan and status filtering
- date-based lifecycle actions
- tenant assignment and revocation
- secure JWT-based access
- Swagger documentation

If you want, I can also generate a Postman collection export or add a sample `.env.example` file for this project.
