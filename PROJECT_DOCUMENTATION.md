# Healthcare Appointment Management System — Project Documentation

---

## 1. Project Overview

A full-stack web application that allows patients to book appointments with doctors, doctors to manage and update appointment statuses, and admins to oversee the entire system. The system is secured with JWT-based authentication and role-based access control.

| Layer     | Technology                          | Port  |
|-----------|-------------------------------------|-------|
| Backend   | Spring Boot 3.2, Java 17, MySQL 8   | 8080  |
| Frontend  | React 18, Redux, Tailwind CSS       | 8081  |

---

## 2. Tech Stack

### Backend
- **Spring Boot 3.2** — REST API framework
- **Spring Security** — Authentication and authorization
- **JWT (jjwt 0.9.1)** — Stateless token-based auth
- **Spring Data JPA + Hibernate** — ORM and database interaction
- **MySQL 8** — Relational database
- **Maven** — Build and dependency management
- **Java 17**

### Frontend
- **React 18** — UI library
- **React Router v6** — Client-side routing
- **Redux Toolkit + Redux Thunk** — State management
- **Axios** — HTTP client for API calls
- **React Hook Form** — Form handling and validation
- **Tailwind CSS** — Utility-first styling
- **React Query** — Server state management

---

## 3. Project Structure

```
final_healthcare_project/
├── springapp/                  # Spring Boot backend
│   ├── src/main/java/com/examly/springapp/
│   │   ├── config/             # Security & CORS config
│   │   ├── controller/         # REST controllers
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── exception/          # Global exception handling
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Spring Data JPA repos
│   │   ├── security/           # JWT filter, util, user details
│   │   └── service/            # Business logic
│   └── src/main/resources/
│       └── application.properties
│
└── reactapp/                   # React frontend
    └── src/
        ├── components/         # Reusable UI components
        ├── context/            # Auth context (global state)
        ├── pages/              # Role-based dashboard pages
        ├── router/             # Protected route logic
        ├── services/           # Axios API service functions
        └── App.js              # Root routing component
```

---

## 4. Database Design

### Table: `users`
| Column           | Type    | Description                          |
|------------------|---------|--------------------------------------|
| id               | Long    | Primary key (auto-increment)         |
| username         | String  | Unique login name                    |
| password         | String  | BCrypt-hashed password               |
| email            | String  | User email                           |
| role             | Enum    | ADMIN / DOCTOR / PATIENT             |
| specialization   | String  | Doctor's specialization              |
| age              | Integer | Patient's age                        |
| gender           | String  | User gender                          |
| experience_years | Integer | Doctor's years of experience         |
| image_url        | String  | Doctor's profile image URL           |
| about            | String  | Doctor's bio/description             |

### Table: `appointment`
| Column           | Type          | Description                          |
|------------------|---------------|--------------------------------------|
| id               | Long          | Primary key                          |
| appointment_date | LocalDateTime | Date and time of appointment         |
| doctor_id        | Long (FK)     | Reference to users table (doctor)    |
| patient_id       | Long (FK)     | Reference to users table (patient)   |
| status           | String        | PENDING / APPROVED / REJECTED        |
| reason           | String        | Reason for the appointment           |

### Table: `notification`
| Column     | Type          | Description                          |
|------------|---------------|--------------------------------------|
| id         | Long          | Primary key                          |
| user_id    | Long          | Target user for the notification     |
| message    | String        | Notification message text            |
| type       | String        | APPOINTMENT / STATUS_UPDATE          |
| is_read    | Boolean       | Read/unread flag                     |
| created_at | LocalDateTime | Timestamp of creation                |

---

## 5. Backend — REST API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint             | Description              | Auth Required |
|--------|----------------------|--------------------------|---------------|
| POST   | `/api/auth/register` | Register a new user      | No            |
| POST   | `/api/auth/login`    | Login and receive JWT    | No            |

**Register Request Body:**
```json
{
  "username": "john_doe",
  "password": "password123",
  "email": "john@example.com",
  "role": "PATIENT",
  "age": 30
}
```

