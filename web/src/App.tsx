import { lazy, Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RootLayout } from './components/layout/RootLayout';
import { ProtectedRoute } from './components/shared/ProtectedRoute';
import { FullPageSpinner } from './components/ui/Spinner';
import { useAuthStore } from './store/authStore';

// Public
const Landing          = lazy(() => import('./pages/Landing'));
const PatientLogin     = lazy(() => import('./pages/auth/PatientLogin'));
const PatientRegister  = lazy(() => import('./pages/auth/PatientRegister'));
const OrgLogin         = lazy(() => import('./pages/auth/OrgLogin'));
const OrgRegister      = lazy(() => import('./pages/auth/OrgRegister'));

// Patient portal
const PatientDashboard = lazy(() => import('./pages/patient/PatientDashboard'));
const FindBranch       = lazy(() => import('./pages/patient/FindBranch'));
const BookAppointment  = lazy(() => import('./pages/patient/BookAppointment'));
const VisitHistory     = lazy(() => import('./pages/patient/VisitHistory'));

// Org dashboard
const OrgDashboard     = lazy(() => import('./pages/org/OrgDashboard'));
const Branches         = lazy(() => import('./pages/org/Branches'));
const Doctors          = lazy(() => import('./pages/org/Doctors'));
const AppointmentQueue = lazy(() => import('./pages/org/AppointmentQueue'));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function AppRoutes() {
  const initialize = useAuthStore((s) => s.initialize);
  const isInitialized = useAuthStore((s) => s.isInitialized);

  useEffect(() => {
    if (!isInitialized) initialize();
  }, [initialize, isInitialized]);

  return (
    <Suspense fallback={<FullPageSpinner />}>
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<Landing />} />
        <Route path="/auth/patient/login"    element={<PatientLogin />} />
        <Route path="/auth/patient/register" element={<PatientRegister />} />
        <Route path="/auth/org/login"        element={<OrgLogin />} />
        <Route path="/auth/org/register"     element={<OrgRegister />} />

        {/* Patient portal — protected */}
        <Route
          element={
            <ProtectedRoute role="patient">
              <RootLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/patient"         element={<PatientDashboard />} />
          <Route path="/patient/find"    element={<FindBranch />} />
          <Route path="/patient/book"    element={<BookAppointment />} />
          <Route path="/patient/history" element={<VisitHistory />} />
        </Route>

        {/* Organization dashboard — protected */}
        <Route
          element={
            <ProtectedRoute role="organization">
              <RootLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/org"          element={<OrgDashboard />} />
          <Route path="/org/branches" element={<Branches />} />
          <Route path="/org/doctors"  element={<Doctors />} />
          <Route path="/org/queue"    element={<AppointmentQueue />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </QueryClientProvider>
  );
}
