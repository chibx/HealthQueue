import { get, post } from './client';
import type {
  BranchResponse,
  ClosestBranchResponse,
  CreateBranchRequest,
} from '../types';

export const branchesApi = {
  createBranch: (body: CreateBranchRequest) =>
    post<BranchResponse>('/branches', body),

  findClosest: (latitude: number, longitude: number, limit = 5) =>
    get<ClosestBranchResponse[]>(
      `/branches/closest?latitude=${latitude}&longitude=${longitude}&limit=${limit}`,
    ),
};
