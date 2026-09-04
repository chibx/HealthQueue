import { get, post } from './client';
import type {
  WhoamiResponse,
  PatientRegisterRequest,
  PatientLoginRequest,
  OrgRegisterRequest,
  OrgLoginRequest,
  DoctorLoginRequest,
  PatientProfileResponse,
  OrgProfileResponse,
  DoctorProfileResponse,
} from '../types';

export const authApi = {
  // ---- Patient ----
  registerPatient: (body: PatientRegisterRequest) =>
    post<void>('/auth/patient/register', body),

  loginPatient: (body: PatientLoginRequest) =>
    post<void>('/auth/patient/login', body),

  refreshPatient: () =>
    post<void>('/auth/patient/refresh'),

  logoutPatient: () =>
    post<void>('/auth/patient/logout'),

  // ---- Organization ----
  registerOrganization: (body: OrgRegisterRequest) =>
    post<void>('/auth/organization/register', body),

  loginOrganization: (body: OrgLoginRequest) =>
    post<void>('/auth/organization/login', body),

  refreshOrganization: () =>
    post<void>('/auth/organization/refresh'),

  logoutOrganization: () =>
    post<void>('/auth/organization/logout'),

  // ---- Staff / Doctor ----
  loginDoctor: (body: DoctorLoginRequest) =>
    post<void>('/auth/doctor/login', body),

  refreshDoctor: () =>
    post<void>('/auth/doctor/refresh'),

  logoutDoctor: () =>
    post<void>('/auth/doctor/logout'),

  // ---- Shared ----
  whoami: () =>
    get<WhoamiResponse>('/auth/whoami'),

  // ---- Profile ----
  getPatientProfile: () =>
    get<PatientProfileResponse>('/profile/patient'),

  getOrgProfile: () =>
    get<OrgProfileResponse>('/profile/organization'),

  getDoctorProfile: () =>
    get<DoctorProfileResponse>('/profile/doctor'),
};
