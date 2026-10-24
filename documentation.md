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
   - [4.13 Automated Integration Tests](#413-automated-integration-tests)
5. [Role-Based Access Control (RBAC) Matrix](#5-role-based-access-control-rbac-matrix)
6. [Pre-Seeded Accounts Directory](#6-pre-seeded-accounts-directory)
7. [How to Run and Test Manually](#7-how-to-run-and-test-manually)

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
│          H2 (In-Memory Dev) or PostgreSQL (Docker)       │
└──────────────────────────────────────────────────────────┘
```

### Why Layers?
In beginner terms, separating code into layers ensures:
- **Cleanliness:** Controllers don't write SQL queries; Repositories don't handle web requests.
- **Maintainability:** You can change the database engine from H2 to PostgreSQL without changing a single line of business logic or UI code.
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

The project is organized into three main areas: root-level build and deployment files, the Spring Boot application under `src/main`, and automated tests under `src/test`. The repository explorer screenshot also shows `.github`, `.vscode`, `HELP.md`, and `target`; these can be local workspace or generated folders and may be ignored by Git rather than included in a clone.

```text
kuet-management-system/
├── .github/                        # Optional local automation or workspace metadata
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties # Maven Wrapper distribution settings
├── .env.example                     # Template for local environment variables
├── .vscode/
│   └── tasks.json                  # Optional VS Code run task (workspace-local)
├── target/                         # Generated Maven output; ignored by Git
├── compose.yaml                    # Docker Compose orchestration (App + PostgreSQL)
├── Dockerfile                      # Multi-stage production container build
├── documentation.md                # Full system architecture documentation (this file)
├── HELP.md                         # Optional generated Spring Boot help file
├── .gitignore                      # Excludes build output and local IDE files
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
│           └── KuetManagementSystemApplicationTests.java # Automated MockMvc integration tests
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
  - Defines the project dependencies and targets Java 25, including Spring Boot web, security, and data JPA, H2 and PostgreSQL drivers, `jjwt` (Java JWT library), and validation.
- **File:** `src/main/resources/application.yml`
  - Configures server port (`8088`), active profile (`dev`), and a Base64-encoded 32-byte JWT signing key from the required `APP_JWT_SECRET` environment variable, plus separate profiles:
    - `dev`: In-memory H2 database (`jdbc:h2:mem:kuet_ums`) and web H2 console enabled.
    - `docker`: PostgreSQL connection (`jdbc:postgresql://db:5432/kuet_ums`).

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
    - *Stage 1 (Builder):* Uses `eclipse-temurin:25-jdk`, downloads dependencies with layer caching, and builds the production `.jar`.
    - *Stage 2 (Runtime):* Uses lightweight `eclipse-temurin:25-jre-alpine`, creates a non-root system user (`kuet`), configures a health check, and launches `app.jar` on port `8088`.
- **File:** `compose.yaml`
  - Orchestrates two containers on an internal network (`kuet-network`):
    - `app`: Spring Boot application connected to the database with active profile `docker`.
    - `db`: PostgreSQL 16 container with health checks and persistent volume storage (`kuet_pgdata`).

---

### 4.13 Automated Integration Tests
- **File:** `src/test/java/edu/kuet/ums/KuetManagementSystemApplicationTests.java`
  - Automated integration tests verifying:
    1. Unauthenticated requests receive `401 Unauthorized`.
    2. Admin has full access to all three resources (`/api/admins`, `/api/teachers`, `/api/students`).
    3. Teacher can access `/api/teachers`, but receives `403 Forbidden` on `/api/admins` and `/api/students`.
    4. Student can access `/api/students`, but receives `403 Forbidden` on `/api/admins` and `/api/teachers`.
    5. Complete CRUD lifecycle test (creating and reading a student).

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
1. Open PowerShell in the project directory (the folder containing `pom.xml`):
   ```powershell
   cd "C:\path\to\kuet-management-system"
   ```
2. Generate a random signing key for this PowerShell session:
   ```powershell
   $secretBytes = New-Object byte[] 32
   $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
   $rng.GetBytes($secretBytes)
   $env:APP_JWT_SECRET = [Convert]::ToBase64String($secretBytes)
   ```
3. Run the application with the Maven Wrapper. A separate Maven installation is not required:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Open in browser: [http://localhost:8088/](http://localhost:8088/)
5. H2 In-Memory DB Console: [http://localhost:8088/h2-console](http://localhost:8088/h2-console)
   - JDBC URL: `jdbc:h2:mem:kuet_ums`
   - User: `sa`
   - Password: *(leave blank)*

### Running with Docker Compose
Copy `.env.example` to `.env`, replace both placeholder values with strong values, and keep `.env` private. Docker Compose reads the environment values from `.env`.

```powershell
copy .env.example .env
docker compose up --build
```
To stop:
```powershell
docker compose down
```
