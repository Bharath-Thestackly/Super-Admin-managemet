# Developer Guide for Super Admin License Management

This is the onboarding guide for the Java Spring Boot license module in this project.

## 1. Project purpose

This service manages software licenses for a multi-tenant platform. It supports:

- creating licenses
- listing and filtering licenses
- retrieving a license by ID or key
- updating license details
- activating, reactivating, and suspending licenses
- renewing expiry dates
- checking license status
- assigning and revoking license assignments to tenants

Main startup class:

- [src/main/java/com/enterprise/superadmin/SuperAdminApplication.java](src/main/java/com/enterprise/superadmin/SuperAdminApplication.java)

## 2. Module structure

The relevant code is organized as follows:

- [src/main/java/com/enterprise/superadmin/license_management_service/controller](src/main/java/com/enterprise/superadmin/license_management_service/controller) – REST endpoints
- [src/main/java/com/enterprise/superadmin/license_management_service/service](src/main/java/com/enterprise/superadmin/license_management_service/service) – business logic and status transitions
- [src/main/java/com/enterprise/superadmin/license_management_service/repository](src/main/java/com/enterprise/superadmin/license_management_service/repository) – JPA queries
- [src/main/java/com/enterprise/superadmin/license_management_service/entity](src/main/java/com/enterprise/superadmin/license_management_service/entity) – entity models
- [src/main/java/com/enterprise/superadmin/license_management_service/dto](src/main/java/com/enterprise/superadmin/license_management_service/dto) – request and response contracts
- [src/main/java/com/enterprise/superadmin/license_management_service/enums](src/main/java/com/enterprise/superadmin/license_management_service/enums) – status/type enums
- [src/main/java/com/enterprise/superadmin/license_management_service/exception](src/main/java/com/enterprise/superadmin/license_management_service/exception) – custom exceptions and global error handling
- [src/main/java/com/enterprise/superadmin/license_management_service/config](src/main/java/com/enterprise/superadmin/license_management_service/config) – security and API config
- [src/main/resources/db/migration](src/main/resources/db/migration) – Flyway migration scripts
- [src/test/java](src/test/java) – controller, repository, and service tests

## 3. Tech stack

- Java 21
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Security
- OAuth2 Resource Server
- SpringDoc OpenAPI / Swagger UI
- JUnit 5 + MockMvc

## 4. Prerequisites

Before running the app, make sure you have:

- JDK 21+
- Maven or Maven wrapper available
- PostgreSQL installed and running
- A local or remote Keycloak/OAuth2 issuer for JWT validation

## 5. Configuration

The application configuration is in [src/main/resources/application.properties](src/main/resources/application.properties).

Default settings:

```properties
spring.application.name=Super-Admin-managemet

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/cloud_platform}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:root}

spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

spring.security.oauth2.resourceserver.jwt.issuer-uri=${JWT_ISSUER_URI:http://localhost:8080/realms/enterprise}

server.port=${SERVER_PORT:8083}
organization-service-url=${ORGANIZATION_SERVICE_URL:http://localhost:8081}
tenant-service-url=${TENANT_SERVICE_URL:http://localhost:8082}

springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Required environment variables

```bash
DB_URL=jdbc:postgresql://localhost:5432/cloud_platform
DB_USERNAME=postgres
DB_PASSWORD=root
JWT_ISSUER_URI=http://localhost:8080/realms/enterprise
SERVER_PORT=8083
ORGANIZATION_SERVICE_URL=http://localhost:8081
TENANT_SERVICE_URL=http://localhost:8082
```

## 6. Run the application

From the project root:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The app runs on:

```text
http://localhost:8083
```

Swagger UI:

```text
http://localhost:8083/swagger-ui.html
```

OpenAPI docs:

```text
http://localhost:8083/v3/api-docs
```

## 7. Security notes

The app uses Spring Security and requires JWT authentication for most requests.

Public endpoints:

- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

All other API routes require authentication.

Some operations also accept `X-Actor-Id` as an optional header for audit tracking.

## 8. Base API path

All license APIs are under:

```text
/api/v1/licenses
```

## 9. License model

### Status values

- `PENDING`
- `ACTIVE`
- `SUSPENDED`
- `EXPIRED`

### Type values

- `SUBSCRIPTION`
- `PERPETUAL`
- `TRIAL`
- `ENTERPRISE`

## 10. API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/licenses` | Create a license |
| GET | `/api/v1/licenses` | List/filter licenses |
| GET | `/api/v1/licenses/{licenseId}` | Get license by ID |
| GET | `/api/v1/licenses/key/{licenseKey}` | Get license by key |
| PUT | `/api/v1/licenses/{licenseId}` | Update license |
| PATCH | `/api/v1/licenses/{licenseId}/activate` | Activate license |
| PATCH | `/api/v1/licenses/{licenseId}/reactivate` | Reactivate license |
| PATCH | `/api/v1/licenses/{licenseId}/suspend` | Suspend license |
| PATCH | `/api/v1/licenses/{licenseId}/renew` | Renew expiry date |
| GET | `/api/v1/licenses/{licenseId}/status` | Get license status summary |
| POST | `/api/v1/licenses/{licenseId}/assign` | Assign license to tenant |
| DELETE | `/api/v1/licenses/{licenseId}/assign/{tenantId}` | Revoke tenant assignment |

