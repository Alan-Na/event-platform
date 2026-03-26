import { apiClient } from '@/api/axios';
import type { ApiResponse, PageResponse } from '@/types/common';
import type { EventDetail, EventSummary, EventUpsertRequest } from '@/types/event';
import type { MyBookingItem } from '@/types/booking';

export interface DashboardStats {
  totalEvents: number;
  publishedEvents: number;
  totalUsers: number;
  confirmedBookings: number;
  waitingEntries: number;
  upcomingEvents: number;
  averageFillRate: number;
}

export interface AdminBookingItem {
  bookingId: number;
  userId: number;
  userName: string;
  userEmail: string;
  status: string;
  source: string;
  bookedAt: string;
}

export interface AdminWaitlistItem {
  waitlistEntryId: number;
  userId: number;
  userName: string;
  userEmail: string;
  position: number;
  status: string;
  joinedAt: string;
  promotedAt?: string | null;
}

export interface AdminUserOverview {
  userId: number;
  fullName: string;
  email: string;
  roles: string[];
  confirmedBookings: number;
  waitingEntries: number;
  cancelledRecords: number;
}

export async function getDashboardStatsApi() {
  const { data } = await apiClient.get<ApiResponse<DashboardStats>>('/admin/dashboard');
  return data.data;
}

export async function getAdminEventsApi(params: { status?: string; keyword?: string; page?: number; size?: number }) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<EventSummary>>>('/admin/events', { params });
  return data.data;
}

export async function createAdminEventApi(payload: EventUpsertRequest) {
  const { data } = await apiClient.post<ApiResponse<EventDetail>>('/admin/events', payload);
  return data.data;
}

export async function updateAdminEventApi(id: string | number, payload: EventUpsertRequest) {
  const { data } = await apiClient.put<ApiResponse<EventDetail>>(`/admin/events/${id}`, payload);
  return data.data;
}

export async function publishAdminEventApi(id: string | number) {
  const { data } = await apiClient.patch<ApiResponse<EventDetail>>(`/admin/events/${id}/publish`);
  return data.data;
}

export async function closeAdminEventApi(id: string | number) {
  const { data } = await apiClient.patch<ApiResponse<EventDetail>>(`/admin/events/${id}/close`);
  return data.data;
}

export async function cancelAdminEventApi(id: string | number, reason = '') {
  const { data } = await apiClient.patch<ApiResponse<EventDetail>>(`/admin/events/${id}/cancel`, { reason });
  return data.data;
}

export async function getAdminEventBookingsApi(id: string | number, page = 0, size = 20) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<AdminBookingItem>>>(`/admin/events/${id}/bookings`, {
    params: { page, size }
  });
  return data.data;
}

export async function getAdminEventWaitlistApi(id: string | number, page = 0, size = 20) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<AdminWaitlistItem>>>(`/admin/events/${id}/waitlist`, {
    params: { page, size }
  });
  return data.data;
}

export async function getAdminUsersApi(params: { keyword?: string; page?: number; size?: number }) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<AdminUserOverview>>>('/admin/users', { params });
  return data.data;
}

export async function getAdminUserBookingsApi(id: string | number, page = 0, size = 10) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<MyBookingItem>>>(`/admin/users/${id}/bookings`, {
    params: { page, size }
  });
  return data.data;
}
