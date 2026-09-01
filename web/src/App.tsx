import { lazy, Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
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
const LiveQueue        = lazy(() => import('./pages/patient/LiveQueue'));
const PatientProfile   = lazy(() => import('./pages/patient/PatientProfile'));

// Org dashboard
const OrgDashboard     = lazy(() => import('./pages/org/OrgDashboard'));
const Branches         = lazy(() => import('./pages/org/Branches'));
const Doctors          = lazy(() => import('./pages/org/Doctors'));
const AppointmentQueue = lazy(() => import('./pages/org/AppointmentQueue'));
const FastStatus       = lazy(() => import('./pages/org/FastStatus'));
const Analytics        = lazy(() => import('./pages/org/Analytics'));
const OrgSettings      = lazy(() => import('./pages/org/OrgSettings'));

const NotFound         = lazy(() => import('./pages/NotFound'));

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
          <Route path="/patient/queue"   element={<LiveQueue />} />
          <Route path="/patient/history" element={<VisitHistory />} />
          <Route path="/patient/profile" element={<PatientProfile />} />
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
          <Route path="/org/status"   element={<FastStatus />} />
          <Route path="/org/analytics" element={<Analytics />} />
          <Route path="/org/settings"  element={<OrgSettings />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<NotFound />} />
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
