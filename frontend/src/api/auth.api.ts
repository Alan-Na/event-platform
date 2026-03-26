import { apiClient } from '@/api/axios';
import type { ApiResponse } from '@/types/common';
import type { AuthPayload, CurrentUser, LoginRequest, RegisterRequest, UpdateProfileRequest } from '@/types/auth';

export async function loginApi(payload: LoginRequest) {
  const { data } = await apiClient.post<ApiResponse<AuthPayload>>('/auth/login', payload);
  return data.data;
}

export async function registerApi(payload: RegisterRequest) {
  const { data } = await apiClient.post<ApiResponse<AuthPayload>>('/auth/register', payload);
  return data.data;
}

export async function getCurrentUserApi() {
  const { data } = await apiClient.get<ApiResponse<CurrentUser>>('/auth/me');
  return data.data;
}

export async function getProfileApi() {
  const { data } = await apiClient.get<ApiResponse<CurrentUser>>('/me/profile');
  return data.data;
}

export async function updateProfileApi(payload: UpdateProfileRequest) {
  const { data } = await apiClient.patch<ApiResponse<CurrentUser>>('/me/profile', payload);
  return data.data;
}
