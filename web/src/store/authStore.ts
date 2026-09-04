import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/auth';
import type { UserRole } from '../types';

interface AuthState {
  userId: string | null;
  orgId: string | null;
  doctorId: number | null;
  userName: string | null;
  userEmail: string | null;
  role: UserRole | null;
  isLoading: boolean;
  isInitialized: boolean;

  // Actions
  initialize: () => Promise<void>;
  setPatient: (userId: string, name?: string, email?: string) => void;
  setOrg: (orgId: string, name?: string, email?: string) => void;
  setDoctor: (doctorId: number, name?: string, email?: string) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      userId: null,
      orgId: null,
      doctorId: null,
      userName: null,
      userEmail: null,
      role: null,
      isLoading: false,
      isInitialized: false,

      initialize: async () => {
        set({ isLoading: true });
        
        const tryWhoami = async () => {
          const data = await authApi.whoami();
          if (data.doctor) {
            try {
              const profile = await authApi.getDoctorProfile();
              const fullName = profile?.fullName || `${profile?.firstName ?? ''} ${profile?.lastName ?? ''}`.trim() || 'Dr. Medical Staff';
              const email = profile?.email || get().userEmail || '';
              set({ doctorId: data.doctor, userId: null, orgId: null, role: 'doctor', userName: fullName, userEmail: email });
            } catch (err) {
              set({ doctorId: data.doctor, userId: null, orgId: null, role: 'doctor', userName: get().userName || 'Dr. Medical Staff' });
            }
            return true;
          } else if (data.user) {
            try {
              const profile = await authApi.getPatientProfile();
              const fullName = profile?.fullName || `${profile?.firstName ?? ''} ${profile?.lastName ?? ''}`.trim() || 'Patient Member';
              const email = profile?.email || get().userEmail || '';
              set({ userId: data.user, orgId: null, doctorId: null, role: 'patient', userName: fullName, userEmail: email });
            } catch (err) {
              set({ userId: data.user, orgId: null, doctorId: null, role: 'patient', userName: get().userName || 'Patient Member' });
            }
            return true;
          } else if (data.org) {
            try {
              const profile = await authApi.getOrgProfile();
              const name = profile?.name || 'Medical Centre';
              const email = profile?.email || get().userEmail || '';
              set({ userId: null, orgId: data.org, doctorId: null, role: 'organization', userName: name, userEmail: email });
            } catch (err) {
              set({ userId: null, orgId: data.org, doctorId: null, role: 'organization', userName: get().userName || 'Medical Centre' });
            }
            return true;
          }
          return false;
        };

        try {
          const success = await tryWhoami();
          if (!success) {
            throw new Error('Not logged in');
          }
        } catch {
          // Attempt refresh
          try {
            const role = get().role;
            if (role === 'patient') {
              await authApi.refreshPatient();
              const success = await tryWhoami();
              if (!success) get().clear();
            } else if (role === 'doctor') {
              await authApi.refreshDoctor();
              const success = await tryWhoami();
              if (!success) get().clear();
            } else if (role === 'organization') {
              await authApi.refreshOrganization();
              const success = await tryWhoami();
              if (!success) get().clear();
            } else {
              get().clear();
            }
          } catch {
            get().clear();
          }
        } finally {
          set({ isLoading: false, isInitialized: true });
        }
      },

      setPatient: (userId: string, name = 'Patient Member', email = '') =>
        set({
          userId,
          orgId: null,
          doctorId: null,
          role: 'patient',
          userName: name,
          userEmail: email,
          isInitialized: true,
        }),

      setOrg: (orgId: string, name = 'Medical Centre', email = '') =>
        set({
          userId: null,
          orgId,
          doctorId: null,
          role: 'organization',
          userName: name,
          userEmail: email,
          isInitialized: true,
        }),

      setDoctor: (doctorId: number, name = 'Dr. Medical Staff', email = '') =>
        set({
          userId: null,
          orgId: null,
          doctorId,
          role: 'doctor',
          userName: name,
          userEmail: email,
          isInitialized: true,
        }),

      clear: () =>
        set({
          userId: null,
          orgId: null,
          doctorId: null,
          userName: null,
          userEmail: null,
          role: null,
        }),
    }),
    {
      name: 'healthqueue_auth',
      partialize: (state) => ({
        userId: state.userId,
        orgId: state.orgId,
        doctorId: state.doctorId,
        userName: state.userName,
        userEmail: state.userEmail,
        role: state.role,
      }),
    }
  )
);
