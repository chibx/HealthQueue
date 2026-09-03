import type { ApiError, StructuredResponse } from '../types';
import { useAuthStore } from '../store/authStore';

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api';

export class ApiException extends Error {
  status: number;
  errors?: { field: string; message: string }[];

  constructor(error: ApiError) {
    super(error.message);
    this.status = error.status;
    this.errors = error.errors;
    this.name = 'ApiException';
  }
}

async function parseResponse<T>(res: Response): Promise<T> {
  const text = await res.text();

  // Empty body (e.g. 204, or endpoints returning null)
  if (!text || text === 'null') {
    if (!res.ok) throw new ApiException({ status: res.status, message: 'Request failed' });
    return null as T;
  }

  let json: unknown;
  try {
    json = JSON.parse(text);
  } catch {
    if (!res.ok) throw new ApiException({ status: res.status, message: text });
    return text as T;
  }

  if (!res.ok) {
    const err = json as ApiError;
    throw new ApiException({ status: res.status, message: err.message ?? 'Unknown error', errors: err.errors });
  }

  // Unwrap StructuredResponse envelope if present
  if (json && typeof json === 'object' && 'data' in (json as object)) {
    return (json as StructuredResponse<T>).data;
  }

  return json as T;
}

let isRefreshing = false;
let refreshPromise: Promise<boolean> | null = null;

export async function apiFetch<T = void>(
  path: string,
  options: RequestInit = {},
  isRetry = false
): Promise<T> {
  const reqOptions = {
    ...options,
    credentials: 'include' as RequestCredentials,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  };

  const res = await fetch(`${BASE_URL}${path}`, reqOptions);

  if (res.status === 401 && !isRetry && !path.includes('/auth/login') && !path.includes('/auth/register') && !path.includes('/auth/refresh') && !path.includes('/auth/whoami')) {
    if (!isRefreshing) {
      isRefreshing = true;
      refreshPromise = (async () => {
        try {
          const role = useAuthStore.getState().role;
          if (!role) return false;
          
          const refreshPath = role === 'patient' ? '/auth/patient/refresh' : '/auth/organization/refresh';
          const refreshRes = await fetch(`${BASE_URL}${refreshPath}`, {
            method: 'POST',
            credentials: 'include',
          });
          
          if (!refreshRes.ok) {
            useAuthStore.getState().clear();
            return false;
          }
          return true;
        } catch {
          useAuthStore.getState().clear();
          return false;
        } finally {
          isRefreshing = false;
        }
      })();
    }

    const success = await refreshPromise;
    if (success) {
      const retryRes = await fetch(`${BASE_URL}${path}`, reqOptions);
      return parseResponse<T>(retryRes);
    }
  }

  return parseResponse<T>(res);
}

export function get<T>(path: string): Promise<T> {
  return apiFetch<T>(path, { method: 'GET' });
}

export function post<T>(path: string, body?: unknown): Promise<T> {
  return apiFetch<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined });
}

export function patch<T>(path: string, body?: unknown): Promise<T> {
  return apiFetch<T>(path, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined });
}

export function del<T = void>(path: string): Promise<T> {
  return apiFetch<T>(path, { method: 'DELETE' });
}
