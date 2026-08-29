import { post } from './client';
import type { DoctorResponse, CreateDoctorRequest } from '../types';

export const doctorsApi = {
  createDoctor: (body: CreateDoctorRequest) =>
    post<DoctorResponse>('/doctors', body),
};
