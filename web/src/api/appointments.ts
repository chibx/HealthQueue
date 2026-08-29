import { post, patch } from './client';
import type {
  AppointmentResponse,
  CreateAppointmentRequest,
  UpdateAppointmentStatusRequest,
  ReassignDoctorRequest,
} from '../types';

export const appointmentsApi = {
  createAppointment: (body: CreateAppointmentRequest) =>
    post<AppointmentResponse>('/appointments', body),

  updateStatus: (id: number, body: UpdateAppointmentStatusRequest) =>
    patch<AppointmentResponse>(`/appointments/${id}/status`, body),

  reassignDoctor: (id: number, body: ReassignDoctorRequest) =>
    patch<AppointmentResponse>(`/appointments/${id}/reassign`, body),
};
