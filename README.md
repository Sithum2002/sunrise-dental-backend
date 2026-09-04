# Sunrise Dental Clinic — Backend

A production-style Spring Boot REST API for managing a dental clinic. It handles patients, appointments, dentists, treatments, billing and payments, notifications (email/SMS), audit logging, role-based access control, and JasperReports PDF reporting.

Built as an academic assignment demonstrating a complete, layered, industry-grade Java backend. Includes a comprehensive JUnit 5 / Mockito unit test suite (393 tests).

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
  - [Default Seeded Users](#default-seeded-users)
- [Project Structure](#project-structure)
- [Authentication & Authorization](#authentication--authorization)
- [API Endpoints](#api-endpoints)
- [Reports](#reports)
- [Testing](#testing)
- [Documentation](#documentation)
- [Environment Variables](#environment-variables)
- [Release Notes](#release-notes)

## Features

- **Authentication & Authorization** — JWT access/refresh tokens, HTTP-only cookies, spring-security role-based access (`ADMIN`, `RECEPTIONIST`, `DOCTOR`).
- **Patient Management** — full CRUD, search/filter, auto-assigned registration numbers (`SD-P####`).
- **Appointments** — register, reschedule, cancel, confirm, complete, no-show; overlap & clinic-hours validation; appointment numbers (`AP-YYYY-####`).
- **Billing & Payments** — bill generation from completed appointments, partial/installment payments, automatic status transitions (Paid / Partially Paid / Unpaid), bills (`B-####`).
- **Dentists & Treatments** — CRUD plus availability status; active/inactive treatments cannot be booked.
- **Reports** — 8 PDF reports via JasperReports (see [Reports](#reports)).
- **Notifications** — in-app + email/SMS notifications triggered by lifecycle events (async via Spring events).
- **Audit Trail** — every CREATE / UPDATE / DELETE logged; auditable entities capture created-by / last-modified-by.
- **Dashboard** — KPI aggregations, today's schedule, appointment status distribution, monthly revenue (MySQL stored procedure with graceful fallback).
- **Number Sequences** — concurrency-safe sequential numbers via a pessimistic-locked counter row.
- **Seed Data** — `DataInitializer` seeds default users, dentists, and treatments when the database is empty.
- **OpenAPI / Swagger UI** — interactive API documentation.
- **Health & Metrics** — Spring Boot Actuator endpoints.

## Tech Stack

| Layer       | Technology                                              |
|-------------|---------------------------------------------------------|
| Language    | Java 17                                                 |
| Framework   | Spring Boot 3.4.1, Spring Security 6, Spring Data JPA   |
| Build       | Maven                                                   |
| Database    | MySQL 8, HikariCP connection pool                       |
| Security    | JJWT (JWT), BCrypt password encoder, HttpOnly cookies    |
| Reports     | JasperReports                                           |
| Async       | Spring `@Async` + `ApplicationEventPublisher`           |
| Validation  | Bean Validation 3 (`jakarta.validation`), custom validators |
| API Docs    | springdoc-openapi (Swagger UI)                          |
| Testing     | JUnit 5, Mockito, JUnit assertions                  |

MapStruct (with the Lombok binding) generates the entity ↔ DTO mappers at compile time.

## Getting Started

### Prerequisites

- **JDK 17** or later
- **Maven 3.8+**
- **MySQL 8** (or any MySQL-compatible server, e.g. Aiven, PlanetScale dev)
- Optional: an SMTP server and an SMS gateway for email/SMS notifications

### Configuration

The application reads configuration from `src/main/resources/application.yml` (defaults) and `application-dev.yml` (the `dev` profile, active by default).

All sensitive/instance-specific values are configurable through environment variables. **Never hardcode database credentials** — supply them via environment variables as shown below.

Minimal configuration you must provide before the app can connect to a database:

```bash
export DB_PASSWORD='your-mysql-password'
```

Optional but recommended:

```bash
export SPRING_PROFILES_ACTIVE=dev            # dev is the default
export SERVER_PORT=8080
export JWT_ACCESS_SECRET='<64-char-random>'
export JWT_REFRESH_SECRET='<64-char-random>'
export CORS_ALLOWED_ORIGINS='http://localhost:3000,http://localhost:3001'
export MAIL_ENABLED=false
export SMS_ENABLED=false
export MAIL_HOST=localhost
export MAIL_PORT=587
export MAIL_USERNAME=''
export MAIL_PASSWORD=''
```

See the [Environment Variables](#environment-variables) section for the full list.

### Running the Application

Clone and run:

```bash
git clone https://github.com/Sithum2002/sunrise-dental-backend.git
cd sunrise-dental-backend
export DB_PASSWORD='your-mysql-password'
mvn spring-boot:run
```

Or build and run the jar:

```bash
mvn clean package
java -jar target/sunrisedental-backend-1.0.0.jar
```

The API starts at `http://localhost:8080`.

> `spring.jpa.hibernate.ddl-auto=update` creates/updates schema automatically. Stored procedures used by the dashboard are initialised by `DbProcedureInitializer` on startup.

### Default Seeded Users

When the database is empty, `DataInitializer` seeds the following users:

| Username      | Role          | Password        | Notes                    |
|---------------|---------------|-----------------|--------------------------|
| `admin`       | `ADMIN`       | `Admin@123`     | Full control + reports   |
| `receptionist`| `RECEPTIONIST`| `Rec@12345`     | Patients/appointments/billing |
| `dr.perera`   | `DOCTOR`      | `Doctor@123`    | Manages own appointments |

> **Security warning:** the seed users ship with fixed passwords for demo purposes. Change them (or disable seeding) before deploying to any shared environment.

## Project Structure

```
src/main/java/com/sunrise/dental
├── audit/          # AuditLog entity, AuditService, Auditable, auditor-aware
├── config/         # Security-related + app config, DataInitializer, DbProcedureInitializer
├── constant/       # AppConstants, RegexPatterns, SecurityConstants
├── controller/     # REST controllers
├── dto/            # request & response DTOs
├── entity/         # JPA entities
├── enums/          # Role, AppointmentStatus, PaymentStatus, ReportType, etc.
├── event/          # Application events + NotificationListener
├── exception/      # Custom exceptions + GlobalExceptionHandler
├── mapper/         # Entity <-> DTO mappers
├── repository/     # Spring Data JPA repositories
├── scheduled/      # AppointmentReminderScheduler
├── security/       # JWT filter, JwtService, SecurityConfig, handlers
├── service/        # Service interfaces + impl
├── specification/  # JPA Specifications for filtering
├── util/           # AppDateUtils, CookieUtil, NumberUtils, SecurityUtils
└── validation/     # Custom validators / constraints
src/main/resources
├── reports/        # JasperReports .jrxml templates
├── application.yml
└── application-dev.yml
src/test/java/com/sunrise/dental/   # Unit tests
```

## Authentication & Authorization

Authentication is **stateless JWT**. On `POST /api/auth/login` the server returns:

- an **access token** (short-lived) in the response body, and
- **refresh token** in an HTTP-only cookie (`/api/auth/refresh` rotates it).

Protected endpoints verify the JWT via `JwtAuthenticationFilter`. Access tokens use `JWT_ACCESS_SECRET`; refresh tokens use `JWT_REFRESH_SECRET`.

### Authorization matrix

| Area | Allowed roles |
|------|---------------|
| `POST /api/auth/login`, `/api/auth/refresh` | Public |
| `/api/health`, `/actuator/health` | Public |
| Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`) | Public |
| `/api/admin/**` (users, dentists) | `ADMIN` |
| `/api/audit-logs/**` | `ADMIN` |
| `/api/reports/**` | `ADMIN`, `RECEPTIONIST`, `DOCTOR` |
| Everything else under `/api/**` | Any authenticated user |

Example login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123"}'
```

## API Endpoints

### Auth — `/api/auth`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Authenticate and obtain access token |
| POST | `/api/auth/refresh` | Rotate refresh token |
| POST | `/api/auth/logout` | Invalidate the session |
| GET | `/api/auth/me` | Current authenticated user profile |
| POST | `/api/auth/register-first-admin` | Register the first admin (when no users exist) |

### Patients — `/api/patients`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/patients` | List (paginated/filtered/search) |
| GET | `/api/patients/{id}` | Get by ID |
| GET | `/api/patients/reg-no/{regNo}` | Get by registration number |
| POST | `/api/patients` | Create patient |
| PUT | `/api/patients/{id}` | Update patient |
| DELETE | `/api/patients/{id}` | Deactivate patient |
| GET | `/api/patients/{id}/history` | Treatment history |

### Appointments — `/api/appointments`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/appointments` | Register appointment |
| GET | `/api/appointments` | List (filtered/paginated) |
| GET | `/api/appointments/{id}` | Get by ID |
| GET | `/api/appointments/number/{appointmentNumber}` | Get by appointment number |
| GET | `/api/appointments/today` | Today's appointments |
| GET | `/api/appointments/upcoming` | Upcoming appointments today |
| PATCH | `/api/appointments/{id}/reschedule` | Reschedule |
| PATCH | `/api/appointments/{id}/cancel` | Cancel |
| PATCH | `/api/appointments/{id}/confirm` | Confirm |
| PATCH | `/api/appointments/{id}/complete` | Complete (generates bill) |
| PATCH | `/api/appointments/{id}/no-show` | Mark as no-show |

### Treatments — `/api/treatments`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/treatments` | List |
| GET | `/api/treatments/{id}` | Get by ID |
| POST | `/api/treatments` | Create |
| PUT | `/api/treatments/{id}` | Update |
| DELETE | `/api/treatments/{id}` | Deactivate |

### Billing — `/api/billing`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/billing` | Generate bill |
| GET | `/api/billing` | List |
| GET | `/api/billing/{id}` | Get by ID |
| GET | `/api/billing/number/{billNumber}` | Get by bill number |
| GET | `/api/billing/patient/{patientId}` | Bills of a patient |
| GET | `/api/billing/{billId}/payments` | Payments of a bill |
| POST | `/api/billing/payments` | Record a payment |

### Dentists (admin) — `/api/admin/dentists`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/dentists` | List |
| GET | `/api/admin/dentists/{id}` | Get by ID |
| POST | `/api/admin/dentists` | Create |
| PUT | `/api/admin/dentists/{id}` | Update |
| DELETE | `/api/admin/dentists/{id}` | Mark unavailable |
| PATCH | `/api/admin/dentists/{id}/status` | Set status |
| GET | `/api/admin/dentists/available` | Available dentists |

### Users (admin) — `/api/admin/users`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/admin/users` | List |
| GET | `/api/admin/users/{id}` | Get by ID |
| POST | `/api/admin/users` | Create |
| PUT | `/api/admin/users/{id}` | Update |
| DELETE | `/api/admin/users/{id}` | Delete |
| PATCH | `/api/admin/users/{id}/toggle-active` | Activate/deactivate |
| PATCH | `/api/admin/users/{id}/unlock` | Unlock account |

### Dashboard / Reports / Misc

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard/stats` | Dashboard KPIs |
| GET | `/api/reports/generate` | Generate a PDF report |
| GET | `/api/notifications` | List notifications |
| PATCH | `/api/notifications/{id}/read` | Mark as read |
| GET | `/api/help` | All help topics |
| GET | `/api/help/search` | Search help topics |
| GET | `/api/audit-logs` | Audit log entries (admin) |
| GET | `/api/health` | Service health |

## Reports

Eight JasperReports PDF reports are available via `GET /api/reports/generate?type=<TYPE>&format=<JSON|PDF|HTML>`:

| Type | Report |
|------|--------|
| `PATIENT_BILL` | Bill / Receipt |
| `APPOINTMENT_LIST` | Appointment List |
| `PATIENT_LIST` | Patient List |
| `PATIENT_TREATMENT_HISTORY` | Patient Treatment History |
| `REVENUE_SUMMARY` | Revenue Summary |
| `DENTIST_PERFORMANCE` | Dentist Performance |
| `TREATMENT_POPULARITY` | Treatment Popularity |
| `MISSED_APPOINTMENTS` | Missed / No-show Appointments |

## Documentation

Once running:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
- **Actuator:** http://localhost:8080/actuator/health

## Testing

The project ships with an extensive JUnit 5 / Mockito unit test suite covering services, controllers, entities, DTOs, validators, utilities, security, exceptions, constants and events.

Run all tests:

```bash
mvn test
```

Run a single test class (or a nested group):

```bash
mvn test -Dtest=AppointmentServiceImplTest
mvn test -Dtest='AppointmentServiceImplTest$Register'
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_PASSWORD` | — | MySQL database password (required) |
| `JWT_ACCESS_SECRET` | dev default | Secret for access tokens |
| `JWT_REFRESH_SECRET` | dev default | Secret for refresh tokens |
| `COOKIE_SECURE` | `false` | Set `true` behind HTTPS |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:3001` | Allowed frontend origins |
| `REMINDER_CRON` | `0 0 7 * * *` | Appointment reminder schedule |
| `MAIL_ENABLED` | `false` | Enable email notifications |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `587` | SMTP server |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | empty | SMTP credentials |
| `SMS_ENABLED` | `false` | Enable SMS (simulated gateway) |

> **Important:** the JWT secrets and DB host shown in the config files are development defaults. Rotate them in any non-local environment.

## Release Notes

- **v1.0.0** — Initial production-ready release: full backend feature set plus the complete unit test suite (393 tests passing).