import { apiClient } from '@/api/axios';
import type { ApiResponse, PageResponse } from '@/types/common';
import type { EventDetail, EventQuery, EventSummary } from '@/types/event';

export async function getEventsApi(params: EventQuery) {
  const { data } = await apiClient.get<ApiResponse<PageResponse<EventSummary>>>('/events', { params });
  return data.data;
}

export async function getFeaturedEventsApi(limit = 6) {
  const { data } = await apiClient.get<ApiResponse<EventSummary[]>>('/events/featured', { params: { limit } });
  return data.data;
}

export async function getEventDetailApi(id: string | number) {
  const { data } = await apiClient.get<ApiResponse<EventDetail>>(`/events/${id}`);
  return data.data;
}
