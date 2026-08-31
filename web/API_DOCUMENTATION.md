# HealthQueue — Backend API Contract

> **Version:** 1.0  
> **Audience:** Backend engineer  
> **Frontend base URL env var:** `VITE_API_URL` (default proxied to `/api`)  
> **Auth strategy:** **HttpOnly cookie** — no `Authorization` header needed. The frontend sends `credentials: 'include'` on every request.

---

## General Conventions

### Base URL
All paths below are relative to the API root. Example: `POST /auth/patient/register` → `https://api.healthqueue.com/auth/patient/register`

### Request headers
```
Content-Type: application/json
```
*(No Authorization header — use HttpOnly cookies)*

### Success envelope
```json
{
  "status": 200,
  "message": "...",
  "data": { }
}
```
The frontend unwraps `data` automatically. If the response body is just the object (no envelope), it also works fine.

### Error envelope
```json
{ "status": 400, "message": "Human-readable message" }
```
Field-level validation errors:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    { "field": "email", "message": "Enter a valid email address" }
  ]
}
```

---

## Enums

### AppointmentStatus
`SCHEDULED | IN_QUEUE | IN_PROGRESS | COMPLETED | OVERSTAYED | CANCELLED`

### UserRole
`patient | organization`

---

## 1. Authentication

### 1.1 Register Patient
`POST /auth/patient/register`

**Request body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "phoneNumber": "+2348012345678",
  "password": "SecurePass123",
  "latitude": 6.5244,
  "longitude": 3.3792
}
```
**Response** `201`: `{ "status": 201, "message": "Patient registered.", "data": null }`

---

### 1.2 Login Patient
`POST /auth/patient/login`

**Request body:**
```json
{ "email": "jane@example.com", "password": "SecurePass123" }
```
**Response** `200`: Sets HttpOnly cookie. Body: `{ "status": 200, "message": "Login successful.", "data": null }`

---

### 1.3 Refresh Patient Token
`POST /auth/patient/refresh` — No body. Rotates HttpOnly cookie.

---

### 1.4 Logout Patient
`POST /auth/patient/logout` — Clears patient cookie.

---

### 1.5 Register Organization
`POST /auth/organization/register`

**Request body:**
```json
{
  "name": "City General Hospital",
  "registrationCode": "CAC-12345",
  "email": "admin@citygeneral.com",
  "password": "OrgPass456"
}
```

---

### 1.6 Login Organization
`POST /auth/organization/login`

**Request body:**
```json
{ "email": "admin@citygeneral.com", "password": "OrgPass456" }
```
Sets org HttpOnly cookie.

---

### 1.7 Refresh Organization Token
`POST /auth/organization/refresh`

---

### 1.8 Logout Organization
`POST /auth/organization/logout`

---

### 1.9 Whoami (Critical — called on every app load)
`GET /auth/whoami`

