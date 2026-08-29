import { useQuery } from '@tanstack/react-query';
import { authApi } from '../api/auth';

export function useWhoami() {
  return useQuery({
    queryKey: ['whoami'],
    queryFn: authApi.whoami,
    staleTime: 1000 * 60 * 5, // 5 minutes
    retry: false,
  });
}
