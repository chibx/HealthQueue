// =============================================
// ENUMS
// =============================================

export type AppointmentStatus =
  | 'SCHEDULED'
  | 'IN_QUEUE'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'OVERSTAYED'
  | 'CANCELLED';

export type UserRole = 'patient' | 'organization';

// =============================================
// BASE API STRUCTURES
// =============================================

export interface StructuredResponse<T = null> {
  status: number;
  message: string;
  data: T;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface StructuredError400 {
  status: number;
  message: string;
  errors: ValidationError[];
}

export interface ApiError {
  status: number;
  message: string;
  errors?: ValidationError[];
}

// =============================================
// AUTH RESPONSES
// =============================================

export interface WhoamiResponse {
  user: string | null; // UUID
  org: string | null; // UUID
}

export interface AuthResponse {
  token: string;
  id: string; // UUID
  role: UserRole;
}

// =============================================
// USER (PATIENT)
// =============================================

export interface UserResponse {
  id: string; // UUID
  firstName: string;
  lastName: string;
  email: string;
}

// =============================================
// ORGANIZATION
// =============================================

export interface OrganizationResponse {
  id: string; // UUID
  name: string;
  registrationCode: string;
}

// =============================================
// BRANCH
// =============================================

export interface BranchResponse {
  id: number;
  organizationId: string; // UUID
  name: string;
  address: string;
  latitude: number;
  longitude: number;
}

export interface ClosestBranchResponse {
  branch: BranchResponse;
  distanceInKilometers: number;
}

// =============================================
// DOCTOR
// =============================================

export interface DoctorResponse {
  id: number;
  branchId: number;
  firstName: string;
  lastName: string;
  specialty: string;
  isAvailable: boolean;
}

// =============================================
// APPOINTMENT
// =============================================

export interface AppointmentResponse {
  id: number;
  userId: string; // UUID
  branchId: number;
  doctorId: number;
  status: AppointmentStatus;
  scheduledStartTime: string; // ISO 8601
  scheduledEndTime: string;
  actualStartTime: string | null;
  actualEndTime: string | null;
}

// =============================================
// CLINIC VISIT
// =============================================

export interface ClinicVisitResponse {
  id: number;
  appointmentId: number;
  userId: string; // UUID
  doctorId: number;
  visitNotes: string | null;
  prescriptionDetails: string | null;
  recordedAt: string; // ISO 8601
}

// =============================================
// REQUEST BODIES (mirrors ServerRequest.java)
// =============================================

export interface PatientRegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
  latitude: number;
  longitude: number;
}

export interface PatientLoginRequest {
  email: string;
  password: string;
}

export interface OrgRegisterRequest {
  name: string;
  registrationCode: string;
  email: string;
  password: string;
}

export interface OrgLoginRequest {
  email: string;
  password: string;
}

export interface CreateBranchRequest {
  organizationId: string;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
}

export interface CreateDoctorRequest {
  branchId: number;
  firstName: string;
  lastName: string;
  specialty: string;
}

export interface CreateAppointmentRequest {
  userId: string;
  branchId: number;
  doctorId: number;
  scheduledStartTime: string;
  scheduledEndTime: string;
}

export interface UpdateAppointmentStatusRequest {
  appointmentId: number;
  status: AppointmentStatus;
  actualStartTime?: string;
  actualEndTime?: string;
}

export interface ReassignDoctorRequest {
  appointmentId: number;
  newDoctorId: number;
}

export interface FindClosestBranchesRequest {
  latitude: number;
  longitude: number;
  limit: number;
}

export interface RecordClinicVisitRequest {
  appointmentId: number;
  userId: string;
  doctorId: number;
  visitNotes?: string;
  prescriptionDetails?: string;
}
