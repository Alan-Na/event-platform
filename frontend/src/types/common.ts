export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiValidationError {
  success: boolean;
  code: string;
  message: string;
  errors: Array<{ field: string; message: string }>;
  timestamp: string;
}
