# Super Admin Management - Platform Settings Service

## 1. Overview

Platform Settings Service manages centralized, system-wide Global Settings for the
enterprise application.

The settings cover:

- Default Language
- Default Time Zone
- Default Currency
- Date Format
- Time Format
- Number Format
- Session Timeout
- Auto Logout
- Password Expiry
- Maximum Login Attempts
- Maintenance Notification
- System Announcement
- Multi-Factor Authentication (MFA)
- Email Notifications
- SMS Notifications
- Push Notifications
- Maximum File Upload Size
- Default Theme
- Maintenance Mode

The service also supports configuration validation, version history, restore to
defaults, search/filter, CSV export, audit integration and configuration
propagation.

---

## 2. FRS and Wireframe Alignment

The Global Settings requirements are organized into:

### General Settings
- Setting ID
- Setting Name
- Category
- Description
- Status

### Application Defaults
- Default Language
- Default Time Zone
- Default Currency
- Date Format
- Time Format
- Number Format

### Operational Settings
- Session Timeout
- Auto Logout
- Password Expiry
- Maximum Login Attempts
- Maintenance Notification
- System Announcement

### Security / Notification / Platform Settings
- Multi-Factor Authentication
- Email/SMS/Push Notifications
- Maximum File Upload Size
- Default Theme
- Maintenance Mode

The FRS also specifies Search, Filter, Export, Refresh, Configuration History,
Audit Viewer, Save, Update, Reset and Cancel capabilities.

The implementation maps these capabilities as follows:

| FRS / UI Capability   | Backend Implementation                |
|-----------------------|---------------------------------------|
| View settings         | GET APIs                              |
| Search                | `search` query parameter              |
| Filter                | `category`, `status` query parameters |
| Refresh               | Reuse GET API                         |
| Save / Update         | PUT API                               |
| Activate / Deactivate | PATCH status API                      |
| Restore Defaults      | POST reset API                        |
| Configuration History | History GET API                       |
| Export                | CSV export API                        |
| Audit                 | Audit integration + history           |
| Validation            | Server-side validation service        |

---

## 3. API Endpoints

| Method | Endpoint                                  | Purpose                   |
|--------|-------------------------------------------|---------------------------|
| GET    | `/api/v1/platform-settings`               | Get all settings          |
| GET    | `/api/v1/platform-settings/{key}`         | Get a setting by key      |
| PUT    | `/api/v1/platform-settings/{key}`         | Update settings           |
| PATCH  | `/api/v1/platform-settings/{key}/status`  | Activate/deactivate       |
| POST   | `/api/v1/platform-settings/reset`         | Restore defaults          |
| GET    | `/api/v1/platform-settings/{key}/history` | Get configuration history |
| GET    | `/api/v1/platform-settings/export`        | Export settings as CSV    |

### Search and Filter

The GET API supports optional query parameters:

```http
GET /api/v1/platform-settings?search=GLOBAL
GET /api/v1/platform-settings?category=GLOBAL
GET /api/v1/platform-settings?status=ACTIVE
GET /api/v1/platform-settings?search=GLOBAL&category=GLOBAL&status=ACTIVE
```

`search` performs a case-insensitive search using setting name/description.
`category` and `status` narrow the result.

Export supports the same optional filters:

```http
GET /api/v1/platform-settings/export?status=ACTIVE
```

---

## 4. Main Configuration

The current default configuration uses:

```text
Setting Key              GLOBAL_SETTINGS
Default Language         English
Default Time Zone        Asia/Kolkata
Default Currency         INR
Date Format              DD/MM/YYYY
Time Format              24 Hours
Number Format            #,##0.00
Session Timeout          30 minutes
Auto Logout              Enabled
Password Expiry          90 days
Maximum Login Attempts   5
Maintenance Notification Disabled
System Announcement      Enabled
MFA                      Enabled
Email Notifications      Enabled
SMS Notifications        Enabled
Push Notifications       Enabled
Maximum File Upload      100 MB
Default Theme            Light
Maintenance Mode         Disabled
Status                   ACTIVE
Version                  1
```

---

## 5. Validation

