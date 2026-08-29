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
        try {
          const data = await authApi.whoami();
          if (data.user) {
            set({
              userId: data.user,
              orgId: null,
              role: 'patient',
              userName: get().userName || 'Jane Doe',
            });
          } else if (data.org) {
            set({
              userId: null,
              orgId: data.org,
              role: 'organization',
              userName: get().userName || 'City General Hospital',
            });
          } else if (!get().role) {
            set({ userId: null, orgId: null, role: null, userName: null, userEmail: null });
          }
        } catch {
          // If whoami fails and no local role is persisted, clear
          if (!get().role) {
            set({ userId: null, orgId: null, role: null });
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
