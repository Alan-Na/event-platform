import { useAuthStore } from '@/store/auth.store';

export function useAuth() {
  return useAuthStore((state) => ({
    token: state.token,
    user: state.user,
    isAuthenticated: state.isAuthenticated,
    login: state.login,
    register: state.register,
    fetchMe: state.fetchMe,
    logout: state.logout
  }));
}