Server-side validation is applied before saving/activation.

Current validation rules:

```text
Default Language              Required
Default Time Zone             Required
Default Currency              Required
Date Format                   Required
Time Format                   Required
Number Format                 Required
Default Theme                 Required

Password Expiry               30 - 365 days
Session Timeout               5 - 240 minutes
Maximum Login Attempts        3 - 10
Maximum File Upload Size      Positive value
```

The FRS also requires the selected language to exist in the Language Repository.
Authoritative repository validation is an external dependency.

Invalid values return `400 Bad Request`.

---

## 6. Versioning and History

The current configuration is stored in:

```text
platform_settings
```

Historical snapshots are stored in:

```text
platform_settings_history
```

Successful configuration changes create a new version and history snapshot.

History can be retrieved with:

```http
GET /api/v1/platform-settings/GLOBAL_SETTINGS/history
```

History is returned newest version first.

The FRS requires configuration history and version control.

---

## 7. Reset to Defaults

```http
POST /api/v1/platform-settings/reset
```

No request body is required.

The service:

```text
Find current GLOBAL_SETTINGS
        ↓
Apply default values
        ↓
Validate
        ↓
Set ACTIVE
        ↓
Increment version
        ↓
Save
        ↓
Create RESTORED history
        ↓
Audit / Propagate
        ↓
Return response
```

---

## 8. Database

### `platform_settings`

Stores the current configuration.

Important fields:

```text
id
setting_name
category
description
status
default_language
default_time_zone
default_currency
date_format
time_format
number_format
session_timeout
auto_logout
password_expiry
maximum_login_attempts
maintenance_notification
system_announcement
multi_factor_authentication
email_notifications
sms_notifications
push_notifications
maximum_file_upload_size
default_theme
maintenance_mode
version_number
created_at
created_by
updated_at
updated_by
```

### `platform_settings_history`

Stores configuration snapshots and activity information:

```text
history_id
setting_id
version_number
action
activity_status
setting snapshot fields
user_id
user_name
ip_address
changed_at
```

A unique constraint prevents duplicate history versions for the same setting.

All schema changes are managed through Flyway.

---

## 9. Complete Project Structure

```text
Super-Admin-management/
│
├── pom.xml
├── README.md
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── enterprise/
│   │   │           └── superadmin/
│   │   │               └── platform_settings_service/
│   │   │                   │
│   │   │                   ├── SuperAdminApplication.java
│   │   │                   │
│   │   │                   ├── config/
│   │   │                   │   ├── OpenApiConfig.java
│   │   │                   │   └── SecurityConfig.java
│   │   │                   │
│   │   │                   ├── controller/
│   │   │                   │   └── PlatformSettingsController.java
│   │   │                   │
│   │   │                   ├── dto/
│   │   │                   │   ├── request/
│   │   │                   │   │   ├── UpdatePlatformSettingsRequest.java
│   │   │                   │   │   └── UpdateSettingStatusRequest.java
│   │   │                   │   │
│   │   │                   │   └── response/
│   │   │                   │       ├── PlatformSettingsResponse.java
│   │   │                   │       ├── PlatformSettingsHistoryResponse.java
│   │   │                   │       └── ErrorResponse.java
│   │   │                   │
│   │   │                   ├── entity/
│   │   │                   │   ├── PlatformSetting.java
│   │   │                   │   └── PlatformSettingHistory.java
│   │   │                   │
│   │   │                   ├── enums/
│   │   │                   │   ├── SettingStatus.java
│   │   │                   │   └── SettingAction.java
│   │   │                   │
│   │   │                   ├── exception/
│   │   │                   │   ├── SettingNotFoundException.java
│   │   │                   │   ├── InvalidSettingException.java
│   │   │                   │   └── GlobalExceptionHandler.java
│   │   │                   │
│   │   │                   ├── integration/
│   │   │                   │   ├── PlatformSettingsAuditIntegration.java
│   │   │                   │   ├── PlatformSettingsPropagationIntegration.java
│   │   │                   │   └── impl/
│   │   │                   │       ├── HistoryBackedPlatformSettingsAuditIntegration.java
│   │   │                   │       └── ApplicationEventPlatformSettingsPropagation.java
│   │   │                   │
│   │   │                   ├── mapper/
│   │   │                   │   └── PlatformSettingsMapper.java
│   │   │                   │
│   │   │                   ├── repository/
│   │   │                   │   ├── PlatformSettingRepository.java
│   │   │                   │   └── PlatformSettingHistoryRepository.java
│   │   │                   │
│   │   │                   ├── security/
│   │   │                   │   ├── CurrentUserProvider.java
│   │   │                   │   └── JwtAuthoritiesConverter.java
│   │   │                   │
│   │   │                   └── service/
│   │   │                       ├── PlatformSettingsService.java
│   │   │                       ├── impl/
│   │   │                       │   └── PlatformSettingsServiceImpl.java
│   │   │                       └── validation/
│   │   │                           └── PlatformSettingsValidationService.java
│   │   │
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__create_platform_settings.sql
│   │               ├── V2__create_platform_settings_history.sql
│   │               ├── V3__insert_default_platform_settings.sql
│   │               └── V4__cleanup_and_seed_history.sql
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── enterprise/
│       │           └── superadmin/
│       │               └── platform_settings_service/
│       │                   ├── controller/
│       │                   │   └── PlatformSettingsControllerTest.java
│       │                   └── service/
│       │                       └── PlatformSettingsServiceTest.java
│       └── resources/
│           └── application-test.yaml
```