## 11. Request examples

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

### List licenses with filters

```bash
curl "http://localhost:8083/api/v1/licenses?plan=PREMIUM&status=ACTIVE"
```

### Get license by key

```bash
curl http://localhost:8083/api/v1/licenses/key/LIC-TEST123
```

### Assign license to tenant

```bash
curl -X POST http://localhost:8083/api/v1/licenses/{licenseId}/assign \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "e6f82a99-041f-46d0-b8d6-b0bbd6a1d39c",
    "assignedBy": "6f0a18d6-7d7b-4eef-b7ab-6796882b5eb8"
  }'
```

## 12. Validation and lifecycle rules

The service validates key business rules before persisting updates. Typical checks include:

- expiry date must be in the future for create/renew flows
- activation date and expiry date must be valid
- expired licenses cannot be edited or reactivated in invalid states
- status transitions are validated before activation, suspension, or reactivation
- assignment cannot duplicate an active assignment for the same license and tenant

## 13. Database model

### Table: licenses

- `id` UUID
- `license_key` unique string
- `license_plan` string
- `license_type` enum string
- `activation_date`
- `expiry_date`
- `status`
- `created_at`
- `created_by`
- `updated_at`
- `updated_by`
- `is_deleted`
- `deleted_at`
- `deleted_by`

### Table: license_assignments

- `id`
- `license_id`
- `tenant_id`
- `assigned_at`
- `assigned_by`
- `revoked_at`

## 14. Testing

Run the test suite from the project root:

```bash
./mvnw test
```

Important test coverage includes:

- controller input validation
- service state transitions
- repository behavior
- assignment logic

## 15. Useful references in the project

- [src/main/java/com/enterprise/superadmin/license_management_service/controller/LicenseController.java](src/main/java/com/enterprise/superadmin/license_management_service/controller/LicenseController.java)
- [src/main/java/com/enterprise/superadmin/license_management_service/controller/LicenseAssignmentController.java](src/main/java/com/enterprise/superadmin/license_management_service/controller/LicenseAssignmentController.java)
- [src/main/java/com/enterprise/superadmin/license_management_service/service/LicenseServiceImpl.java](src/main/java/com/enterprise/superadmin/license_management_service/service/LicenseServiceImpl.java)
- [src/main/java/com/enterprise/superadmin/license_management_service/service/LicenseAssignmentServiceImpl.java](src/main/java/com/enterprise/superadmin/license_management_service/service/LicenseAssignmentServiceImpl.java)
- [src/test/java/com/enterprise/superadmin/controller/LicenseControllerTest.java](src/test/java/com/enterprise/superadmin/controller/LicenseControllerTest.java)
- [src/test/java/com/enterprise/superadmin/controller/LicenseAssigmentControllerTest.java](src/test/java/com/enterprise/superadmin/controller/LicenseAssigmentControllerTest.java)

## 16. Summary

This project is a clean Spring Boot license management service intended for:

- license lifecycle tracking
- tenant-based assignment handling
- secure API access via JWT
- Flyway-managed PostgreSQL persistence
- Swagger-based API testing and documentation

If you are starting development here, begin with the start class, then review the controller and service implementation files, and finally test the APIs through Swagger or curl.
