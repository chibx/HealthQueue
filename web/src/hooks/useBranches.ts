import { useQuery } from '@tanstack/react-query';
import { branchesApi } from '../api/branches';

export function useClosestBranches(
  latitude: number | null,
  longitude: number | null,
  limit = 5,
) {
  return useQuery({
    queryKey: ['branches', 'closest', latitude, longitude, limit],
    queryFn: () => branchesApi.findClosest(latitude!, longitude!, limit),
    enabled: latitude !== null && longitude !== null,
    staleTime: 1000 * 60 * 2,
  });
}
