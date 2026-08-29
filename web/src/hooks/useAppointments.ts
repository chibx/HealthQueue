import { useMutation, useQueryClient } from '@tanstack/react-query';
import { appointmentsApi } from '../api/appointments';
import type {
  CreateAppointmentRequest,
  UpdateAppointmentStatusRequest,
  ReassignDoctorRequest,
} from '../types';

export function useCreateAppointment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateAppointmentRequest) =>
      appointmentsApi.createAppointment(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

export function useUpdateAppointmentStatus(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: UpdateAppointmentStatusRequest) =>
      appointmentsApi.updateStatus(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

export function useReassignDoctor(id: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: ReassignDoctorRequest) =>
      appointmentsApi.reassignDoctor(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}
