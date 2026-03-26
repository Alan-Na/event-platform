import { create } from 'zustand';

export interface ToastItem {
  id: number;
  title: string;
  tone: 'success' | 'error' | 'info';
}

interface UiState {
  mobileMenuOpen: boolean;
  adminSidebarCollapsed: boolean;
  toasts: ToastItem[];
  toggleMobileMenu: () => void;
  toggleAdminSidebar: () => void;
  addToast: (toast: Omit<ToastItem, 'id'>) => void;
  removeToast: (id: number) => void;
}

export const useUiStore = create<UiState>((set) => ({
  mobileMenuOpen: false,
  adminSidebarCollapsed: false,
  toasts: [],
  toggleMobileMenu() {
    set((state) => ({ mobileMenuOpen: !state.mobileMenuOpen }));
  },
  toggleAdminSidebar() {
    set((state) => ({ adminSidebarCollapsed: !state.adminSidebarCollapsed }));
  },
  addToast(toast) {
    const id = Date.now();
    set((state) => ({ toasts: [...state.toasts, { id, ...toast }] }));
    window.setTimeout(() => {
      set((state) => ({ toasts: state.toasts.filter((item) => item.id !== id) }));
    }, 2800);
  },
  removeToast(id) {
    set((state) => ({ toasts: state.toasts.filter((item) => item.id !== id) }));
  }
}));
