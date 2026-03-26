import { apiClient } from '@/api/axios';
import type { BookingActionResponse, MyBookingItem } from '@/types/booking';
import type { ApiResponse, PageResponse } from '@/types/common';

export async function createBookingApi(eventId: number) {
  const { data } = await apiClient.post<ApiResponse<BookingActionResponse>>(`/events/${eventId}/bookings`);
  return data.data;
}

export async function cancelBookingApi(eventId: number) {
  const { data } = await apiClient.delete<ApiResponse<BookingActionResponse>>(`/events/${eventId}/bookings/me`);
  return data.data;
}

export async function getMyBookingsApi(status = 'ALL', page = 0, size = 10) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<MyBookingItem>>>('/me/bookings', {
    params: { status, page, size }
  });
  return data.data;
}
