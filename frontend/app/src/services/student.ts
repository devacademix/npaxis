import api, { authService } from './auth';

export interface StudentUser {
  userId: number;
  displayName: string;
  email: string;
  role?: string;
}

export interface StudentProfile {
  userId: number;
  displayName: string;
  email: string;
  university?: string;
  program?: string;
  graduationYear?: string;
  phone?: string;
  inquiriesSent?: number;
  recentlyViewed?: number;
  savedPreceptors?: number;
  isDeleted?: boolean;
  deleted?: boolean;
}

export interface StudentPreceptor {
  userId: number;
  displayName: string;
  specialty?: string;
  location?: string;
  credentials?: string;
  setting?: string;
  honorarium?: string;
  requirements?: string;
  isVerified?: boolean;
  isPremium?: boolean;
}

export interface AdminStudentListItem extends StudentProfile {
  createdAt?: string;
}

export interface StudentInquirySummary {
  inquiryId?: number;
  subject?: string;
  message?: string;
  status?: string;
  createdAt?: string;
}

export interface StudentPaginatedResponse<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

const unwrapApiData = <T>(response: any): T => {
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

const ADMIN_API_PREFIX = '/api/v1/administration';
const SAVED_PRECEPTOR_REMOVALS_KEY = 'npaxis:hidden-saved-preceptors';

const toNumber = (value: unknown, fallback = 0): number => {
  const converted = Number(value);
  return Number.isFinite(converted) ? converted : fallback;
};

const extractPageItems = <T>(payload: any): T[] => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  return [];
};

const getSavedRemovalStorageKey = (userId: number | string) =>
  `${SAVED_PRECEPTOR_REMOVALS_KEY}:${String(userId)}`;

const getHiddenSavedPreceptorIds = (userId: number | string): number[] => {
  if (typeof window === 'undefined') return [];
  const rawValue = window.localStorage.getItem(getSavedRemovalStorageKey(userId));
  if (!rawValue) return [];

  try {
    const parsed = JSON.parse(rawValue);
    if (!Array.isArray(parsed)) return [];
    return parsed
      .map((value) => Number(value))
      .filter((value) => Number.isFinite(value));
  } catch {
    return [];
  }
};

const setHiddenSavedPreceptorIds = (userId: number | string, ids: number[]) => {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(getSavedRemovalStorageKey(userId), JSON.stringify(ids));
};

const hideSavedPreceptorLocally = (userId: number | string, preceptorId: number | string) => {
  const normalizedId = Number(preceptorId);
  if (!Number.isFinite(normalizedId)) return;

  const nextIds = Array.from(new Set([...getHiddenSavedPreceptorIds(userId), normalizedId]));
  setHiddenSavedPreceptorIds(userId, nextIds);
};

const restoreSavedPreceptorLocally = (userId: number | string, preceptorId: number | string) => {
  const normalizedId = Number(preceptorId);
  if (!Number.isFinite(normalizedId)) return;

  const nextIds = getHiddenSavedPreceptorIds(userId).filter((id) => id !== normalizedId);
  setHiddenSavedPreceptorIds(userId, nextIds);
};

const normalizeUser = (payload: any): StudentUser => ({
  userId: toNumber(payload?.userId ?? payload?.id),
  displayName: String(payload?.displayName ?? payload?.name ?? payload?.username ?? 'Student'),
  email: String(payload?.email ?? ''),
  role: payload?.role ? String(payload.role) : undefined,
});

const normalizeStudentProfile = (payload: any): StudentProfile => ({
  userId: toNumber(payload?.userId ?? payload?.id),
  displayName: String(payload?.displayName ?? payload?.name ?? ''),
  email: String(payload?.email ?? ''),
  university: payload?.university ? String(payload.university) : undefined,
  program: payload?.program ? String(payload.program) : undefined,
  graduationYear: payload?.graduationYear ? String(payload.graduationYear) : undefined,
  phone: payload?.phone ? String(payload.phone) : undefined,
  inquiriesSent: toNumber(payload?.inquiriesSent ?? payload?.inquiries ?? payload?.totalInquiries),
  recentlyViewed: toNumber(payload?.recentlyViewed ?? payload?.recentViews),
  savedPreceptors: toNumber(payload?.savedPreceptors),
  isDeleted: Boolean(payload?.isDeleted ?? payload?.deleted),
  deleted: Boolean(payload?.isDeleted ?? payload?.deleted),
});

const normalizePreceptor = (payload: any): StudentPreceptor => ({
  userId: toNumber(payload?.userId ?? payload?.id),
  displayName: String(payload?.displayName ?? payload?.name ?? 'Preceptor'),
  specialty: payload?.specialty ? String(payload.specialty) : undefined,
  location: payload?.location ? String(payload.location) : undefined,
  credentials: payload?.credentials ? String(payload.credentials) : undefined,
  setting: payload?.setting ? String(payload.setting) : undefined,
  honorarium: payload?.honorarium ? String(payload.honorarium) : undefined,
  requirements: payload?.requirements ? String(payload.requirements) : undefined,
  isVerified: Boolean(payload?.isVerified),
  isPremium: Boolean(payload?.isPremium),
});

