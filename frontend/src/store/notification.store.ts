import { create } from 'zustand';
import { getNotificationsApi, getUnreadCountApi, markAllNotificationsReadApi, markNotificationReadApi } from '@/api/notifications.api';
import type { NotificationItem } from '@/types/notification';

interface NotificationState {
  items: NotificationItem[];
  unreadCount: number;
  loading: boolean;
  fetchNotifications: () => Promise<void>;
  fetchUnreadCount: () => Promise<void>;
  markRead: (id: number) => Promise<void>;
  markAllRead: () => Promise<void>;
}

export const useNotificationStore = create<NotificationState>((set, get) => ({
  items: [],
  unreadCount: 0,
  loading: false,
  async fetchNotifications() {
    set({ loading: true });
    try {
      const result = await getNotificationsApi(0, 10, false);
      set({ items: result.items, loading: false });
    } catch {
      set({ loading: false });
    }
  },
  async fetchUnreadCount() {
    try {
      const unreadCount = await getUnreadCountApi();
      set({ unreadCount });
    } catch {
      set({ unreadCount: 0 });
    }
  },
  async markRead(id) {
    await markNotificationReadApi(id);
    set({ items: get().items.map((item) => (item.id === id ? { ...item, isRead: true } : item)) });
    await get().fetchUnreadCount();
  },
  async markAllRead() {
    await markAllNotificationsReadApi();
    set({ items: get().items.map((item) => ({ ...item, isRead: true })), unreadCount: 0 });
  }
}));
