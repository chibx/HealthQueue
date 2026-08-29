import type { ApiError, StructuredResponse } from '../types';

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

export async function apiFetch<T = void>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    credentials: 'include', // send HttpOnly auth cookies
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

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
