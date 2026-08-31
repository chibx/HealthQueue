import { ApiException } from '../api/client';

/**
 * Extracts a human-readable error message from any thrown value.
 * Handles ApiException (with optional per-field validation errors),
 * plain Error objects, and unknown values.
 */
export function getErrorMessage(err: unknown): string {
  if (err instanceof ApiException) {
    // If the server sent validation errors, list them
    if (err.errors && err.errors.length > 0) {
      return err.errors.map((e) => e.message).join('. ');
    }
    return err.message || 'An unexpected error occurred.';
  }
  if (err instanceof Error) return err.message;
  return 'An unexpected error occurred. Please try again.';
}

/**
 * Returns per-field validation errors from an ApiException as a record,
 * so react-hook-form setError can be called for each field.
 */
export function getFieldErrors(err: unknown): Record<string, string> {
  if (err instanceof ApiException && err.errors) {
    return Object.fromEntries(err.errors.map((e) => [e.field, e.message]));
  }
  return {};
}