**Login Response:**
```json
{
  "token": "<jwt_token>",
  "role": "PATIENT",
  "id": 1
}
```

---

### Appointments (`/api/appointments`)
| Method | Endpoint                              | Description                    | Auth Required |
|--------|---------------------------------------|--------------------------------|---------------|
| POST   | `/api/appointments/`                  | Book a new appointment         | Yes           |
| GET    | `/api/appointments/doctor/{doctorId}` | Get appointments for a doctor  | Yes           |
| GET    | `/api/appointments/patient/{patientId}` | Get appointments for a patient | Yes         |
| PUT    | `/api/appointments/{id}/status`       | Update appointment status      | Yes           |
| DELETE | `/api/appointments/{id}`              | Delete an appointment          | Yes           |

**Book Appointment Request Body:**
```json
{
  "patientId": 2,
  "doctorId": 3,
  "appointmentDate": "2025-09-15T10:30:00",
  "reason": "Routine checkup"
}
```

---

### Users (`/api/users`)
| Method | Endpoint              | Description              | Auth Required |
|--------|-----------------------|--------------------------|---------------|
| GET    | `/api/users/{id}`     | Get user by ID           | Yes           |
| PUT    | `/api/users/{id}`     | Update user profile      | Yes           |
| DELETE | `/api/users/{id}`     | Delete user (Admin only) | Yes (ADMIN)   |
| GET    | `/api/users/doctors`  | Get all doctors          | Yes           |
| GET    | `/api/users/patients` | Get all patients         | Yes           |

---

### Notifications (`/api/notifications`)
| Method | Endpoint                                  | Description                  | Auth Required |
|--------|-------------------------------------------|------------------------------|---------------|
| GET    | `/api/notifications/user/{userId}`        | Get notifications for a user | Yes           |
| POST   | `/api/notifications/user/{userId}/markAllRead` | Mark all notifications read | Yes          |

---

## 6. Security Implementation

- **JWT Authentication**: Every request (except `/api/auth/**`) requires a valid Bearer token in the `Authorization` header.
- **JwtAuthenticationFilter**: Intercepts all requests, validates the JWT, and sets the security context.
- **JwtUtil**: Handles token generation and validation using a secret key (`SecretKeyForJWTGeneration`).
- **BCryptPasswordEncoder**: All passwords are hashed before storage.
- **Role-based Access**: Admin-only endpoints (e.g., delete user) check the authenticated user's role before proceeding.
- **CORS**: Configured via `WebConfig` to allow requests from the React frontend.

---

## 7. Frontend — Pages & Components

### Pages
| Page               | Route       | Accessible By |
|--------------------|-------------|---------------|
| Login              | `/login`    | Public        |
| Register           | `/register` | Public        |
| Patient Dashboard  | `/patient`  | PATIENT role  |
| Doctor Dashboard   | `/doctor`   | DOCTOR role   |
| Admin Dashboard    | `/admin`    | ADMIN role    |

### Components
| Component           | Description                                              |
|---------------------|----------------------------------------------------------|
| `NavBar.js`         | Top navigation bar with role-aware links and logout      |
| `Footer.js`         | Page footer                                              |
| `HeroBanner.js`     | Landing hero section                                     |
| `WelcomeBanner.js`  | Personalized welcome message after login                 |
| `ServiceHighlights.js` | Highlights key features of the system               |
| `NotificationBell.js` | Bell icon showing unread notification count           |

### Routing
- `ProtectedRoute.js` wraps role-specific routes. If a user tries to access a route they're not authorized for, they are redirected to `/login`.

---

## 8. State Management & Auth Flow

- **AuthContext** (`context/AuthContext.js`) manages global auth state using React Context API.
- On login: JWT token and full user profile are stored in `localStorage`.
- On logout: `localStorage` is cleared and state is reset.
- The `useAuth()` hook provides `{ user, token, login, register, logout }` to any component.

---

