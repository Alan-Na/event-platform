import { apiClient } from '@/api/axios';
import type { ApiResponse, PageResponse } from '@/types/common';
import type { NotificationItem } from '@/types/notification';

export async function getNotificationsApi(page = 0, size = 10, unreadOnly = false) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<NotificationItem>>>('/me/notifications', {
    params: { page, size, unreadOnly }
  });
  return data.data;
}

export async function markNotificationReadApi(id: number) {
  const { data } = await apiClient.patch<ApiResponse<NotificationItem>>(`/me/notifications/${id}/read`);
  return data.data;
}

export async function markAllNotificationsReadApi() {
  await apiClient.patch('/me/notifications/read-all');
}

export async function getUnreadCountApi() {
  const { data } = await apiClient.get<ApiResponse<{ unreadCount: number }>>('/me/notifications/unread-count');
  return data.data.unreadCount;
}
