# 🏛️ KUET University Management System (UMS)

> A modern, secure, and containerized University Management System web application for **Khulna University of Engineering & Technology (KUET)**, built using **Java Spring Boot**, **Spring Security (RBAC + JWT)**, **PostgreSQL / H2**, and **Docker Compose**.

---

## 📌 Executive Summary & Requirements Fulfillment

| Requirement | Implementation Detail | Status |
| :--- | :--- | :---: |
| **REST API** | Full CRUD operations for `Admin`, `Teacher`, and `Student` resources with standard REST conventions, JSON responses, DTO validation, and proper HTTP status codes (`200`, `201`, `204`, `400`, `401`, `403`, `404`). | ✅ Complete |
| **Spring Security & RBAC** | Stateless JWT authentication, login/logout, BCrypt password hashing, method-level `@PreAuthorize`, and strict Role-Based Access Control (RBAC). | ✅ Complete |
| **Role Permissions** | **Admins** manage all 3 resources (`Admins`, `Teachers`, `Students`).<br>**Teachers** manage only `Teachers` (restricted from `Admins` and `Students`).<br>**Students** manage only `Students` (restricted from `Admins` and `Teachers`). | ✅ Complete |
| **Dockerization** | Multi-stage production `Dockerfile` (eclipse-temurin 21) + `compose.yaml` orchestrating Spring Boot and PostgreSQL with health-checked networking. | ✅ Complete |
| **Interactive Web Application** | Premium KUET-branded portal with dark mode, quick 1-click role switcher, responsive CRUD tables, modals, and a live REST API activity logger. | ✅ Complete |

---

## 🔐 Role-Based Access Control (RBAC) Matrix

| Resource Endpoint | HTTP Methods | Admin (`ROLE_ADMIN`) | Teacher (`ROLE_TEACHER`) | Student (`ROLE_STUDENT`) | Unauthenticated |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `/api/auth/login` | `POST` | 🟢 Allow (Public) | 🟢 Allow (Public) | 🟢 Allow (Public) | 🟢 Allow |
| `/api/auth/logout` | `POST` | 🟢 Allow | 🟢 Allow | 🟢 Allow | 🟢 Allow |
| `/api/auth/me` | `GET` | 🟢 Allow | 🟢 Allow | 🟢 Allow | 🔴 401 Unauthorized |
| **`/api/admins/**`** | `GET`, `POST`, `PUT`, `DELETE` | 🟢 **Full Access** | 🔴 **403 Forbidden** | 🔴 **403 Forbidden** | 🔴 401 Unauthorized |
| **`/api/teachers/**`** | `GET`, `POST`, `PUT`, `DELETE` | 🟢 **Full Access** | 🟢 **Full Access** | 🔴 **403 Forbidden** | 🔴 401 Unauthorized |
| **`/api/students/**`** | `GET`, `POST`, `PUT`, `DELETE` | 🟢 **Full Access** | 🔴 **403 Forbidden** | 🟢 **Full Access** | 🔴 401 Unauthorized |

---

## 🔑 Pre-Seeded Evaluation Accounts

The application automatically seeds the database on startup with the following accounts for immediate testing:

| Role | Username | Password | Full Name | Department / Affiliation |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Prof. Dr. Mihir Ranjan Halder | Office of the Vice-Chancellor |
| **Teacher** | `teacher_rokib` | `teacher123` | Prof. Dr. Kazi Md. Rokibul Hasan | Computer Science & Engineering |
| **Teacher** | `teacher_shibbir` | `teacher123` | Dr. Shibbir Ahmed | Electrical & Electronic Engineering |
| **Student** | `student_tanvir` | `student123` | Tanvir Ahmed (Roll: 2007001) | Computer Science & Engineering (2K20) |
| **Student** | `student_nusrat` | `student123` | Nusrat Jahan (Roll: 2107042) | Computer Science & Engineering (2K21) |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17, 21, or 25** installed
- **Docker & Docker Compose** (for containerized execution)

---

### Option 1: Run Locally (Standalone / Dev Profile)

The application includes an embedded Maven wrapper (`./mvnw` or `mvnw.cmd`) and runs with an in-memory **H2 database** by default.

1. **Navigate to the project directory:**
   ```bash
   cd "C:\Users\Mesbaul Islam\.gemini\antigravity-ide\scratch\kuet-management-system"
   ```

2. **Run with Maven Wrapper:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   *Or launch the pre-compiled JAR:*
   ```powershell
   java -jar target\kuet-management-system-1.0.0.jar
   ```

3. **Open the Web Portal in your browser:**
   ```text
   http://localhost:8088/
   ```

4. **Access the H2 Database Console:**
   ```text
   http://localhost:8088/h2-console
   JDBC URL: jdbc:h2:mem:kuet_ums
   User: sa
   Password: (empty)
   ```

---

### Option 2: Run with Docker Compose (PostgreSQL + App)

To run the application and a production PostgreSQL database together:

1. **Start the containers:**
   ```bash
   docker compose up --build
   ```

2. **Stop the containers:**
   ```bash
   docker compose down
   ```

---

## 🧪 Running Automated Tests

Run the comprehensive integration test suite verifying authentication, BCrypt hashing, RBAC permissions, and CRUD lifecycles:

```powershell
.\mvnw.cmd test
```

---

## 📡 REST API Reference