---

## 10. Layer Responsibilities

| Layer       | Responsibility                                               |
|-------------|--------------------------------------------------------------|
| Controller  | REST endpoints, request parameters and OpenAPI documentation |
| DTO         | Request/response API contracts                               |
| Service     | Business logic and transaction handling                      |
| Validation  | Server-side settings validation                              |
| Repository  | PostgreSQL data access                                       |
| Entity      | Database mapping                                             |
| Mapper      | Entity ↔ DTO conversion                                      |
| Security    | JWT authentication and authority mapping                     |
| Integration | Audit and configuration propagation                          |
| Exception   | Consistent error responses                                   |
| Flyway      | Database schema and seed data                                |

---

## 11. Security

Production APIs are designed to use JWT authentication and RBAC.

Controller methods use:

```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
```

JWT roles are converted to Spring Security authorities. For example:

```json
{
  "roles": ["SUPER_ADMIN"]
}
```

becomes:

```text
ROLE_SUPER_ADMIN
```

### Temporary Local Development Bypass

When an enterprise JWT/token is not available, authentication can be temporarily
bypassed for local Swagger/Postman functional testing.

For isolated local testing:

```java
.requestMatchers("/api/v1/platform-settings/**").permitAll()
```

The controller-level `@PreAuthorize` checks must also be temporarily disabled if
method security is enabled.

This allows validation, database persistence, history, reset and export flows
to be tested without an external identity provider.

**This must not be used as the production security configuration. Restore JWT
authentication and RBAC before integration/deployment.**

---

