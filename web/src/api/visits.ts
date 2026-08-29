import { get, post } from './client';
import type { ClinicVisitResponse, RecordClinicVisitRequest } from '../types';

export const visitsApi = {
  /** Org: record a completed clinic visit */
  recordVisit: (body: RecordClinicVisitRequest) =>
    post<ClinicVisitResponse>('/visits', body),

  /** Patient: get own visit history (auth from cookie) */
  getPatientVisitHistory: () =>
    get<ClinicVisitResponse[]>('/visits/history'),
};
