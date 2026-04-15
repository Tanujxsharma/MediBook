# MediBook Backend - Doctor Appointment Booking System

A microservices-based doctor appointment booking system built with Spring Boot and Java. This backend manages user authentication, provider profiles, appointment management, and payments.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Running Services](#running-services)
- [API Documentation](#api-documentation)
- [Database Setup](#database-setup)
- [Service Communication](#service-communication)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Future Improvements](#future-improvements)

---

## 🎯 Project Overview

MediBook is a comprehensive appointment booking platform where:

- **Patients** can browse available doctor slots and book appointments
- **Doctors** can create appointment slots and view their bookings
- **Admins** can manage all users and appointments
- **Payment processing** is available for completed bookings

The backend is built using a **microservices architecture** with the following services:
- Auth Service (user management, JWT authentication)
- Provider Service (doctor profile management)
- Appointment Service (slot & appointment management)
- Payment Service (payment processing)
- API Gateway (request routing)

---

## 🏗️ Architecture

### Service Overview

```
┌─────────────────────────┐
│     Frontend (React)    │
└────────────┬────────────┘
             │
┌────────────▼────────────┐
│     API Gateway         │
└────────────┬────────────┘
             │
    ┌────────┼────────┬─────────┐
    │        │        │         │
    ▼        ▼        ▼         ▼
  Auth    Provider Appointment Payment
 Service  Service   Service     Service
```

### Data Flow Example: Appointment Booking

1. Patient selects a slot on frontend
2. Frontend sends request to API Gateway (8087)
3. Gateway routes to Appointment Service
4. Appointment Service validates with Provider Service
5. Appointment is recorded in database
6. Frontend initiates payment via Payment Service

---

## 🛠️ Tech Stack

### Core Technologies
- **Java 21** - Programming language
- **Spring Boot 3.x** - Framework for building microservices
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - ORM for database operations
- **Maven** - Build tool

### Database & Datastore
- **MySQL** - Primary relational database (H2 for testing)
- **Separate databases** for each service (database-per-service pattern)

### APIs & Communication
- **REST API** - HTTP-based service communication
- **RestTemplate** - HTTP client for inter-service calls
- **JWT** - Token-based authentication

### Libraries
- **Lombok** - Reducing boilerplate code
- **Jackson** - JSON serialization/deserialization
- **Validation API** - Input validation
- **Log4j/SLF4j** - Logging

---

## 🔧 Services

### 1. Auth Service
**Database:** `auth_db`

Handles user authentication, registration, and JWT token generation.

**Key Endpoints:**
- `POST /auth/signup` - User registration (Patient/Doctor)
- `POST /auth/login` - User login
- `POST /auth/refresh-token` - Refresh JWT token
- `GET /users/admin/patients` - List all patients (Admin only)
- `GET /users/internal/{userId}` - Internal service call for user details
- `POST /auth/reset-password` - Password reset

**Key Features:**
- Password hashing with BCrypt
- JWT token generation with userId, email, and role
- Google OAuth integration
- Role-based access control (PATIENT, DOCTOR, ADMIN)

---

### 2. Provider Service
**Database:** `provider_db`

Manages doctor/provider profiles and information.

**Key Endpoints:**
- `POST /providers/add` - Create provider profile (Doctor)
- `GET /providers/admin/all` - List all providers (Admin only)
- `GET /providers/{providerId}` - Get provider details by ID
- `GET /providers/internal/by-user/{userId}` - Internal service call for provider lookup
- `PUT /providers/{providerId}` - Update provider profile

**Key Features:**
- Doctor profile management
- Specialization tracking
- Clinic information storage
- Fee management

---

### 3. Appointment Service
**Database:** `appointment_db`

Manages appointment slots and bookings.

**Key Endpoints:**
- `POST /slots` - Create appointment slot (Doctor)
- `GET /slots/public` - List available slots (Public)
- `POST /appointments/book/{slotId}` - Book appointment (Patient)
- `GET /appointments/my` - Get patient's appointments
- `GET /appointments/provider` - Get doctor's appointments
- `DELETE /appointments/{appointmentId}` - Cancel appointment

**Key Features:**
- Slot creation with validation (no overlap, future dates)
- Public slot listing with provider enrichment
- Appointment booking with availability checks
- Appointment status tracking (BOOKED, COMPLETED, CANCELLED)
- Automatic slot availability updates

---

### 4. Payment Service
**Database:** `payment_db`

Handles payment processing for appointments.

**Key Endpoints:**
- `POST /payments/process` - Process appointment payment
- `GET /payments/{appointmentId}` - Get payment details
- `GET /payments/history` - Get payment history (Patient/Doctor)

**Key Features:**
- Demo payment processing
- Payment status tracking (PENDING, SUCCESS, FAILED)
- Invoice generation
- Payment history management

---

### 5. API Gateway
Centralized entry point for all frontend requests. Routes requests to appropriate backend services.

**Responsibilities:**
- Request routing based on URL path
- CORS handling
- Request/response logging
- Load balancing between services (future enhancement)

---

## 📦 Prerequisites

- **Java 21** or higher
- **Maven 3.8.x** or higher
- **MySQL 8.0** or higher
- **Git**
- **IntelliJ IDEA** or VS Code (recommended)

### Verify Installation

```bash
java -version
mvn -version
mysql --version
```

---

## 🚀 Setup & Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd medibookoffline
```

### 2. Create Databases

```bash
mysql -u root -p
```

```sql
CREATE DATABASE auth_db;
CREATE DATABASE provider_db;
CREATE DATABASE appointment_db;
CREATE DATABASE payment_db;
```

### 3. Configure Database Connections

Update `application.properties` or `application.yml` in each service's `src/main/resources/` directory with appropriate database credentials and connection details.

### 4. Build All Services

```bash
# Build from root directory
mvn clean install

# Or build individual services
cd auth-service/auth-service && mvn clean install
cd provider-service/provider-service && mvn clean install
cd appointment-service/appointment-service && mvn clean install
cd payment-service/payment-service && mvn clean install
cd api-gateway-service/api-gateway && mvn clean install
```

---

## ▶️ Running Services

### Option 1: Run from IDE

1. Open each service folder in IntelliJ IDEA or VS Code
2. Right-click the main Spring Boot application class
3. Select "Run" or "Debug"

### Option 2: Run from Terminal

**Start all services in order:**

```bash
# Terminal 1 - Auth Service
cd auth-service/auth-service
mvn spring-boot:run

# Terminal 2 - Provider Service
cd provider-service/provider-service
mvn spring-boot:run

# Terminal 3 - Appointment Service
cd appointment-service/appointment-service
mvn spring-boot:run

# Terminal 4 - Payment Service
cd payment-service/payment-service
mvn spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway-service/api-gateway
mvn spring-boot:run
```

---

## 📡 API Documentation

**API Testing with Postman:**

All endpoints can be tested using Postman. Import the collection or manually create requests for the following endpoints:

### Authentication Endpoints
- `POST /auth/signup` - User registration (Patient/Doctor)
- `POST /auth/login` - User login
- `POST /auth/refresh-token` - Refresh JWT token
- `POST /auth/reset-password` - Password reset

### Admin Endpoints
- `GET /users/admin/patients` - List all patients
- `GET /providers/admin/all` - List all providers

### Provider Endpoints
- `POST /providers/add` - Create provider profile
- `GET /providers/{providerId}` - Get provider details
- `PUT /providers/{providerId}` - Update provider profile
- `GET /providers/internal/by-user/{userId}` - Internal service call

### Appointment & Slot Endpoints
- `POST /slots` - Create appointment slot
- `GET /slots/public` - List available slots
- `POST /appointments/book/{slotId}` - Book appointment
- `GET /appointments/my` - Get patient's appointments
- `GET /appointments/provider` - Get doctor's appointments
- `DELETE /appointments/{appointmentId}` - Cancel appointment

### Payment Endpoints
- `POST /payments/process` - Process appointment payment
- `GET /payments/{appointmentId}` - Get payment details
- `GET /payments/history` - Get payment history

**Authentication:**
Include JWT token in the Authorization header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

---

## 🗄️ Database Setup

### Auto-Schema Generation (via JPA)

Each service's `application.properties` has:
```properties
spring.jpa.hibernate.ddl-auto=update
```

This automatically creates tables on startup. Tables will include:
- **auth_db:** `users`
- **provider_db:** `providers`
- **appointment_db:** `slots`, `appointments`
- **payment_db:** `payments`

### Manual Schema (Optional)

See individual service documentation for custom SQL scripts.

### Database Diagram

**auth_db - users table:**
```
id (PK) | email | password | firstName | lastName | role | createdAt | updatedAt
```

**provider_db - providers table:**
```
id (PK) | userId | specialization | clinicName | fees | isBookable | createdAt | updatedAt
```

**appointment_db:**
```
slots table:
id (PK) | providerId | startTime | endTime | isBookable | createdAt

appointments table:
id (PK) | slotId | patientId | status | createdAt | updatedAt
```

**payment_db - payments table:**
```
id (PK) | appointmentId | amount | status | createdAt | updatedAt
```

---

## 🔗 Service Communication

### How Services Talk

Currently, services communicate via **synchronous HTTP calls** using `RestTemplate`:

**Example: Appointment Service → Provider Service**
```java
RestTemplate restTemplate = new RestTemplate();
ResponseEntity<Provider> response = restTemplate.getForEntity(
    "http://localhost:8082/providers/internal/by-user/" + userId,
    Provider.class
);
Provider provider = response.getBody();
```

### Service Endpoints (Internal)

**Provider Service:**
- `GET /providers/internal/by-user/{userId}` - Get provider by user ID

**Auth Service:**
- `GET /users/internal/{userId}` - Get user details by user ID

### Hardcoded Service URLs

- Auth Service: `http://localhost`
- Provider Service: `http://localhost`
- Appointment Service: `http://localhost`
- Payment Service: `http://localhost`

### Limitations & Future Improvements

**Current Issues:**
- Tight service coupling
- No retry logic
- No circuit breaker
- No service discovery
- Service down = dependent services fail

**Future Solutions:**
1. **OpenFeign** - Declarative HTTP clients
2. **Service Discovery (Eureka)** - Dynamic service registration
3. **Message Queue (Kafka/RabbitMQ)** - Async communication
4. **Resilience4j** - Circuit breaker & retry logic
5. **gRPC** - Faster internal communication

---

## 🧪 Testing

### Run Unit Tests

```bash
# All tests
mvn test

# Specific service
cd auth-service/auth-service
mvn test

# Specific test class
mvn test -Dtest=UserControllerTest
```

### Run Integration Tests

```bash
mvn verify
```

### Test Coverage

```bash
mvn clean test jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## 🐛 Troubleshooting

### Service Won't Start

**Problem:** `Port already in use`
```
Error: Address already in use
```

**Solution:**
```bash
# Kill process on port (e.g., 8081)
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8081 | xargs kill -9
```

### Database Connection Error

**Problem:** `Communications link failure`

**Solution:**
```bash
# Verify MySQL is running
mysql -u root -p -e "SELECT 1"

# Check application.properties:
# - Database name is correct
# - Username/password are correct
# - MySQL is listening on 3306
```

### Service Cannot Find Other Services

**Problem:** `Connection refused` when calling another service

**Solution:**
- Ensure all services are running on correct ports
- Check firewall settings
- Verify `application.properties` has correct service URLs
- Use `http://localhost:PORT` not `http://127.0.0.1:PORT`

### JWT Token Invalid

**Problem:** `401 Unauthorized` on protected endpoints

**Solution:**
```bash
# Ensure token is included correctly:
curl -H "Authorization: Bearer <TOKEN>"

# Token should start with "Bearer " (with space)
# Check token expiration in JWT payload

# Verify JWT secret is same across services
```

### Hibernate DDL Auto Issues

**Problem:** Tables not created or old schema persists

**Solution:**
```properties
# In application.properties, change:
spring.jpa.hibernate.ddl-auto=create-drop  # Drops & recreates
# OR
spring.jpa.hibernate.ddl-auto=create       # Only creates
# OR use validate for production
spring.jpa.hibernate.ddl-auto=validate    # No changes
```

---

## Author

**Tanuj Sharma**

