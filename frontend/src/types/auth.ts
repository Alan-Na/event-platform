export interface CurrentUser {
  id: number;
  email: string;
  fullName: string;
  preferredLanguage: string;
  roles: string[];
  enabled: boolean;
}

export interface AuthPayload {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: CurrentUser;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  preferredLanguage: string;
}

export interface UpdateProfileRequest {
  fullName: string;
  preferredLanguage: string;
}
