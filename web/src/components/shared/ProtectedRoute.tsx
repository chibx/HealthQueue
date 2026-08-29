import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { FullPageSpinner } from '../ui/Spinner';
import type { UserRole } from '../../types';

interface ProtectedRouteProps {
  children: React.ReactNode;
  role: UserRole;
}

export function ProtectedRoute({ children, role }: ProtectedRouteProps) {
  const { isInitialized, isLoading, role: authRole } = useAuthStore();

  if (!isInitialized || isLoading) return <FullPageSpinner />;

  if (!authRole) {
    // Not authenticated at all
    const loginPath = role === 'patient' ? '/auth/patient/login' : '/auth/org/login';
    return <Navigate to={loginPath} replace />;
  }

  if (authRole !== role) {
    // Wrong role — redirect to the correct portal
    return <Navigate to={authRole === 'patient' ? '/patient' : '/org'} replace />;
  }

  return <>{children}</>;
}