## 12. Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8081/v3/api-docs
```

When security is enabled, use Swagger **Authorize** and provide:

```text
Bearer <access-token>
```

When using the temporary local security bypass, no token is required.

---

## 13. Postman / API Testing

Base URL:

```text
http://localhost:8081
```

Examples:

```http
GET    /api/v1/platform-settings
GET    /api/v1/platform-settings?search=GLOBAL
GET    /api/v1/platform-settings?status=ACTIVE
GET    /api/v1/platform-settings/GLOBAL_SETTINGS
PUT    /api/v1/platform-settings/GLOBAL_SETTINGS
PATCH  /api/v1/platform-settings/GLOBAL_SETTINGS/status
POST   /api/v1/platform-settings/reset
GET    /api/v1/platform-settings/GLOBAL_SETTINGS/history
GET    /api/v1/platform-settings/export
```

Recommended test sequence:

```text
1. GET current settings
2. Test Search
3. Test Category/Status filters
4. Update a setting
5. Verify version increment
6. Retrieve history
7. Activate/deactivate
8. Export CSV
9. Restore defaults
10. Test invalid validation values
11. Test invalid setting key
```

Expected common responses:

```text
200 OK
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
500 Internal Server Error
```

---

## 14. Audit and Configuration Propagation

Every successful mutation is versioned and recorded in configuration history.

A pluggable audit integration is provided so the service can connect to the
approved enterprise Audit & Compliance service.

A pluggable propagation integration is also provided. The current implementation
uses an in-process application event as an adapter. The final cross-module
configuration propagation mechanism must use the approved enterprise mechanism.

Sensitive values such as passwords, tokens and secrets must never be returned
in normal API responses or written to logs.

---

## 15. External / Future Dependencies

The following integrations require their owning service/interface contracts:

### Authentication / RBAC
- Enterprise JWT/token provider
- Final JWT claim structure
- Approved permission identifiers
- Super Administrator authorization

### Audit & Compliance
- Shared audit API/module
- Audit Viewer
- Enterprise audit metadata such as device/browser/session information

### Language Repository
- Validate that the configured language exists in the authoritative repository

### Configuration Propagation
- Propagate activated settings to consuming modules
- Cache refresh/event mechanism
- Rules for applying changes to existing sessions

### Notification Management
- Consume email/SMS/push preference settings
- Enterprise notification behavior

### User Management
- Authoritative user identity information for audit/context

### Platform Consumers
Settings will eventually be consumed by the relevant platform modules for:
- Session/security policies
- Password policies
- MFA
- Upload limits
- Maintenance mode
- Localization
- Currency/date/time formatting
- Theme and UI defaults

### Monitoring and Logging
- Centralized monitoring
- Centralized application logging
- Production alerting/observability

No external URLs, credentials, permission IDs or provider-specific contracts are
invented until the corresponding interfaces are approved.

---

## 16. Application Configuration

Example:

```yaml
spring:
  application:
    name: public-admin

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/cloud_platform}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:your-password}

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  flyway:
    enabled: true

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:9000}

server:
  port: ${SERVER_PORT:8081}
```

Use environment variables for credentials and deployment-specific values.

---

## 17. Running the Application

### Prerequisites

- JDK 21
- Maven
- PostgreSQL
- IntelliJ IDEA or another Java IDE

### Build

```bash
mvn clean compile
```

### Test

```bash
mvn clean test
```

### Run

```bash
mvn spring-boot:run
```

Or run `PlatformSettingsServiceApplication.java` from IntelliJ.

### Health Check

```text
http://localhost:8081/actuator/health
```

---

## 18. Database Verification

Current configuration:

```sql
SELECT
    setting_name,
    status,
    version_number,
    updated_at,
    updated_by
FROM platform_settings;
```

History:

```sql
SELECT
    setting_id,
    version_number,
    action,
    activity_status,
    user_name,
    ip_address,
    changed_at
FROM platform_settings_history
ORDER BY version_number;
```

---

## 19. Acceptance Coverage

The implementation supports the main FRS expectations:

- Centralized Global Settings
- Required field validation
- Range validation
- Authorized modification
- Activation validation
- Configuration versioning
- Configuration history
- Restore defaults
- Search and filtering
- Export
- Audit integration
- Safe configuration responses
- PostgreSQL persistence
- Swagger/OpenAPI documentation

The exact enterprise integrations remain dependent on the contracts supplied by
the respective services.

---

## 20. Reference Documents

This implementation is aligned with:

1. **Java Suite FRS - Global Settings**
2. **Java Suite Wireframes - Global Settings**

The FRS defines the fields, validation, business rules, lifecycle, authorization,
history/audit requirements, restore workflow, dependencies and acceptance criteria.

The Wireframes define the Global Settings UI organization and the configuration
areas for localization, security, notifications, file upload and platform
behavior.

---

## 21. Project Metadata

```text
Artifact ID:    platform-settings-service
Version:        0.0.1-SNAPSHOT
Java:           21
Spring Boot:    4.1.1
Database:       PostgreSQL
Port:           8081
Application:    super-admin
Base Package:   com.enterprise.superadmin.platform_settings_service
```

Internal enterprise project. Follow applicable source-code, security, database,
deployment and access-control policies.