export const studentService = {
  getLoggedInUser: async (): Promise<StudentUser> => {
    const fallbackUserIdRaw = localStorage.getItem('userId');
    const fallbackUserId = fallbackUserIdRaw ? Number(fallbackUserIdRaw) : null;

    try {
      return normalizeUser(await authService.getCurrentUserCached());
    } catch (error) {
      if (!fallbackUserId || Number.isNaN(fallbackUserId)) {
        throw error;
      }
      const response = await api.get(`/users/active/user-${fallbackUserId}`, authConfig());
      return normalizeUser(unwrapApiData<any>(response));
    }
  },

  getStudentProfile: async (userId: number | string): Promise<StudentProfile> => {
    const response = await api.get(`/students/active/student-${userId}`, authConfig());
    return normalizeStudentProfile(unwrapApiData<any>(response));
  },

  getSavedPreceptors: async (userId: number | string): Promise<StudentPreceptor[]> => {
    const response = await api.get(`/students/student-${userId}/saved`, authConfig());
    const payload = unwrapApiData<any>(response);
    const hiddenIds = new Set(getHiddenSavedPreceptorIds(userId));
    return extractPageItems<any>(payload)
      .map(normalizePreceptor)
      .filter((preceptor) => !hiddenIds.has(preceptor.userId));
  },

  savePreceptor: async (userId: number | string, preceptorId: number | string): Promise<void> => {
    await api.post(`/students/student-${userId}/save-preceptor/${preceptorId}`, null, authConfig());
    restoreSavedPreceptorLocally(userId, preceptorId);
  },

  removeSavedPreceptor: async (
    userId: number | string,
    preceptorId: number | string
  ): Promise<{ serverSynced: boolean }> => {
    try {
      await api.delete(`/students/student-${userId}/save-preceptor/${preceptorId}`, authConfig());
      restoreSavedPreceptorLocally(userId, preceptorId);
      return { serverSynced: true };
    } catch (error: any) {
      const status = error?.response?.status;
      const message = String(error?.message || error?.response?.data?.message || '').toLowerCase();
      const deleteNotSupported =
        message.includes('request method') && message.includes('delete') && message.includes('not supported');

      if (status === 404 || status === 405 || status === 501 || deleteNotSupported) {
        hideSavedPreceptorLocally(userId, preceptorId);
        return { serverSynced: false };
      }
      throw error;
    }
  },

  searchPreceptors: async (limit = 8): Promise<StudentPreceptor[]> => {
    const response = await api.get('/preceptors/search', {
      ...authConfig(),
      params: { page: 0, size: limit },
    });
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(normalizePreceptor);
  },

  getActiveStudents: async (): Promise<StudentProfile[]> => {
    const response = await api.get('/students/active/all', authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(normalizeStudentProfile);
  },

  updateStudentDetails: async (userId: number | string, payload: Record<string, any>) => {
    const response = await api.put(`/students/student-${userId}`, payload, authConfig());
    return normalizeStudentProfile(unwrapApiData<any>(response));
  },

  softDeleteStudent: async (userId: number | string) => {
    const response = await api.delete(`/students/soft-delete/student-${userId}`, authConfig());
    return unwrapApiData(response);
  },

  hardDeleteStudent: async (userId: number | string) => {
    const response = await api.delete(`/students/hard-delete/student-${userId}`, authConfig());
    return unwrapApiData(response);
  },

  restoreStudent: async (userId: number | string) => {
    const response = await api.put(`/students/restore/student-${userId}`, null, authConfig());
    return unwrapApiData(response);
  },

  getAdminStudents: async (params?: { page?: number; size?: number }): Promise<AdminStudentListItem[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/students/list`, {
      ...authConfig(),
      params,
    });
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(normalizeStudentProfile);
  },

  getAdminStudentsPaginated: async (params?: { page?: number; size?: number }): Promise<StudentPaginatedResponse<AdminStudentListItem>> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/students/list`, {
      ...authConfig(),
      params,
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<any>(payload).map(normalizeStudentProfile),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? params?.page ?? 0),
      size: Number(meta?.size ?? params?.size ?? 10),
    };
  },

  searchAdminStudents: async (filters?: { university?: string; program?: string; page?: number; size?: number }): Promise<StudentPaginatedResponse<AdminStudentListItem>> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/students/search`, {
      ...authConfig(),
      params: filters,
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<any>(payload).map(normalizeStudentProfile),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? filters?.page ?? 0),
      size: Number(meta?.size ?? filters?.size ?? 10),
    };
  },

  getAdminStudentDetail: async (userId: number | string): Promise<StudentProfile> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/students/detail-${userId}`, authConfig());
    return normalizeStudentProfile(unwrapApiData<any>(response));
  },

  updateAdminStudentDetail: async (userId: number | string, payload: Record<string, any>): Promise<StudentProfile> => {
    const response = await api.put(`${ADMIN_API_PREFIX}/students/update-${userId}`, payload, authConfig());
    return normalizeStudentProfile(unwrapApiData<any>(response));
  },

  deleteAdminStudent: async (userId: number | string) => {
    const response = await api.delete(`${ADMIN_API_PREFIX}/students/update-${userId}`, authConfig());
    return unwrapApiData(response);
  },

  getAdminStudentInquiries: async (userId: number | string): Promise<StudentInquirySummary[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/students/detail-${userId}/inquiries`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<StudentInquirySummary>(payload);
  },
};

export default studentService;
