import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { CurrentUser, LoginRequest, RegisterRequest } from '@/types/auth';
import { clearStoredToken, setStoredToken } from '@/utils/storage';
import { getCurrentUserApi, loginApi, registerApi } from '@/api/auth.api';

interface AuthState {
  token: string | null;
  user: CurrentUser | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  fetchMe: () => Promise<void>;
  logout: () => void;
  setUser: (user: CurrentUser | null) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      loading: false,
      async login(payload) {
        set({ loading: true });
        try {
          const auth = await loginApi(payload);
          setStoredToken(auth.accessToken);
          set({ token: auth.accessToken, user: auth.user, isAuthenticated: true, loading: false });
        } catch (error) {
          set({ loading: false });
          throw error;
        }
      },
      async register(payload) {
        set({ loading: true });
        try {
          const auth = await registerApi(payload);
          setStoredToken(auth.accessToken);
          set({ token: auth.accessToken, user: auth.user, isAuthenticated: true, loading: false });
        } catch (error) {
          set({ loading: false });
          throw error;
        }
      },
      async fetchMe() {
        if (!get().token) return;
        try {
          const user = await getCurrentUserApi();
          set({ user, isAuthenticated: true });
        } catch {
          get().logout();
        }
      },
      logout() {
        clearStoredToken();
        set({ token: null, user: null, isAuthenticated: false, loading: false });
      },
      setUser(user) {
        set({ user, isAuthenticated: !!user });
      }
    }),
    {
      name: 'eventflow-auth',
      partialize: (state) => ({ token: state.token, user: state.user, isAuthenticated: state.isAuthenticated })
    }
  )
);