## 9. API Service Layer (`services/api.js`)

All backend calls are centralized here using Axios:

| Function                          | Description                          |
|-----------------------------------|--------------------------------------|
| `login(username, password)`       | Authenticate user                    |
| `register(userData)`              | Register new user                    |
| `createAppointment(data, token)`  | Book a new appointment               |
| `getAppointmentsByDoctor(id, token)` | Fetch doctor's appointments       |
| `getAppointmentsByPatient(id, token)` | Fetch patient's appointments     |
| `updateAppointmentStatus(id, status, token)` | Update appointment status |
| `deleteAppointment(id, token)`    | Delete an appointment                |
| `getAllDoctors(token)`            | Fetch all doctors                    |
| `getAllPatients(token)`           | Fetch all patients                   |
| `getUserById(id, token)`          | Fetch user profile by ID             |
| `updateUserById(id, user, token)` | Update user profile                  |

---

## 10. Notification System

- When a patient books an appointment → a notification is sent to the **doctor**.
- When a doctor updates appointment status → a notification is sent to the **patient**.
- Notifications are stored in the `notification` table with `isRead` flag.
- The `NotificationBell` component polls and displays unread count in the navbar.

---

## 11. Role-Based Dashboards

### Patient Dashboard
- View personal profile
- Browse available doctors (with specialization, experience, about)
- Book appointments with a selected doctor
- View own appointment history with status (PENDING / APPROVED / REJECTED)
- Receive notifications on status changes

### Doctor Dashboard
- View personal profile and update it (specialization, experience, about, image)
- View all incoming appointments from patients
- Approve or Reject appointments
- Receive notifications when new appointments are booked

### Admin Dashboard
- View all registered doctors and patients
- Delete users from the system
- Overview of the entire user base

---

## 12. Configuration

### Backend (`application.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/healthcare_db
spring.datasource.username=root
spring.datasource.password=<your_password>
spring.jpa.hibernate.ddl-auto=update
server.port=8080
jwt.secret=SecretKeyForJWTGeneration
```

### Frontend
- API base URL is set to `http://localhost:8080/api` in `services/api.js`
- React app runs on port `8081` (configured via `cross-env PORT=8081` in `package.json`)

---

## 13. How to Run

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+ & npm
- MySQL 8.0+ running locally

### Step 1 — Create Database
```sql
CREATE DATABASE healthcare_db;
```

### Step 2 — Start Backend
```cmd
cd springapp
mvnw.cmd spring-boot:run
```
Backend starts at: `http://localhost:8080`

### Step 3 — Start Frontend
```cmd
cd reactapp
npm install
npm start
```
Frontend starts at: `http://localhost:8081`

---

## 14. Testing

### Backend Tests
Located in `springapp/src/test/java/com/examly/springapp/`:
- `HealthcareAppointmentManagementSystemApplicationTests.java` — Spring context load test
- `AppointmentControllerTest.java` — Appointment API tests
- `DoctorControllerTest.java` — Doctor-related API tests
- `PatientControllerTest.java` — Patient-related API tests

Run with:
```cmd
mvnw.cmd test
```

### Frontend Tests
Located in `reactapp/src/tests/`:
- `AppointmentForm.test.js` — Tests appointment form rendering and submission

Run with:
```cmd
npm test
```

---

## 15. Key Design Decisions

| Decision | Reason |
|----------|--------|
| Single `User` entity for all roles | Simplifies auth; role field differentiates ADMIN/DOCTOR/PATIENT |
| JWT stateless auth | No server-side session needed; scales easily |
| `LocalDateTime` for appointments | Combines date and time in one field |
| Notifications as DB records | Persistent, survives page refresh, supports read/unread tracking |
| React Context for auth state | Lightweight alternative to Redux for auth; avoids over-engineering |
| `spring.jpa.hibernate.ddl-auto=update` | Auto-creates/updates tables on startup; no manual schema needed |

---

*Documentation generated for Healthcare Appointment Management System — Full Stack Project*
