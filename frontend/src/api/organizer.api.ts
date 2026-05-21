import { apiClient } from '@/api/axios';
import type { ApiResponse, PageResponse } from '@/types/common';
import type { EventDetail, EventSummary, EventUpsertRequest } from '@/types/event';

export interface OrganizerDashboardDto {
  totalEvents: number;
  publishedEvents: number;
  totalConfirmedBookings: number;
  totalWaitlistCount: number;
  totalCheckedIn: number;
  upcomingEvents: OrganizerEventStatsDto[];
}

export interface OrganizerEventStatsDto {
  eventId: number;
  eventTitle: string;
  status: string;
  capacity: number;
  confirmedCount: number;
  waitlistCount: number;
  checkedInCount: number;
  startTime: string;
}

export interface AdminBookingItem {
  bookingId: number;
  userId: number;
  userFullName: string;
  userEmail: string;
  status: string;
  bookedAt: string;
  cancelledAt?: string | null;
  confirmationCode?: string | null;
  ticketTypeName?: string | null;
  checkedInAt?: string | null;
}

export interface CheckInRequest {
  confirmationCode: string;
}

export interface CheckInResponse {
  bookingId: number;
  confirmationCode: string;
  eventId: number;
  eventTitle: string;
  userDisplayName: string;
  userEmail: string;
  ticketTypeName?: string | null;
  checkedInAt: string;
  checkedInByName: string;
}

export async function getOrganizerDashboardApi() {
  const { data } = await apiClient.get<ApiResponse<OrganizerDashboardDto>>('/organizer/dashboard');
  return data.data;
}

export async function getMyEventsApi(params?: { status?: string; keyword?: string; page?: number; size?: number }) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<EventSummary>>>('/organizer/events', { params });
  return data.data;
}

export async function createOrganizerEventApi(request: EventUpsertRequest) {
  const { data } = await apiClient.post<ApiResponse<EventDetail>>('/organizer/events', request);
  return data.data;
}

export async function updateOrganizerEventApi(id: number, request: EventUpsertRequest) {
  const { data } = await apiClient.put<ApiResponse<EventDetail>>(`/organizer/events/${id}`, request);
  return data.data;
}

export async function publishOrganizerEventApi(id: number) {
  const { data } = await apiClient.patch<ApiResponse<EventDetail>>(`/organizer/events/${id}/publish`);
  return data.data;
}

export async function closeOrganizerEventApi(id: number) {
  const { data } = await apiClient.patch<ApiResponse<EventDetail>>(`/organizer/events/${id}/close`);
  return data.data;
}

export async function getOrganizerEventBookingsApi(id: number, page = 0, size = 20) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<AdminBookingItem>>>(
    `/organizer/events/${id}/bookings`, { params: { page, size } }
  );
  return data.data;
}

export function getOrganizerCsvUrl(eventId: number) {
  return `/api/v1/organizer/events/${eventId}/registrations.csv`;
}

export async function checkInApi(eventId: number, confirmationCode: string) {
  const { data } = await apiClient.post<ApiResponse<CheckInResponse>>(
    `/events/${eventId}/check-ins`, { confirmationCode }
  );
  return data.data;
}
