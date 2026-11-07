# 🏛️ KUET University Management System (UMS)
## Comprehensive Architecture, File Structure & Code Logic Guide

> **Welcome!** This guide is designed specifically to explain every file, architectural layer, design pattern, and logic flow in this project in clear, beginner-friendly detail.

---

## 📑 Table of Contents
1. [System Overview & Architecture](#1-system-overview--architecture)
2. [How the Request Lifecycle Works (Data Flow)](#2-how-the-request-lifecycle-works-data-flow)
3. [Complete Project File Directory](#3-complete-project-file-directory)
4. [File-by-File Deep Dive: Logic & Functionality](#4-file-by-file-deep-dive-logic--functionality)
   - [4.1 Application Entry Point](#41-application-entry-point)
   - [4.2 Configuration Layer](#42-configuration-layer)
   - [4.3 Security & JWT Engine](#43-security--jwt-engine)
   - [4.4 Entity Layer (Database Models)](#44-entity-layer-database-models)
   - [4.5 Repository Layer (Spring Data JPA)](#45-repository-layer-spring-data-jpa)
   - [4.6 Service Layer (Business Logic)](#46-service-layer-business-logic)
   - [4.7 Controller Layer (REST API Endpoints)](#47-controller-layer-rest-api-endpoints)
   - [4.8 DTOs & Request/Response Payloads](#48-dtos--requestresponse-payloads)
   - [4.9 Error Handling & Global Exceptions](#49-error-handling--global-exceptions)
   - [4.10 Configuration Files](#410-configuration-files)
   - [4.11 Frontend Web Portal](#411-frontend-web-portal)
   - [4.12 Containerization & DevOps](#412-containerization--devops)
   - [4.13 Automated Testing Suite (Unit Tests & Integration Tests)](#413-automated-testing-suite-unit-tests--integration-tests)
5. [Role-Based Access Control (RBAC) Matrix](#5-role-based-access-control-rbac-matrix)
6. [Pre-Seeded Accounts Directory](#6-pre-seeded-accounts-directory)
7. [How to Run and Test Manually](#7-how-to-run-and-test-manually)
8. [Continuous Integration with GitHub Actions Pipeline](#8-continuous-integration-with-github-actions-pipeline)
9. [Complete Testing Taxonomy: Unit vs Integration Tests](#9-complete-testing-taxonomy-unit-vs-integration-tests)

---

## 1. System Overview & Architecture

The **KUET Management System** is an enterprise-grade university portal built with a multi-layered Spring Boot architecture.

```
┌──────────────────────────────────────────────────────────┐
│                   Frontend Client                        │
│   (HTML5, CSS3 Light Theme, JavaScript ES6 Single-Page)  │
└────────────────────────────┬─────────────────────────────┘
                             │ HTTP Requests (JSON + Bearer JWT)
                             ▼
┌──────────────────────────────────────────────────────────┐
│            Spring Security & Filter Pipeline             │
│    JwtAuthenticationFilter ──▶ SecurityFilterChain       │
└────────────────────────────┬─────────────────────────────┘
                             │ Authenticated Principal
                             ▼
┌──────────────────────────────────────────────────────────┐
│                   Controller Layer                       │
│      AuthController, AdminController, TeacherController, │
│                      StudentController                   │
└────────────────────────────┬─────────────────────────────┘
                             │ DTOs (Data Transfer Objects)
                             ▼
┌──────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│  Validation, BCrypt Hashing, Business Rules & Security   │
└────────────────────────────┬─────────────────────────────┘
                             │ Entity Objects
                             ▼
┌──────────────────────────────────────────────────────────┐
│                   Repository Layer                       │
│             Spring Data JPA Interfaces                   │
└────────────────────────────┬─────────────────────────────┘
                             │ SQL / JDBC
                             ▼
┌──────────────────────────────────────────────────────────┐
│                   Database Engine                        │
│             Embedded In-Memory H2 Database               │
└──────────────────────────────────────────────────────────┘
```

### Why Layers?
In beginner terms, separating code into layers ensures:
- **Cleanliness:** Controllers don't write SQL queries; Repositories don't handle web requests.
- **Maintainability & Simplicity:** Self-contained in-memory database requires zero external database installation, making it instantly runnable.
- **Security:** Security checks happen before the request even reaches your controllers.

---

## 2. How the Request Lifecycle Works (Data Flow)

Here is what happens behind the scenes during a typical user interaction:

### Scenario A: User Logs In
1. The user enters their username and password in the browser and clicks **Sign In**.
2. JavaScript sends a `POST /api/auth/login` request with `{ "username": "admin", "password": "admin123" }`.
3. `AuthController.login()` receives this request and delegates to `AuthService`.
4. `AuthService` passes the credentials to Spring's `AuthenticationManager`.
5. Spring calls `CustomUserDetailsService`, which queries `UserRepository` for the user record.
6. The `PasswordEncoder` (BCrypt) checks if the hashed version of `"admin123"` matches the hash saved in the database.
7. If valid, `JwtTokenProvider` generates a signed **JSON Web Token (JWT)** containing the user's ID, username, and role (`ROLE_ADMIN`).
8. The server responds with `{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", ... }`.
9. The frontend JavaScript stores this token in `localStorage`.

### Scenario B: Accessing a Protected Resource (e.g., `/api/teachers`)
1. The user navigates to the Teachers tab.
2. The browser sends `GET /api/teachers` with the HTTP header:
   ```http
   Authorization: Bearer eyJhbGciOi...
   ```
3. `JwtAuthenticationFilter` intercepts the request before it reaches any controller.
4. The filter extracts the token, verifies the signature using the server's secret key, extracts the user details and role (`ROLE_TEACHER`), and puts a `UserPrincipal` into Spring's `SecurityContextHolder`.
5. The request hits `TeacherController.getAllTeachers()`.
6. The `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")` annotation verifies whether the user possesses either `ROLE_ADMIN` or `ROLE_TEACHER`.
   - If **yes**, `TeacherService.getAllTeachers()` queries the database and returns the list of teachers with HTTP `200 OK`.
   - If **no** (e.g. a Student tried to access `/api/admins`), Spring Security immediately throws an `AccessDeniedException` and returns HTTP `403 Forbidden`.

---

## 3. Complete Project File Directory

```text
kuet-management-system/
├── .github/
│   └── workflows/
│       └── maven.yml                   # GitHub Actions automated CI/CD pipeline
├── compose.yaml                    # Simple Docker Compose containerization (No external DB)
├── Dockerfile                      # Multi-stage production container build
├── documentation.md                # Full system architecture documentation (this file)
├── mvnw & mvnw.cmd                 # Maven wrapper scripts (Windows & Linux)
├── pom.xml                         # Maven dependencies & build configuration
├── README.md                       # Quickstart & evaluation guide
├── src/
│   ├── main/
│   │   ├── java/edu/kuet/ums/
│   │   │   ├── KuetManagementSystemApplication.java  # Main application starter
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java              # Security rules, endpoints & filters
│   │   │   │   └── WebConfig.java                   # CORS settings
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java             # /api/admins endpoints
│   │   │   │   ├── AuthController.java              # /api/auth (login, logout, me)
│   │   │   │   ├── StudentController.java           # /api/students endpoints
│   │   │   │   └── TeacherController.java           # /api/teachers endpoints
│   │   │   ├── entity/
│   │   │   │   ├── AdminProfile.java                # Admin table mapping
│   │   │   │   ├── Role.java                        # Role Enum (ADMIN, TEACHER, STUDENT)
│   │   │   │   ├── StudentProfile.java              # Student table mapping
│   │   │   │   ├── TeacherProfile.java              # Teacher table mapping
│   │   │   │   └── User.java                        # Central credentials & role entity
│   │   │   ├── exception/
│   │   │   │   ├── BadRequestException.java         # 400 custom exception
│   │   │   │   ├── GlobalExceptionHandler.java      # Intercepts errors & formats clean JSON
│   │   │   │   └── ResourceNotFoundException.java   # 404 custom exception
│   │   │   ├── payload/
│   │   │   │   ├── request/                         # Incoming JSON bodies
│   │   │   │   │   ├── AdminCreateRequest.java
│   │   │   │   │   ├── AdminUpdateRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── StudentCreateRequest.java
│   │   │   │   │   ├── StudentUpdateRequest.java
│   │   │   │   │   ├── TeacherCreateRequest.java
│   │   │   │   │   └── TeacherUpdateRequest.java
│   │   │   │   └── response/                        # Outgoing JSON bodies
│   │   │   │       ├── AdminResponse.java
│   │   │   │       ├── ApiResponse.java
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── StudentResponse.java
│   │   │   │       └── TeacherResponse.java
│   │   │   ├── repository/
│   │   │   │   ├── AdminProfileRepository.java      # DB queries for Admins
│   │   │   │   ├── StudentProfileRepository.java    # DB queries for Students
│   │   │   │   ├── TeacherProfileRepository.java    # DB queries for Teachers
│   │   │   │   └── UserRepository.java              # DB queries for Users
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetailsService.java    # Loads user by username for Spring
│   │   │   │   ├── JwtAuthenticationEntryPoint.java # Formats 401 Unauthorized responses
│   │   │   │   ├── JwtAuthenticationFilter.java     # Extracts & verifies JWT on every request
│   │   │   │   ├── JwtTokenProvider.java            # Creates & validates JWT tokens
│   │   │   │   └── UserPrincipal.java               # Spring Security user object
│   │   │   └── service/
│   │   │       ├── AdminService.java                # Admin business logic & validation
│   │   │       ├── AuthService.java                 # Login authentication logic
│   │   │       ├── DataInitializer.java             # Auto-seeds default users on startup
│   │   │       ├── StudentService.java              # Student business logic & validation
│   │   │       └── TeacherService.java              # Teacher business logic & validation
│   │   └── resources/
│   │       ├── application.yml                      # Application settings & profiles
│   │       └── static/                              # Built-in Frontend web files
│   │           ├── index.html                       # Single Page Application UI
│   │           ├── css/styles.css                   # Custom Light Theme stylesheet
│   │           ├── js/app.js                        # Client-side API interactions & UI logic
│   │           └── images/kuet-logo.png             # Official KUET emblem asset
│   └── test/
│       └── java/edu/kuet/ums/
│           ├── KuetManagementSystemApplicationTests.java # Context loading & core E2E suite
│           ├── unit/                                     # Fast, isolated Mockito unit tests (28 tests)
│           │   ├── AdminServiceUnitTest.java             # Admin business rules & validation
│           │   ├── AuthServiceUnitTest.java              # User authentication & token issuance
│           │   ├── JwtTokenProviderUnitTest.java         # Token creation, parsing & expiry
│           │   ├── StudentServiceUnitTest.java           # Student CRUD rules & uniqueness
│           │   └── TeacherServiceUnitTest.java           # Faculty registration & ID checks
│           └── integration/                              # Spring Boot MockMvc integration tests (24 tests)
│               ├── AdminControllerIntegrationTest.java   # Admin endpoints & RBAC verification
│               ├── AuthControllerIntegrationTest.java    # Authentication & token endpoints
│               ├── StudentControllerIntegrationTest.java # Student CRUD & authorization
│               └── TeacherControllerIntegrationTest.java # Teacher CRUD & authorization
```

---

## 4. File-by-File Deep Dive: Logic & Functionality

### 4.1 Application Entry Point
- **File:** `src/main/java/edu/kuet/ums/KuetManagementSystemApplication.java`
- **What it does:** The bootstrap class of the entire application.
- **Code Logic:** Contains the standard Java `main()` method which executes `SpringApplication.run(KuetManagementSystemApplication.class, args)`. The `@SpringBootApplication` annotation enables auto-configuration, component scanning, and property resolution.

---

### 4.2 Configuration Layer
- **File:** `src/main/java/edu/kuet/ums/config/SecurityConfig.java`
  - **What it does:** Configures the Spring Security rules, URL permissions, and filters.
  - **Code Logic:**
    - `@EnableMethodSecurity`: Enables method-level annotations like `@PreAuthorize` across controllers.
    - `SecurityFilterChain`:
      - Disables CSRF (because we are using stateless JWTs rather than session cookies).
      - Configures session management to `SessionCreationPolicy.STATELESS`.
      - Allows public access to `/api/auth/login`, `/api/auth/logout`, static assets (`/`, `/index.html`, `/css/**`, `/js/**`, `/images/**`), and the H2 console (`/h2-console/**`).
      - Requires authentication for all other `/api/**` endpoints.
      - Injects `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
    - `PasswordEncoder`: Returns a `BCryptPasswordEncoder` bean used to securely hash passwords before saving them.

- **File:** `src/main/java/edu/kuet/ums/config/WebConfig.java`
  - **What it does:** Configures CORS (Cross-Origin Resource Sharing).
  - **Code Logic:** Implements `WebMvcConfigurer` to allow web clients from external hosts or ports to make API requests with GET, POST, PUT, DELETE, and OPTIONS headers.

---

### 4.3 Security & JWT Engine
- **File:** `src/main/java/edu/kuet/ums/security/JwtTokenProvider.java`
  - **What it does:** Creates, signs, parses, and validates JSON Web Tokens using the HMAC-SHA secret key.
  - **Code Logic:**
    - `generateToken(Authentication auth)`: Encodes the user's ID, username, and role into a cryptographically signed token with an expiration timestamp (e.g. 24 hours).
    - `getUsernameFromJwt(token)`: Extracts the username from the token's subject.
    - `validateToken(token)`: Confirms the token hasn't expired and hasn't been tampered with.

- **File:** `src/main/java/edu/kuet/ums/security/JwtAuthenticationFilter.java`
  - **What it does:** A per-request filter (`OncePerRequestFilter`) that inspects incoming HTTP requests for a valid bearer token.
  - **Code Logic:**
    - Checks the `Authorization` header for `Bearer <token>`.
    - Validates the token using `JwtTokenProvider`.
    - Loads user permissions and builds an `Authentication` object, then places it into Spring's `SecurityContextHolder`.

- **File:** `src/main/java/edu/kuet/ums/security/JwtAuthenticationEntryPoint.java`
  - **What it does:** Handles unauthenticated access attempts.
  - **Code Logic:** If a request without a valid token attempts to access a protected endpoint, this class catches the rejection and outputs a clean JSON response with HTTP `401 Unauthorized` instead of an HTML error page.

- **File:** `src/main/java/edu/kuet/ums/security/UserPrincipal.java`
  - **What it does:** Implements Spring Security's `UserDetails` interface.
  - **Code Logic:** Bridges our application's `User` entity to Spring Security, holding the username, hashed password, and authority list (`ROLE_ADMIN`, `ROLE_TEACHER`, or `ROLE_STUDENT`).

- **File:** `src/main/java/edu/kuet/ums/security/CustomUserDetailsService.java`
  - **What it does:** Implements Spring's `UserDetailsService`.
  - **Code Logic:** Loads a user from the database via `UserRepository.findByUsernameOrEmail()` during login and builds a `UserPrincipal`.

---

### 4.4 Entity Layer (Database Models)
- **File:** `src/main/java/edu/kuet/ums/entity/Role.java`
  - **What it does:** An Enum defining the exact 3 system roles:
    - `ROLE_ADMIN`
    - `ROLE_TEACHER`
    - `ROLE_STUDENT`

- **File:** `src/main/java/edu/kuet/ums/entity/User.java`
  - **What it does:** The parent account entity containing credentials.
  - **Fields:** `id`, `username` (unique), `email` (unique), `password` (BCrypt hashed), `role` (`Role` enum), `createdAt`.

- **File:** `src/main/java/edu/kuet/ums/entity/AdminProfile.java`
  - **What it does:** Maps the `admin_profiles` table for administrators.
  - **Fields:** `id`, `user` (`@OneToOne` cascade link to `User`), `fullName`, `employeeId` (unique), `department`, `designation`, `phone`.

- **File:** `src/main/java/edu/kuet/ums/entity/TeacherProfile.java`
  - **What it does:** Maps the `teacher_profiles` table for faculty members.
  - **Fields:** `id`, `user` (`@OneToOne` cascade link to `User`), `fullName`, `teacherId` (unique), `department`, `designation`, `phone`, `specialization`.

- **File:** `src/main/java/edu/kuet/ums/entity/StudentProfile.java`
  - **What it does:** Maps the `student_profiles` table for registered students.
  - **Fields:** `id`, `user` (`@OneToOne` cascade link to `User`), `fullName`, `rollNumber` (unique), `department`, `series`, `phone`. *(Note: CGPA has been completely removed).*

---

### 4.5 Repository Layer (Spring Data JPA)
Each interface extends `JpaRepository<Entity, Long>`, giving automatic CRUD operations without writing raw SQL.
- **File:** `src/main/java/edu/kuet/ums/repository/UserRepository.java`
  - Contains custom query helpers: `findByUsername()`, `findByEmail()`, `findByUsernameOrEmail()`, `existsByUsername()`, `existsByEmail()`.
- **File:** `src/main/java/edu/kuet/ums/repository/AdminProfileRepository.java`
  - Helper: `existsByEmployeeId()`, `findByUserId()`.
- **File:** `src/main/java/edu/kuet/ums/repository/TeacherProfileRepository.java`
  - Helper: `existsByTeacherId()`, `findByUserId()`.
- **File:** `src/main/java/edu/kuet/ums/repository/StudentProfileRepository.java`
  - Helper: `existsByRollNumber()`, `findByUserId()`.

---

### 4.6 Service Layer (Business Logic)
- **File:** `src/main/java/edu/kuet/ums/service/AuthService.java`
  - **Logic:** Authenticates credentials using Spring's `AuthenticationManager`, generates a JWT via `JwtTokenProvider`, and returns an `AuthResponse` containing token, username, role, and profile ID.

- **File:** `src/main/java/edu/kuet/ums/service/AdminService.java`
  - **Logic:**
    - `getAllAdmins()`: Fetches all administrator records and converts them to `AdminResponse` DTOs.
    - `getAdminById(id)`: Fetches a single admin or throws `ResourceNotFoundException`.
    - `createAdmin(request)`: Validates that username, email, and employeeId are not duplicates, hashes the password, persists `User` and `AdminProfile`.
    - `updateAdmin(id, request)`: Updates profile information and optionally updates password if a new one is provided.
    - `deleteAdmin(id)`: Deletes the admin profile and cascades to the associated user account.

- **File:** `src/main/java/edu/kuet/ums/service/TeacherService.java`
  - **Logic:** Manages complete CRUD operations for faculty members with validation on email and `teacherId`.

- **File:** `src/main/java/edu/kuet/ums/service/StudentService.java`
  - **Logic:** Manages complete CRUD operations for students with validation on email and `rollNumber`.

- **File:** `src/main/java/edu/kuet/ums/service/DataInitializer.java`
  - **What it does:** Implements Spring Boot's `CommandLineRunner` interface.
  - **Logic:** Runs automatically on application startup. If the database is empty, it seeds 5 Administrators (VC + 4 Deans), 14 Teachers, and 4 Students with pre-hashed passwords using BCrypt.

---

### 4.7 Controller Layer (REST API Endpoints)
Controllers receive HTTP requests, enforce RBAC security, call the corresponding service, and return HTTP status codes.

- **File:** `src/main/java/edu/kuet/ums/controller/AuthController.java`
  - `POST /api/auth/login`: Public login endpoint returning JWT token (`200 OK`).
  - `POST /api/auth/logout`: Stateless logout acknowledging token invalidation (`200 OK`).
  - `GET /api/auth/me`: Returns details of the currently authenticated token owner (`200 OK` or `401 Unauthorized`).

- **File:** `src/main/java/edu/kuet/ums/controller/AdminController.java`
  - Protected with: `@PreAuthorize("hasRole('ADMIN')")`.
  - Endpoints: `GET /api/admins`, `GET /api/admins/{id}`, `POST /api/admins` (`201 Created`), `PUT /api/admins/{id}`, `DELETE /api/admins/{id}`.

- **File:** `src/main/java/edu/kuet/ums/controller/TeacherController.java`
  - Protected with: `@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")`.
  - Endpoints: `GET /api/teachers`, `GET /api/teachers/{id}`, `POST /api/teachers` (`201 Created`), `PUT /api/teachers/{id}`, `DELETE /api/teachers/{id}`.

- **File:** `src/main/java/edu/kuet/ums/controller/StudentController.java`
  - Protected with: `@PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")`.
  - Endpoints: `GET /api/students`, `GET /api/students/{id}`, `POST /api/students` (`201 Created`), `PUT /api/students/{id}`, `DELETE /api/students/{id}`.

---

### 4.8 DTOs & Request/Response Payloads
DTOs (Data Transfer Objects) ensure internal database entities are never exposed directly to external HTTP clients.
- `payload/request/`: Classes representing incoming JSON (`AdminCreateRequest`, `TeacherCreateRequest`, `StudentCreateRequest`, `LoginRequest`). They contain Bean Validation annotations like `@NotBlank`, `@Email`, and `@Size`.
- `payload/response/`: Classes representing outgoing JSON responses (`AdminResponse`, `TeacherResponse`, `StudentResponse`, `AuthResponse`, `ApiResponse`).

---

### 4.9 Error Handling & Global Exceptions
- **File:** `src/main/java/edu/kuet/ums/exception/GlobalExceptionHandler.java`
  - **What it does:** Uses `@RestControllerAdvice` to intercept exceptions across the entire application and return a standardized JSON error structure.
  - **Catches:**
    - `ResourceNotFoundException` -> Returns `404 Not Found`.
    - `BadRequestException` -> Returns `400 Bad Request`.
    - `AccessDeniedException` -> Returns `403 Forbidden`.
    - `BadCredentialsException` -> Returns `401 Unauthorized`.
    - Validation failures (`MethodArgumentNotValidException`) -> Returns `400 Bad Request` with field-specific validation errors.

---

### 4.10 Configuration Files
- **File:** `pom.xml`
  - Defines project dependencies: Spring Boot Starter Web, Spring Boot Starter Security, Spring Boot Starter Data JPA, Embedded H2 Database Driver, `jjwt` (Java JWT library), and Spring Boot Starter Validation.
- **File:** `src/main/resources/application.yml`
  - Unified, single configuration:
    - Server port (`8088`).
    - Embedded in-memory H2 database (`jdbc:h2:mem:kuet_ums`).
    - Web H2 console enabled (`/h2-console`).
    - Stateless JWT secret key and expiration.

---

### 4.11 Frontend Web Portal
- **File:** `src/main/resources/static/index.html`
  - The Single-Page Application interface:
    - **Header:** Official KUET crest, title, system status, user avatar badge.
    - **Quick Role Switcher:** 1-click evaluation buttons (`Admin`, `Teacher`, `Student`).
    - **Sidebar:** Navigation tabs for Overview, Administrators, Teachers, Students, REST API Explorer, and H2 Console.
    - **Views:** Dynamic tables with search filters, action buttons (Add/Edit/Delete), and modals for CRUD forms.
    - **Live REST API Activity Logger:** Real-time log showing every HTTP request, status code, response time, and dark-font JSON payloads.

- **File:** `src/main/resources/static/css/styles.css`
  - **Light Standard Theme:**
    - Institutional color palette: Soft grey background (`#f1f5f9`), crisp white surfaces (`#ffffff`), slate borders (`#e2e8f0` and `#cbd5e1`), and dark typography (`#0f172a`).
    - KUET Identity Colors: KUET Green (`#006a31`) and Gold (`#b45309`) accents for buttons, active tabs, and focus states.
    - Activity Logger styling with `#0f172a` dark monospace font on white cards for readability.

- **File:** `src/main/resources/static/js/app.js`
  - Client-side application controller:
    - Stores the JWT token in `localStorage`.
    - Intercepts all calls with `apiCall()` to automatically attach `Authorization: Bearer <token>`.
    - `quickLoginAs(role)`: Automatically logs in as Admin, Teacher, or Student for instant testing.
    - Dynamically renders tables, handles form submissions, search filtering, and logs requests into the activity logger.

- **File:** `src/main/resources/static/images/kuet-logo.png`
  - Official high-resolution emblem of Khulna University of Engineering & Technology.

---

### 4.12 Containerization & DevOps
- **File:** `Dockerfile`
  - **Multi-Stage Build:**
    - *Stage 1 (Builder):* Uses `eclipse-temurin:21-jdk`, downloads dependencies with layer caching, and builds the production `.jar`.
    - *Stage 2 (Runtime):* Uses lightweight `eclipse-temurin:21-jre-alpine`, creates a non-root system user (`kuet`), configures a health check, and launches `app.jar` on port `8088`.
- **File:** `compose.yaml`
  - Containerizes the Spring Boot application on port `8088` with embedded in-memory database:
    - Runs cleanly with zero external database dependencies.

---

### 4.13 Automated Testing Suite (Unit Tests & Integration Tests)

The testing architecture in KUET Management System is divided strictly into **Unit Tests** and **Integration Tests**, achieving high test coverage (52 tests total), deterministic execution, and fast CI/CD feedback.

#### 4.13.1 Unit Testing Layer (Mockito Isolation — 28 Unit Tests)
Unit tests verify individual components in complete isolation from the Spring framework, web container, and database. External dependencies (like JPA repositories, `PasswordEncoder`, and `AuthenticationManager`) are mocked using Mockito via `@ExtendWith(MockitoExtension.class)`.

- **`StudentServiceUnitTest.java` (8 Tests):**
  - `testGetAllStudents_ReturnsList`: Queries repository and verifies mapping to `StudentResponse` DTOs.
  - `testGetStudentById_Success`: Validates retrieval and response mapping when student exists.
  - `testGetStudentById_NotFound_ThrowsException`: Asserts `ResourceNotFoundException` is thrown when ID does not exist.
  - `testCreateStudent_Success`: Verifies password hashing, entity instantiation, role assignment (`ROLE_STUDENT`), and repository persistence.
  - `testCreateStudent_DuplicateUsername_ThrowsBadRequest`: Asserts `BadRequestException` when username is already taken.
  - `testCreateStudent_DuplicateRollNumber_ThrowsBadRequest`: Asserts `BadRequestException` when roll number is already registered.
  - `testUpdateStudent_Success`: Tests updating mutable profile fields and re-encoding password when provided.
  - `testDeleteStudent_Scenarios`: Verifies repository `delete()` on success and `ResourceNotFoundException` on missing ID.

- **`TeacherServiceUnitTest.java` (6 Tests):**
  - `testGetAllTeachers_ReturnsList`: Verifies fetching all faculty records and DTO mapping.
  - `testGetTeacherById_Success`: Asserts single teacher profile retrieval by ID.
  - `testGetTeacherById_NotFound_ThrowsException`: Ensures `ResourceNotFoundException` when ID does not exist.
  - `testCreateTeacher_Success`: Verifies role assignment (`ROLE_TEACHER`), BCrypt encryption, and profile persistence.
  - `testCreateTeacher_DuplicateTeacherId_ThrowsBadRequest`: Prevents registration with an already used Teacher ID.
  - `testDeleteTeacher_Success`: Ensures profile deletion when teacher exists.

- **`AdminServiceUnitTest.java` (7 Tests):**
  - `testGetAllAdmins_ReturnsList`: Verifies admin list querying and DTO mapping.
  - `testGetAdminById_Success`: Asserts single admin profile retrieval by ID.
  - `testGetAdminById_NotFound_ThrowsException`: Verifies `ResourceNotFoundException` on missing ID.
  - `testCreateAdmin_Success`: Verifies admin account creation with `ROLE_ADMIN` and encoded credentials.
  - `testCreateAdmin_DuplicateEmployeeId_ThrowsBadRequest`: Asserts uniqueness check on employee IDs.
  - `testDeleteAdmin_LastAdmin_ThrowsBadRequest`: Asserts critical business rule preventing deletion of the last remaining admin (`count <= 1`).
  - `testDeleteAdmin_Success`: Confirms deletion proceeds when more than one admin remains in the system (`count > 1`).

- **`JwtTokenProviderUnitTest.java` (5 Tests):**
  - `testGenerateAndValidateToken_Success`: Generates token with custom claims and asserts `validateToken()` returns `true`.
  - `testGetUsernameFromJWT_Success`: Extracts username claim accurately from signed JWT payload.
  - `testValidateToken_MalformedToken_ReturnsFalse`: Catches `MalformedJwtException` and safely returns `false`.
  - `testValidateToken_EmptyOrNullToken_ReturnsFalse`: Rejects empty strings and whitespace safely without throwing exceptions.
  - `testValidateToken_ExpiredToken_ReturnsFalse`: Catches `ExpiredJwtException` on tokens with past timestamp and returns `false`.

- **`AuthServiceUnitTest.java` (2 Tests):**
  - `testAuthenticateUser_StudentSuccess`: Stubs `AuthenticationManager.authenticate()`, generates token, finds user profile, and constructs `AuthResponse`.
  - `testGetCurrentUser_UserNotFound_ThrowsException`: Verifies `BadRequestException` when user ID has no matching DB record.

---

#### 4.13.2 Integration Testing Layer (Spring Boot & MockMvc — 24 Integration Tests)
Integration tests bootstrap the real Spring Application Context (`@SpringBootTest`), Spring Security filter chain, embedded in-memory H2 database, and dispatch simulated HTTP requests via `MockMvc` (`@AutoConfigureMockMvc`).

- **`AuthControllerIntegrationTest.java` (5 Tests):**
  - `testLogin_AdminSuccess`: Dispatches `POST /api/auth/login` with valid credentials; asserts HTTP `200 OK`, non-null JWT token, and `ROLE_ADMIN`.
  - `testLogin_InvalidCredentials`: Dispatches `POST /api/auth/login` with wrong password; asserts HTTP `401 Unauthorized`.
  - `testGetCurrentUser_Unauthenticated`: Asserts `GET /api/auth/me` without Bearer token returns HTTP `401 Unauthorized`.
  - `testLoginAndFetchProfileFlow`: End-to-end integration flow — logs in with username/password, extracts Bearer token, and executes `GET /api/auth/me` with `Authorization: Bearer <token>` to verify authentication.
  - `testLogout`: Dispatches `POST /api/auth/logout`; asserts security context clearing and `200 OK`.

- **`StudentControllerIntegrationTest.java` (4 Tests):**
  - `testAdminFullStudentCrudLifecycle`: Performs full end-to-end lifecycle (`POST` create -> `GET` read -> `PUT` update -> `DELETE` remove -> `GET` verify `404 Not Found`).
  - `testStudentRoleAccessAllowed`: Verifies `@WithMockUser(roles = "STUDENT")` can access `/api/students`.
  - `testTeacherRoleForbiddenFromStudents`: Enforces RBAC rule — `@WithMockUser(roles = "TEACHER")` accessing `/api/students` receives HTTP `403 Forbidden`.
  - `testCreateStudent_InvalidPayload_ReturnsBadRequest`: Verifies Jakarta Validation triggers HTTP `400 Bad Request` when required fields are missing.

- **`TeacherControllerIntegrationTest.java` (3 Tests):**
  - `testAdminFullTeacherCrudLifecycle`: Admin creates, reads, updates, and deletes faculty profiles.
  - `testTeacherRoleCanAccessTeachers`: Verifies teacher role can access teacher directory (`200 OK`).
  - `testStudentRoleForbiddenFromTeachers`: Enforces RBAC rule — student role is blocked with HTTP `403 Forbidden`.

- **`AdminControllerIntegrationTest.java` (4 Tests):**
  - `testAdminRoleCanAccessAdmins`: Verifies admin role has full access to administrator listings (`200 OK`).
  - `testTeacherRoleForbiddenFromAdmins`: Block teachers from viewing or managing administrator profiles (`403 Forbidden`).
  - `testStudentRoleForbiddenFromAdmins`: Block students from viewing or managing administrator profiles (`403 Forbidden`).
  - `testAdminCreateAndDeleteFlow`: Verifies administrator provisioning and deletion while maintaining minimum admin constraint.

- **`KuetManagementSystemApplicationTests.java` (8 Tests):**
  - Context load verification, global security entrypoint checks, unauthenticated 401 response checks across all endpoints, and RBAC matrix validation across Admin, Teacher, and Student roles.

---

## 5. Role-Based Access Control (RBAC) Matrix

| Endpoint | Method | Admin (`ROLE_ADMIN`) | Teacher (`ROLE_TEACHER`) | Student (`ROLE_STUDENT`) | Unauthenticated |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `/api/auth/login` | `POST` | 🟢 Allow (Public) | 🟢 Allow (Public) | 🟢 Allow (Public) | 🟢 Allow |
| `/api/auth/logout` | `POST` | 🟢 Allow | 🟢 Allow | 🟢 Allow | 🟢 Allow |
| `/api/auth/me` | `GET` | 🟢 Allow | 🟢 Allow | 🟢 Allow | 🔴 401 Unauthorized |
| **`/api/admins/**`** | Full CRUD | 🟢 **Full Access** | 🔴 **403 Forbidden** | 🔴 **403 Forbidden** | 🔴 401 Unauthorized |
| **`/api/teachers/**`** | Full CRUD | 🟢 **Full Access** | 🟢 **Full Access** | 🔴 **403 Forbidden** | 🔴 401 Unauthorized |
| **`/api/students/**`** | Full CRUD | 🟢 **Full Access** | 🔴 **403 Forbidden** | 🟢 **Full Access** | 🔴 401 Unauthorized |

---

## 6. Pre-Seeded Accounts Directory

### 👑 1. Administrators (`ROLE_ADMIN`)
| Username | Password | Full Name | Designation | Faculty / Department | Email | Phone |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **Professor Dr. Mohammad Mashud** | Vice-Chancellor & Chief Administrator | VC Office | `admin@kuet.ac.bd` | `+880-41-769468` |
| `admin_mahbub` | `admin123` | **Dr. Khondoker Mahbub Hassan** | Dean | Faculty of Civil Engineering | `mahbub@ce.kuet.ac.bd` | `+880-1711-201001` |
| `admin_rokibul` | `admin123` | **Dr. Kazi Md. Rokibul Alam** | Dean | Faculty of Electrical and Electronic Engineering | `rokibul@eee.kuet.ac.bd` | `+880-1711-201002` |
| `admin_sultan` | `admin123` | **Dr. Mohammad Sultan Mahmud** | Dean | Faculty of Mechanical Engineering | `sultan@me.kuet.ac.bd` | `+880-1711-201003` |
| `admin_yousuf` | `admin123` | **Dr. Mohammad Abu Yousuf** | Dean | Faculty of Science and Humanities | `yousuf@hum.kuet.ac.bd` | `+880-1711-201004` |

### 👨‍🏫 2. Faculty & Teachers (`ROLE_TEACHER`)
| Username | Password | Full Name | Designation | Department | Email |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `teacher_mahmud` | `teacher123` | **Prof. Dr. Al-Mahmud** | Professor & Head | CSE | `al-mahmud@cse.kuet.ac.bd` |
| `teacher_shibbir` | `teacher123` | **Dr. Shibbir Ahmed** | Associate Professor | EEE | `shibbir@eee.kuet.ac.bd` |
| `teacher_saeed` | `teacher123` | **Kazi Saeed Alam** | Assistant Professor | CSE | `saeed.alam@cse.kuet.ac.bd` |
| `teacher_repon` | `teacher123` | **Md. Repon Islam** | Assistant Professor | CSE | `repon@cse.kuet.ac.bd` |
| `teacher_sakhawat` | `teacher123` | **Md. Sakhawat Hossain** | Assistant Professor | CSE | `sakhawat@cse.kuet.ac.bd` |
| `teacher_shawon` | `teacher123` | **Md Nazirulhasan Shawon** | Assistant Professor | CSE | `shawon@cse.kuet.ac.bd` |
| `teacher_isha` | `teacher123` | **Most. Kaniz Fatema Isha** | Lecturer | CSE | `isha@cse.kuet.ac.bd` |
| `teacher_tajmilur` | `teacher123` | **Md Tajmilur Rahman** | Lecturer | CSE | `tajmilur@cse.kuet.ac.bd` |
| `teacher_nfs` | `teacher123` | **Nabil Faiyaz Sadi** | Lecturer | CSE | `nfs@cse.kuet.ac.bd` |
| `teacher_nawar` | `teacher123` | **Subah Nawar** | Lecturer | CSE | `Nawar@cse.kuet.ac.bd` |
| `teacher_waliul` | `teacher123` | **Waliul Islam Sumon** | Lecturer | CSE | `waliul@cse.kuet.ac.bd` |
| `teacher_man` | `teacher123` | **Md. Mubtashim Abrar Nihal** | Lecturer | CSE | `man@cse.kuet.ac.bd` |
| `teacher_deya` | `teacher123` | **Lamisa Bintee Mizan Deya** | Lecturer | CSE | `deya@cse.kuet.ac.bd` |
| `teacher_sadia` | `teacher123` | **Mst. Sadia Islam** | Lecturer | CSE | `sadia@cse.kuet.ac.bd` |

### 🎓 3. Registered Students (`ROLE_STUDENT`)
| Username | Password | Full Name | Roll Number | Department | Series | Institutional Email |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `student_mesba` | `student123` | **Mesbaul Islam** | `2207075` | Computer Science & Engineering | 2K22 | `mesbaul.2207075@stud.kuet.ac.bd` |
| `student_evon` | `student123` | **Ahmed Evon** | `2207074` | Computer Science & Engineering | 2K22 | `evon.2207074@stud.kuet.ac.bd` |
| `student_safiul` | `student123` | **Safiul Islam** | `2207068` | Computer Science & Engineering | 2K22 | `safiul.2207068@stud.kuet.ac.bd` |
| `student_torikul` | `student123` | **Torikul Islam** | `2207078` | Computer Science & Engineering | 2K22 | `torikul.2207078@stud.kuet.ac.bd` |

---

## 7. How to Run and Test Manually

### Running Locally with Java
1. Open terminal in the project directory:
   ```powershell
   java -jar target\kuet-management-system-1.0.0.jar
   ```
2. Or use Maven wrapper:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
3. Open in browser: [http://localhost:8088/](http://localhost:8088/)
4. H2 In-Memory DB Console: [http://localhost:8088/h2-console](http://localhost:8088/h2-console)
   - JDBC URL: `jdbc:h2:mem:kuet_ums`
   - User: `sa`
   - Password: *(leave blank)*

### Running with Docker Compose
```bash
docker compose up --build
```
To stop:
```bash
docker compose down
```

---

## 8. Continuous Integration with GitHub Actions Pipeline

Continuous Integration (CI) guarantees that every code modification pushed to the repository is automatically checked out, compiled, and validated through the automated test suite before merging or deployment.

### 8.1 Workflow Architecture (`.github/workflows/maven.yml`)

```text
┌────────────────────────────────────────────────────────┐
│               GitHub Repository Event                  │
│       • Push to main branch                            │
│       • Pull Request to main branch                    │
│       • Manual trigger via workflow_dispatch           │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│            Ubuntu Runner (ubuntu-latest)               │
│                                                        │
│  1. 📥 actions/checkout@v4                             │
│     Downloads codebase into runner workspace           │
│                                                        │
│  2. ☕ actions/setup-java@v4                           │
│     Configures Eclipse Temurin JDK 21 & Maven cache   │
│                                                        │
│  3. 🛡️ chmod +x ./mvnw                                 │
│     Guarantees executable bit on wrapper script        │
│                                                        │
│  4. 🧪 ./mvnw clean test --no-transfer-progress        │
│     Executes 52 Unit and Integration Tests             │
│                                                        │
│  5. 📦 ./mvnw package -DskipTests                      │
│     Compiles production executable JAR artifact        │
│                                                        │
│  6. 📊 actions/upload-artifact@v4                      │
│     Archives Surefire XML test reports for 7 days      │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│            CI Status: ✅ SUCCESS (Green Badge)         │
└────────────────────────────────────────────────────────┘
```

### 8.2 Detailed Step-by-Step Instructions to Pass Tests on GitHub Actions

Follow these exact steps to ensure your GitHub Actions pipeline triggers and succeeds:

#### Step 1: Commit and Push Code to GitHub
Ensure all test files, source files, and the `.github/workflows/maven.yml` file are staged, committed, and pushed:
```bash
git add .
git commit -m "Add Unit Tests, Integration Tests, and GitHub Actions CI workflow"
git push origin main
```

#### Step 2: Navigate to GitHub Actions in Your Browser
1. Open your repository on GitHub: [https://github.com/mesbaul2022/KUETManagementSystem](https://github.com/mesbaul2022/KUETManagementSystem)
2. Click on the **Actions** tab in the top navigation bar.

#### Step 3: Monitor the Automated Build Execution
1. You will see a newly triggered workflow titled **"Java CI with Maven (KUET Management System)"**.
2. Click on the active workflow run to enter the summary view.
3. In the left panel, click on **"Build & Run Automated Test Suite"** to view live, color-coded console logs.

#### Step 4: Verify Test Execution Steps
Inside the log viewer, observe each step succeeding:
- **`📥 Checkout Repository Code`**: Clones repository at HEAD commit.
- **`☕ Set up JDK 21 (Eclipse Temurin)`**: Sets up the Java 21 runtime and warms Maven dependency cache.
- **`🛡️ Ensure Maven Wrapper Permissions`**: Runs `chmod +x ./mvnw` to eliminate permission errors.
- **`🧪 Execute Unit & Integration Tests`**: Executes Surefire plugin:
  ```text
  [INFO] Running Unit Tests: AdminService Business Logic
  [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Unit Tests: AuthService Authentication Logic
  [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Unit Tests: JwtTokenProvider Engine
  [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Unit Tests: StudentService Business Logic
  [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Unit Tests: TeacherService Business Logic
  [INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Integration Tests: AdminController REST Endpoints & RBAC Enforcement
  [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Integration Tests: AuthController Endpoints & JWT Pipeline
  [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Integration Tests: StudentController REST Endpoints & RBAC
  [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running Integration Tests: TeacherController REST Endpoints & RBAC
  [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
  [INFO] Running edu.kuet.ums.KuetManagementSystemApplicationTests
  [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
  [INFO] ------------------------------------------------------------------------
  [INFO] Tests run: 52, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```
- **`📦 Build Executable JAR Package`**: Generates `target/kuet-management-system-1.0.0.jar`.
- **`📊 Publish Test Reports Artifact`**: Archives surefire test XML summaries.

#### Step 5: Manual Triggering (On-Demand Execution)
If you want to trigger the pipeline manually at any time without pushing new code:
1. Go to the **Actions** tab on GitHub.
2. Select **"Java CI with Maven (KUET Management System)"** in the left sidebar under *Workflows*.
3. Click the **"Run workflow"** dropdown button on the right.
4. Select the **`main`** branch and click the green **"Run workflow"** button.

#### Step 6: Downloading Test Reports
1. In the workflow run summary, scroll down to the **Artifacts** section.
2. Click **`surefire-test-reports`** to download a ZIP archive containing individual XML reports for each test class for auditing or compliance.

---

### 8.3 Troubleshooting CI/CD Failures (Key Pitfalls & Solutions)

| Potential Failure Point | Root Cause | Built-in Resolution in this Project |
| :--- | :--- | :--- |
| `Permission denied: ./mvnw` | Git on Windows strips Linux executable bits (`+x`). | Workflow includes explicit `run: chmod +x ./mvnw` step, and `git update-index --chmod=+x mvnw` is recorded in git index. |
| `Unsupported class file major version` | Runner Java version does not match compiled bytecode. | Workflow fixes JDK version to `temurin 21`, aligning with `Dockerfile` and local runtime. |
| Port Collision (`8088 in use`) | Multiple tests trying to bind to localhost network ports. | Integration tests use Spring's `MockMvc` mock servlet container, executing inside memory without opening TCP sockets. |
| Database Unavailable | Runner lacks MySQL/Postgres service containers. | Application uses self-contained in-memory H2 database (`jdbc:h2:mem:kuet_ums`), zero network dependencies. |

---

## 9. Complete Testing Taxonomy: Unit vs Integration Tests

Understanding the distinction between testing tiers is fundamental to software reliability and enterprise architecture.

### 9.1 Comparison Matrix

| Dimension | 🔬 Unit Tests (Service / Security) | 🌐 Integration Tests (Controller / Spring MVC) |
| :--- | :--- | :--- |
| **Primary Scope** | A single class or method in complete isolation. | Cross-cutting collaboration across Filters, Controllers, Services, and DB. |
| **Spring Context** | ❌ **No Spring Context** loaded. Runs as pure JUnit 5 test. | 🟢 **Full Spring ApplicationContext** loaded (`@SpringBootTest`). |
| **Database** | ❌ **No Database**. Repositories are mocked via Mockito. | 🟢 **Real Embedded Database**. Runs queries against in-memory H2. |
| **Execution Speed** | ⚡ Extremely fast (~10–50 milliseconds per class). | ⏱️ Moderate (~1–4 seconds to bootstrap Spring container). |
| **HTTP Dispatcher** | ❌ None. Direct Java method invocation. | 🟢 Simulated via `MockMvc` (Headers, JSON serialization, URL routes). |
| **Security Layer** | ❌ Spring Security bypassed or manually stubbed. | 🟢 Full filter chain tested (`JwtAuthenticationFilter`, `@PreAuthorize`). |
| **Failure Indication** | Indicates a specific logical bug in domain code. | Indicates routing, JSON parsing, database constraint, or authorization failure. |

---

### 9.2 Deep Dive: Unit Testing Methodology
Unit tests use **Mockito** to eliminate external variables:
1. **`@Mock`**: Creates dummy proxy implementations of classes (e.g. `UserRepository`, `PasswordEncoder`).
2. **`@InjectMocks`**: Creates the real instance of the service under test (`StudentService`) and injects the mocks into its constructor.
3. **`when(...).thenReturn(...)`**: Defines predictable return values for mock methods (stubbing).
4. **`verify(mock, times(1)).method(...)`**: Verifies that a specific method was actually invoked with the expected parameters.
5. **`assertThrows(...)`**: Validates that invalid operations raise the appropriate custom exception (`BadRequestException`, `ResourceNotFoundException`).

### 9.3 Deep Dive: Integration Testing Methodology
Integration tests use **Spring Boot Test + MockMvc**:
1. **`@SpringBootTest`**: Discovers `@SpringBootConfiguration` and spins up the whole application environment.
2. **`@AutoConfigureMockMvc`**: Configures the `MockMvc` engine to simulate incoming HTTP calls without requiring a running web server.
3. **`@WithMockUser`**: Injects a fake `SecurityContext` with specific roles (`ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`) to test authorization boundaries.
4. **`jsonPath("$.field", is("value"))`**: Inspects outgoing JSON payloads and verifies property names and types.

---

### 9.4 How to Run Tests Locally

Execute from project root directory:

```powershell
# 1. Run all 52 unit & integration tests
.\mvnw.cmd test

# 2. Run ONLY unit tests (Fast feedback: < 3 seconds)
.\mvnw.cmd test -Dtest="*UnitTest"

# 3. Run ONLY integration tests
.\mvnw.cmd test -Dtest="*IntegrationTest"

# 4. Run a specific test class
.\mvnw.cmd test -Dtest="StudentServiceUnitTest"

# 5. Linux / macOS equivalent
./mvnw test
```
