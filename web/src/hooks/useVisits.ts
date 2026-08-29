import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { visitsApi } from '../api/visits';
import type { RecordClinicVisitRequest } from '../types';

/** Patient hook — fetch own visit history from the real backend */
export function usePatientVisitHistory() {
  return useQuery({
    queryKey: ['visits', 'patient'],
    queryFn: visitsApi.getPatientVisitHistory,
    staleTime: 1000 * 60 * 2, // 2 min cache
    retry: false,
  });
}

/** Org hook — record a completed clinic visit */
export function useRecordVisit() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: RecordClinicVisitRequest) => visitsApi.recordVisit(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['visits'] });
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}
