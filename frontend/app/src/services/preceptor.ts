import type { AxiosProgressEvent } from 'axios';
import api from './auth';

export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | string;

export interface PreceptorProfile {
  userId: number;
  displayName?: string;
  credentials?: string;
  specialty?: string;
  location?: string;
  setting?: string;
  availableDays?: string[];
  honorarium?: string;
  requirements?: string;
  isVerified?: boolean;
  isPremium?: boolean;
  phone?: string;
  licenseNumber?: string;
  licenseState?: string;
  licenseFileUrl?: string;
  verificationStatus?: VerificationStatus;
  verificationSubmittedAt?: string;
  verificationReviewedAt?: string;
}

export interface PreceptorStatsResponse {
  profileViews?: number;
  contactReveals?: number;
  inquiries?: number;
}

export interface PreceptorContact {
  phone?: string;
  email?: string;
}

export interface LicenseDocumentInfo {
  downloadUrl: string;
  viewUrl: string;
}

export type AnalyticsEventType = 'PROFILE_VIEW' | 'CONTACT_REVEAL' | 'INQUIRY';

export interface PreceptorSearchFilters {
  specialty?: string;
  location?: string;
  availableDays?: string[];
  page?: number;
  size?: number;
}

export interface PreceptorSearchItem {
  userId: number;
  displayName: string;
  credentials?: string;
  specialty?: string;
  location?: string;
  setting?: string;
  honorarium?: string;
  requirements?: string;
  isVerified: boolean;
  isPremium: boolean;
  verificationStatus?: VerificationStatus;
}

export interface PreceptorSearchResult {
  items: PreceptorSearchItem[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
}

export interface LoggedInPreceptorUser {
  userId: number;
  displayName: string;
  email: string;
  username?: string;
}

interface SubmitLicensePayload {
  file: File;
  licenseNumber?: string;
  licenseState?: string;
}

export interface PreceptorUpdatePayload {
  name: string;
  credentials: string;
  specialty: string;
  location: string;
  setting: string;
  availableDays: string[];
  honorarium: string;
  requirements: string;
  email: string;
  phone: string;
  licenseNumber: string;
  licenseState: string;
  licenseFileUrl: string;
}

const extractData = <T>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response.data as T;
};

const authConfig = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) return {};
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

const normalizeUser = (payload: any): LoggedInPreceptorUser => ({
  userId: Number(payload?.userId ?? payload?.id ?? 0),
  displayName: String(payload?.displayName ?? payload?.name ?? payload?.username ?? ''),
  email: String(payload?.email ?? ''),
  username: payload?.username ? String(payload.username) : undefined,
});

const normalizeSearchItem = (payload: any): PreceptorSearchItem => ({
  userId: Number(payload?.userId ?? payload?.id ?? 0),
  displayName: String(payload?.displayName ?? payload?.name ?? 'Preceptor'),
  credentials: payload?.credentials ? String(payload.credentials) : undefined,
  specialty: payload?.specialty ? String(payload.specialty) : undefined,
  location: payload?.location ? String(payload.location) : undefined,
  setting: payload?.setting ? String(payload.setting) : undefined,
  honorarium: payload?.honorarium ? String(payload.honorarium) : undefined,
  requirements: payload?.requirements ? String(payload.requirements) : undefined,
  isVerified: Boolean(payload?.isVerified),
  isPremium: Boolean(payload?.isPremium),
  verificationStatus: payload?.verificationStatus ? String(payload.verificationStatus) as VerificationStatus : undefined,
});