### 1. Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```
**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "email": "admin@kuet.ac.bd",
  "role": "ROLE_ADMIN",
  "fullName": "Prof. Dr. Mihir Ranjan Halder",
  "profileId": 1
}
```

#### Logout
```http
POST /api/auth/logout
```
**Response (200 OK):**
```json
{
  "success": true,
  "message": "User logged out successfully. Token cleared.",
  "data": null,
  "timestamp": "2026-10-23T18:00:00"
}
```

#### Current User Profile
```http
GET /api/auth/me
Authorization: Bearer <JWT_TOKEN>
```

---

### 2. Administrator Resource (`/api/admins`) — Restricted to `ROLE_ADMIN`

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/admins` | List all administrators | `200 OK` |
| `GET` | `/api/admins/{id}` | Get administrator by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/admins` | Create a new administrator | `201 Created` |
| `PUT` | `/api/admins/{id}` | Update existing administrator | `200 OK` |
| `DELETE` | `/api/admins/{id}` | Delete administrator | `200 OK` |

**Sample Create Request (`POST /api/admins`):**
```json
{
  "username": "admin_kamal",
  "email": "kamal@kuet.ac.bd",
  "password": "Password123!",
  "fullName": "Dr. Md. Kamal Hossain",
  "employeeId": "KUET-ADM-010",
  "department": "Office of the Dean (Faculty of CE)",
  "designation": "Dean",
  "phone": "+880-1711-888999"
}
```

---

### 3. Faculty / Teacher Resource (`/api/teachers`) — `ROLE_ADMIN` & `ROLE_TEACHER`

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/teachers` | List all faculty members | `200 OK` |
| `GET` | `/api/teachers/{id}` | Get teacher by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/teachers` | Add a new faculty member | `201 Created` |
| `PUT` | `/api/teachers/{id}` | Update faculty member | `200 OK` |
| `DELETE` | `/api/teachers/{id}` | Delete teacher record | `200 OK` |

**Sample Create Request (`POST /api/teachers`):**
```json
{
  "username": "teacher_amin",
  "email": "amin@ece.kuet.ac.bd",
  "password": "Password123!",
  "fullName": "Dr. Md. Ruhul Amin",
  "teacherId": "KUET-TCH-042",
  "department": "Electronics and Communication Engineering",
  "designation": "Assistant Professor",
  "specialization": "VLSI Design & Embedded Systems",
  "phone": "+880-1712-112233"
}
```

---

### 4. Student Resource (`/api/students`) — `ROLE_ADMIN` & `ROLE_STUDENT`

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/students` | List all students | `200 OK` |
| `GET` | `/api/students/{id}` | Get student by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/students` | Register a new student | `201 Created` |
| `PUT` | `/api/students/{id}` | Update student record | `200 OK` |
| `DELETE` | `/api/students/{id}` | Delete student record | `200 OK` |

**Sample Create Request (`POST /api/students`):**
```json
{
  "username": "student_sadia",
  "email": "sadia.2207010@stud.kuet.ac.bd",
  "password": "Password123!",
  "fullName": "Sadia Afrin",
  "rollNumber": "2207010",
  "department": "Computer Science & Engineering",
  "series": "2K22",
  "cgpa": 3.92,
  "phone": "+880-1811-998877"
}
```

---

## 📂 Project Architecture

```
kuet-management-system/
├── Dockerfile                         # Production multi-stage Dockerfile (Temurin 21)
├── compose.yaml                       # Docker Compose specification (App + PostgreSQL)
├── pom.xml                            # Maven dependencies & build configuration
├── README.md                          # Project documentation
└── src/
    ├── main/
    │   ├── java/edu/kuet/ums/
    │   │   ├── KuetManagementSystemApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java         # Spring Security 6+ filter chains & RBAC
    │   │   │   └── WebConfig.java              # Jackson ObjectMapper & CORS config
    │   │   ├── controller/
    │   │   │   ├── AdminController.java        # CRUD for /api/admins
    │   │   │   ├── AuthController.java         # /api/auth (login, logout, me)
    │   │   │   ├── StudentController.java      # CRUD for /api/students
    │   │   │   └── TeacherController.java      # CRUD for /api/teachers
    │   │   ├── entity/
    │   │   │   ├── AdminProfile.java           # Admin domain model
    │   │   │   ├── Role.java                   # Enum (ROLE_ADMIN, ROLE_TEACHER, ROLE_STUDENT)
    │   │   │   ├── StudentProfile.java         # Student domain model
    │   │   │   ├── TeacherProfile.java         # Teacher domain model
    │   │   │   └── User.java                   # Core authentication credentials & role
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── GlobalExceptionHandler.java # Standardized JSON error handler
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── payload/
    │   │   │   ├── request/                    # Strongly-typed DTOs with Jakarta validation
    │   │   │   └── response/                   # Normalized JSON responses
    │   │   ├── repository/                     # Spring Data JPA repositories
    │   │   ├── security/
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAuthenticationEntryPoint.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   └── UserPrincipal.java
    │   │   └── service/
    │   │       ├── AdminService.java
    │   │       ├── AuthService.java
    │   │       ├── DataInitializer.java        # Pre-seeds KUET demo accounts
    │   │       ├── StudentService.java
    │   │       └── TeacherService.java
    │   └── resources/
    │       ├── application.yml                 # Configuration (H2 dev & PostgreSQL docker)
    │       └── static/
    │           ├── css/styles.css              # Custom styling & KUET identity
    │           ├── js/app.js                   # Client logic, JWT handling & live logger
    │           └── index.html                  # Responsive Single-Page Application
    └── test/
        └── java/edu/kuet/ums/
            └── KuetManagementSystemApplicationTests.java  # Comprehensive RBAC & CRUD test suite
```
