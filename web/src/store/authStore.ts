import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/auth';
import type { UserRole } from '../types';

interface AuthState {
  userId: string | null;
  orgId: string | null;
  userName: string | null;
  userEmail: string | null;
  role: UserRole | null;
  isLoading: boolean;
  isInitialized: boolean;

  // Actions
  initialize: () => Promise<void>;
  setPatient: (userId: string, name?: string, email?: string) => void;
  setOrg: (orgId: string, name?: string, email?: string) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      userId: null,
      orgId: null,
      userName: null,
      userEmail: null,
      role: null,
      isLoading: false,
      isInitialized: false,

      initialize: async () => {
        set({ isLoading: true });
        
        const tryWhoami = async () => {
          const data = await authApi.whoami();
          if (data.user) {
            try {
              const profile = await authApi.getPatientProfile();
              const fullName = profile?.fullName || profile?.full_name || (profile?.firstName && profile?.lastName ? `${profile.firstName} ${profile.lastName}` : null) || get().userName || 'Jane Doe';
              const email = profile?.email || get().userEmail || 'jane@example.com';
              set({ userId: data.user, orgId: null, role: 'patient', userName: fullName, userEmail: email });
            } catch (err) {
              set({ userId: data.user, orgId: null, role: 'patient', userName: get().userName || 'Jane Doe' });
            }
            return true;
          } else if (data.org) {
            try {
              const profile = await authApi.getOrgProfile();
              const name = profile?.name || profile?.organizationName || profile?.organization_name || get().userName || 'City General Hospital';
              const email = profile?.email || get().userEmail || 'admin@hospital.com';
              set({ userId: null, orgId: data.org, role: 'organization', userName: name, userEmail: email });
            } catch (err) {
              set({ userId: null, orgId: data.org, role: 'organization', userName: get().userName || 'City General Hospital' });
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

      setPatient: (userId: string, name = 'Jane Doe', email = 'jane@example.com') =>
        set({
          userId,
          orgId: null,
          role: 'patient',
          userName: name,
          userEmail: email,
          isInitialized: true,
        }),

      setOrg: (orgId: string, name = 'City General Hospital', email = 'admin@hospital.com') =>
        set({
          userId: null,
          orgId,
          role: 'organization',
          userName: name,
          userEmail: email,
          isInitialized: true,
        }),

      clear: () =>
        set({
          userId: null,
          orgId: null,
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
        userName: state.userName,
        userEmail: state.userEmail,
        role: state.role,
      }),
    }
  )
);
