export interface AuthResponse {
  userId: number;
  displayName: string;
  email: string;
  accessToken: string;
  role: 'ADMIN' | 'STUDENT' | 'PRECEPTOR' | 'ROLE_ADMIN' | 'ROLE_STUDENT' | 'ROLE_PRECEPTOR';
}

export interface UserResponse {
  userId: number;
  displayName: string;
  email: string;
  role: string;
}

export interface GenericApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
  timestamp: string;
}
