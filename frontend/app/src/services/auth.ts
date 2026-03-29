import axios, { type AxiosError } from 'axios';
import type { AuthResponse, UserResponse } from '../types';

const api = axios.create({
  baseURL: '/api/v1',
  withCredentials: true, // For refresh token cookie
});

const clearAuthStorage = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('role');
  localStorage.removeItem('userId');
  localStorage.removeItem('displayName');
};

const unwrapApiData = <T>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response.data as T;
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token && config.headers && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor for generic error handling could be added here
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<any>) => {
    const originalRequest = error.config as (typeof error.config & { _retry?: boolean }) | undefined;
    const statusCode = error.response?.status;
    const requestUrl = originalRequest?.url || '';
    const isAuthCall =
      requestUrl.includes('/auth/login') ||
      requestUrl.includes('/auth/refresh-token') ||
      requestUrl.includes('/auth/logout');

    if (statusCode === 401 && originalRequest && !originalRequest._retry && !isAuthCall) {
      originalRequest._retry = true;
      try {
        const refreshResponse = await api.post('/auth/refresh-token');
        const refreshed = unwrapApiData<AuthResponse>(refreshResponse);

        if (refreshed?.accessToken) {
          localStorage.setItem('accessToken', refreshed.accessToken);
          localStorage.setItem('role', refreshed.role);
          localStorage.setItem('userId', String(refreshed.userId));
          localStorage.setItem('displayName', refreshed.displayName);

          originalRequest.headers = originalRequest.headers ?? {};
          originalRequest.headers.Authorization = `Bearer ${refreshed.accessToken}`;
          return api(originalRequest);
        }
      } catch {
        clearAuthStorage();
        if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
        return Promise.reject({
          ...error,
          message: 'Session expired. Please login again.',
        });
      }
    }

    // Standardize error messages across JSON, plain-text, and network failures.
    let message = 'An unexpected error occurred';
    const responseData = error.response?.data;

    if (typeof responseData === 'string') {
      const trimmed = responseData.trim();
      if (trimmed.startsWith('<')) {
        message = `Request failed with status ${error.response?.status ?? 'unknown'}.`;
      } else {
        try {
          const parsed = JSON.parse(trimmed);
          message = parsed?.error || parsed?.message || trimmed || message;
        } catch {
          message = trimmed || message;
        }
      }
    } else if (responseData && typeof responseData === 'object') {
      message = responseData.error || responseData.message || message;

      if (Array.isArray(responseData.validationErrors) && responseData.validationErrors.length > 0) {
        message = responseData.validationErrors[0] || message;
      } else if (responseData.errors && typeof responseData.errors === 'object') {
        const fieldKeys = Object.keys(responseData.errors);
        if (fieldKeys.length > 0) {
          message = responseData.errors[fieldKeys[0]];
        }
      }
    }

    if (!error.response) {
      if (error.message === 'Network Error') {
        message = 'Unable to reach the server. Please ensure backend is running on port 8080.';
      } else if (error.message) {
        message = error.message;
      }
    }

    const normalizedMessage = message.toLowerCase();
    if (
      normalizedMessage.includes('jwt expired') ||
      normalizedMessage.includes('token expired') ||
      normalizedMessage.includes('session expired')
    ) {
      clearAuthStorage();
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      return Promise.reject({
        ...error,
        message: 'Session expired. Please login again.',
      });
    }
    
    return Promise.reject({ ...error, message });
  }
);

export const authService = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', { email, password });
    return unwrapApiData<AuthResponse>(response);
  },

  register: async (registrationData: any) => {
    const response = await api.post('/auth/register', registrationData);
    return unwrapApiData(response);
  },

  verifyOtp: async (email: string, otp: string): Promise<AuthResponse> => {
    const response = await api.post('/auth/verify-otp', { email, otp });
    return unwrapApiData<AuthResponse>(response);
  },

  forgotPassword: async (email: string) => {
    const response = await api.post('/auth/forgot-password', { email });
    return unwrapApiData(response);
  },

  resendOtp: async (email: string) => {
    // Reuses existing OTP mail endpoint for resend flow.
    const response = await api.post('/auth/forgot-password', { email });
    return unwrapApiData<string>(response);
  },

  resetPassword: async (email: string, newPassword: string) => {
    const response = await api.post('/auth/reset-password', { email, password: newPassword });
    return unwrapApiData(response);
  },

  logout: async () => {
    await api.post('/auth/logout');
    clearAuthStorage();
  },

  getCurrentUser: async (): Promise<UserResponse> => {
    const response = await api.get('/users/user/me', {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`
      }
    });
    return unwrapApiData<UserResponse>(response);
  }
};

export default api;