export const preceptorService = {
  searchPreceptors: async (filters: PreceptorSearchFilters = {}): Promise<PreceptorSearchResult> => {
    const response = await api.get('/preceptors/search', {
      ...authConfig(),
      params: {
        ...(filters.specialty ? { specialty: filters.specialty } : {}),
        ...(filters.location ? { location: filters.location } : {}),
        ...(filters.availableDays && filters.availableDays.length > 0 ? { availableDays: filters.availableDays } : {}),
        page: filters.page ?? 0,
        size: filters.size ?? 12,
      },
    });

    const payload = response?.data ?? {};
    const list = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload?.data?.items)
      ? payload.data.items
      : [];
    const meta = payload?.meta ?? {};

    return {
      items: list.map(normalizeSearchItem),
      page: Number(meta?.page ?? filters.page ?? 0),
      size: Number(meta?.size ?? filters.size ?? list.length ?? 0),
      totalPages: Number(meta?.totalPages ?? 1),
      totalElements: Number(meta?.totalElements ?? list.length ?? 0),
    };
  },

  getLoggedInUser: async (): Promise<LoggedInPreceptorUser> => {
    const fallbackUserId = localStorage.getItem('userId');
    try {
      const response = await api.get('/users/user/me', authConfig());
      return normalizeUser(extractData<any>(response));
    } catch (error) {
      if (!fallbackUserId) {
        throw error;
      }
      const response = await api.get(`/users/active/user-${fallbackUserId}`, authConfig());
      return normalizeUser(extractData<any>(response));
    }
  },

  getStats: async (id: number | string) => {
    try {
      const response = await api.get(`/analytics/preceptors/${id}/stats`, authConfig());
      return extractData<PreceptorStatsResponse>(response);
    } catch (error: any) {
      throw error;
    }
  },

  getPreceptorById: async (id: number | string): Promise<PreceptorProfile> => {
    const response = await api.get(`/preceptors/active/preceptor-${id}`, authConfig());
    return extractData<PreceptorProfile>(response);
  },

  revealContact: async (id: number | string): Promise<PreceptorContact> => {
    const response = await api.post(`/preceptors/preceptor-${id}/reveal-contact`, null, authConfig());
    return extractData<PreceptorContact>(response);
  },

  trackAnalyticsEvent: async (eventType: AnalyticsEventType, preceptorId: number | string): Promise<void> => {
    try {
      await api.post('/analytics/event', { eventType, preceptorId }, authConfig());
    } catch {
      // Analytics should never block the primary student workflow.
    }
  },

  updatePreceptorProfile: async (
    id: number | string,
    payload: PreceptorUpdatePayload
  ): Promise<PreceptorProfile> => {
    const response = await api.put(`/preceptors/preceptor-${id}`, payload, authConfig());
    return extractData<PreceptorProfile>(response);
  },

  submitLicense: async (
    id: number | string,
    payload: SubmitLicensePayload,
    onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
  ): Promise<PreceptorProfile> => {
    const requestConfig = authConfig() as { headers?: Record<string, string> };
    const formData = new FormData();
    // Keep compatibility with both payload field names used by different backend handlers.
    formData.append('licenseFile', payload.file);
    formData.append('file', payload.file);
    if (payload.licenseNumber) {
      formData.append('licenseNumber', payload.licenseNumber);
    }
    if (payload.licenseState) {
      formData.append('licenseState', payload.licenseState);
    }

    const response = await api.post(
      `/preceptors/preceptor-${id}/submit-license`,
      formData,
      {
        ...requestConfig,
        headers: {
          ...(requestConfig.headers ?? {}),
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress,
      }
    );
    return extractData<PreceptorProfile>(response);
  },

  getLicenseLinks: (id: number | string): LicenseDocumentInfo => ({
    downloadUrl: `/api/v1/preceptors/preceptor-${id}/license`,
    viewUrl: `/api/v1/preceptors/preceptor-${id}/license/view`,
  }),

  verifyPreceptor: async (id: number | string) => {
    const response = await api.put(`/preceptors/verify/preceptor-${id}`, null, authConfig());
    return extractData<PreceptorProfile>(response);
  },

  restorePreceptor: async (id: number | string) => {
    const response = await api.put(`/preceptors/restore/preceptor-${id}`, null, authConfig());
    return extractData<PreceptorProfile>(response);
  },

  softDeletePreceptor: async (id: number | string) => {
    const response = await api.delete(`/preceptors/soft-delete/preceptor-${id}`, authConfig());
    return extractData<PreceptorProfile>(response);
  },

  hardDeletePreceptor: async (id: number | string) => {
    const response = await api.delete(`/preceptors/hard-delete/preceptor-${id}`, authConfig());
    return extractData<PreceptorProfile>(response);
  },
};
