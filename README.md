# HealthQueue

> **Intelligent, real-time healthcare queue management and clinic discovery platform.**

HealthQueue bridges the gap between patients and healthcare providers by eliminating waiting room congestion. Patients can discover nearby clinics via GPS, reserve queue tickets remotely, and track their position in real time. Clinics and hospital networks gain a live operations console to manage appointments, branches, doctors, and visit records seamlessly.

---

## 🏥 Architecture Overview

HealthQueue is built as a modern full-stack application structured into three decoupled layers:

```
┌────────────────────────────────────────────────────────┐
│                   React 19 Frontend                    │
│      (Vite · TypeScript · Tailwind CSS v4 · Zustand)   │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP / JSON API (Port 3000)
┌───────────────────────────▼────────────────────────────┐
│                  Java Backend Service                  │
│       (Java 21 · Spark Framework · JOOQ · JWT)         │
└───────────────────────────┬────────────────────────────┘
                            │ JDBC / SQL
┌───────────────────────────▼────────────────────────────┐
│                 PostgreSQL Database                    │
│             (PostGIS Spatial Indexing)                 │
└────────────────────────────────────────────────────────┘
```

---

## ✨ Key Features

### For Patients
- 📍 **GPS Proximity Search**: Auto-detects coordinates and ranks partner clinics by real-time distance using PostGIS spatial indexing.
- 🎟️ **3-Step Booking Wizard**: Select clinic branch, choose available doctor specialist, and reserve a queue ticket in seconds.
- ⏱️ **Live Queue Position Tracker**: Real-time ticker showing current queue position, estimated wait time, and arrival alerts.
- 📋 **Medical History & Prescriptions**: Access past consultation logs, doctor notes, and prescription details.
- 👤 **Personalized Profile**: Patient name and initials avatar integration.

### For Healthcare Organizations & Clinics
- 📊 **Executive Overview Dashboard**: Live metrics for active patient tickets, on-duty doctors, and department wait times.
- 📋 **Live Queue Console**: Filter tickets by status (`IN_QUEUE`, `IN_PROGRESS`, `SCHEDULED`, `COMPLETED`), call next patients, and reassign specialists.
- 🏢 **Multi-Branch Management**: Register and configure physical clinic branches with GPS coordinates.
- 👨‍⚕️ **Doctor Directory**: Manage doctor availability, specialties, and branch assignments.
- 🔒 **Role-Based Authentication**: Secure JWT-based HttpOnly cookie authentication and session rotation.

---

## 🛠️ Tech Stack

| Layer | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | React 19, TypeScript, Vite | Fast, responsive single-page application |
| **Styling** | Tailwind CSS v4, Lucide Icons | Custom "Calm Clinical" light design system (`#00685b`) |
| **State & Cache** | Zustand, TanStack React Query | Persistent auth store and optimistic caching |
| **Backend** | Java 21, Spark Framework | High-performance RESTful micro-framework |
| **ORM / Data** | jOOQ, HikariCP | Type-safe SQL query generation and connection pooling |
| **Database** | PostgreSQL 14+, PostGIS | Relational database with geographic spatial indexing |

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your system:
- **Node.js** (v18.0.0 or higher) & **npm**
- **Java Development Kit (JDK 21+)**
- **PostgreSQL 14+** with the **PostGIS** extension

---

### 1. Database Setup (PostgreSQL)

1. Start your PostgreSQL service.
2. Create the target database:
   ```bash
   createdb healthqueue
   ```
3. Execute the schema script (including PostGIS spatial extensions, tables, and indexes):
   ```bash
   psql -U postgres -d healthqueue -f sql/main.sql
   ```

---

### 2. Backend Setup (Java)

1. Configure environment variables (or rely on defaults):
   ```bash
   export PORT=3000
   export DATABASE_URL="jdbc:postgresql://localhost:5432/healthqueue"
   export DB_USER="postgres"
   export DB_PASSWORD="your_password"
   ```
2. Build and run the Spark backend server:
   ```bash
   ./gradlew run
   ```
   *The backend will start listening on `http://localhost:3000`.*

---

### 3. Frontend Setup (React & Vite)

1. Navigate to the `web` directory:
   ```bash
   cd web
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. Open your browser at `http://localhost:5173`.

> **Note on Offline Mode**: If the Java backend is offline, the frontend automatically falls back to local sample data, allowing you to preview and test all UI flows without a database connection.

---

## 📡 API Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/patient/register` | Register a new patient account |
| `POST` | `/api/auth/patient/login` | Authenticate patient & issue JWT cookies |
| `POST` | `/api/auth/patient/refresh` | Refresh patient session token |
| `POST` | `/api/auth/patient/logout` | Invalidate patient session |
| `POST` | `/api/auth/organization/register` | Register a new clinic organization |
| `POST` | `/api/auth/organization/login` | Authenticate organization admin |
| `POST` | `/api/auth/organization/logout` | Invalidate organization session |
| `GET`  | `/api/auth/whoami` | Retrieve active authenticated session identity |

### Branches (`/api/branches`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET`  | `/api/branches/closest` | Find closest clinics sorted by PostGIS distance (`?latitude=&longitude=&limit=`) |
| `POST` | `/api/branches` | Create a new clinic branch (*Org admin only*) |

### Doctors (`/api/doctors`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/doctors` | Register a new doctor to a branch |
| `POST` | `/api/doctors/change-branch` | Reassign a doctor to another branch |
| `DELETE` | `/api/doctors` | Remove a doctor record |

### Appointments & Queue (`/api/appointments`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST`  | `/api/appointments` | Create and book an appointment ticket |
| `PATCH` | `/api/appointments/:id/status` | Update ticket status (`IN_QUEUE`, `IN_PROGRESS`, `COMPLETED`, etc.) |
| `PATCH` | `/api/appointments/:id/reassign` | Reassign ticket to another doctor |

### Clinic Visits (`/api/visits`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/visits` | Record consultation notes and prescription |
| `GET`  | `/api/visits/history` | Retrieve authenticated patient's visit history |

---

## 📂 Directory Structure

```
HealthQueue/
├── app/                           # Java Backend (Spark Framework)
│   ├── build.gradle               # Gradle build & dependency configuration
│   └── src/main/java/com/healthqueue/
│       ├── App.java               # Main entry point & route definitions
│       ├── controllers/           # Auth, Branch, Doctor, Appointment, Visit controllers
│       ├── middlewares/           # JWT auth injection & CORS configuration
│       ├── models/                # JOOQ models & type definitions
│       └── utils/                 # Password hashing, JWT, Snowflake ID generators
├── sql/
│   └── main.sql                   # PostGIS tables, enums, triggers & spatial indexes
├── web/                           # React Frontend (Vite)
│   ├── src/
│   │   ├── api/                   # Typed API client & endpoint definitions
│   │   ├── components/            # Layouts, UI primitives (Modal, Toast, Button, Input)
│   │   ├── hooks/                 # Geolocation, TanStack Query hooks
│   │   ├── pages/                 # Landing, Patient portal, Org dashboard, Auth views
│   │   ├── store/                 # Persistent Zustand authentication store
│   │   └── types/                 # TypeScript interfaces matching backend models
│   ├── package.json
│   └── vite.config.ts             # Vite config with dev proxy to backend
└── README.md
```

---

## 🛡️ License

This project is open source and available under the [MIT License](LICENSE).