**Response** `200`:
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "user": "550e8400-e29b-41d4-a716-446655440000",
    "org": null
  }
}
```
- Patient cookie valid → `user` = UUID, `org` = null
- Org cookie valid → `user` = null, `org` = UUID
- No session → both null

---

## 2. Branches

### 2.1 Create Branch
`POST /branches` — Requires org cookie

**Request body:**
```json
{
  "organizationId": "550e8400-...",
  "name": "Victoria Island Branch",
  "address": "14 Adeola Odeku Street, VI, Lagos",
  "latitude": 6.4268,
  "longitude": 3.4218
}
```
**Response** `201 BranchResponse`

---

### 2.2 Find Closest Branches
`GET /branches/closest?latitude={lat}&longitude={lng}&limit={limit}` — Requires patient cookie

**Response** `200 ClosestBranchResponse[]`:
```json
{
  "status": 200,
  "data": [
    {
      "branch": { "id": 1, "organizationId": "...", "name": "VI Branch", "address": "...", "latitude": 6.4268, "longitude": 3.4218 },
      "distanceInKilometers": 1.42
    }
  ]
}
```

---

## 3. Doctors

### 3.1 Create Doctor
`POST /doctors` — Requires org cookie

**Request body:**
```json
{
  "branchId": 1,
  "firstName": "Emeka",
  "lastName": "Okafor",
  "specialty": "Cardiology"
}
```
**Response** `201 DoctorResponse`:
```json
{ "id": 1, "branchId": 1, "firstName": "Emeka", "lastName": "Okafor", "specialty": "Cardiology", "isAvailable": true }
```

---

## 4. Appointments

### 4.1 Create Appointment
`POST /appointments` — Requires patient cookie

**Request body:**
```json
{
  "userId": "550e8400-...",
  "branchId": 1,
  "doctorId": 1,
  "scheduledStartTime": "2026-09-01T09:00:00Z",
  "scheduledEndTime": "2026-09-01T09:30:00Z"
}
```
**Response** `201 AppointmentResponse`

---

### 4.2 Update Appointment Status
`PATCH /appointments/{id}/status` — Requires org cookie

**Request body:**
```json
{
  "appointmentId": 101,
  "status": "IN_PROGRESS",
  "actualStartTime": "2026-09-01T09:05:00Z",
  "actualEndTime": null
}
```
**Response** `200 AppointmentResponse`

---

### 4.3 Reassign Doctor
`PATCH /appointments/{id}/reassign` — Requires org cookie

**Request body:**
```json
{ "appointmentId": 101, "newDoctorId": 3 }
```
**Response** `200 AppointmentResponse`

---

## 5. Clinic Visits

### 5.1 Record Clinic Visit
`POST /visits` — Requires org cookie

**Request body:**
```json
{
  "appointmentId": 101,
  "userId": "550e8400-...",
  "doctorId": 1,
  "visitNotes": "Patient presented with hypertension...",
  "prescriptionDetails": "Amlodipine 5mg — once daily for 30 days"
}
```
**Response** `201 ClinicVisitResponse`

---

### 5.2 Get Patient Visit History
`GET /visits/history` — Requires patient cookie. Returns records for authenticated patient.

**Response** `200 ClinicVisitResponse[]`

---

## 6. Route Summary

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/auth/patient/register` | Public | Register patient |
| `POST` | `/auth/patient/login` | Public | Login patient (sets cookie) |
| `POST` | `/auth/patient/refresh` | Patient cookie | Refresh patient token |
| `POST` | `/auth/patient/logout` | Patient cookie | Logout patient |
| `POST` | `/auth/organization/register` | Public | Register org |
| `POST` | `/auth/organization/login` | Public | Login org (sets cookie) |
| `POST` | `/auth/organization/refresh` | Org cookie | Refresh org token |
| `POST` | `/auth/organization/logout` | Org cookie | Logout org |
| `GET` | `/auth/whoami` | Any cookie | Get current session identity |
| `POST` | `/branches` | Org cookie | Create branch |
| `GET` | `/branches/closest` | Patient cookie | Find nearest branches by GPS |
| `POST` | `/doctors` | Org cookie | Register doctor |
| `POST` | `/appointments` | Patient cookie | Book appointment / queue ticket |
| `PATCH` | `/appointments/{id}/status` | Org cookie | Update appointment status |
| `PATCH` | `/appointments/{id}/reassign` | Org cookie | Reassign doctor |
| `POST` | `/visits` | Org cookie | Record completed visit |
| `GET` | `/visits/history` | Patient cookie | Get patient visit history |

---

## 7. CORS & Cookie Configuration

Frontend origins:
- Development: `http://localhost:5173`
- Production: `https://healthqueue.com`

Required CORS headers:
```
Access-Control-Allow-Origin: <frontend-origin>
Access-Control-Allow-Credentials: true
```

Cookie attributes:
```
HttpOnly: true
SameSite: Lax
Secure: true (production only)
Path: /
```
