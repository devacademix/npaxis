import api from './auth';

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

const toNumber = (value: unknown, fallback = 0): number => {
  const converted = Number(value);
  return Number.isFinite(converted) ? converted : fallback;
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
      const response = await api.get('/users/user/me', authConfig());
      return normalizeUser(unwrapApiData<any>(response));
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
    const list = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.items)
      ? payload.items
      : [];

    return list.map(normalizePreceptor);
  },

  savePreceptor: async (userId: number | string, preceptorId: number | string): Promise<void> => {
    await api.post(`/students/student-${userId}/save-preceptor/${preceptorId}`, null, authConfig());
  },

  removeSavedPreceptor: async (
    userId: number | string,
    preceptorId: number | string
  ): Promise<{ serverSynced: boolean }> => {
    try {
      await api.delete(`/students/student-${userId}/save-preceptor/${preceptorId}`, authConfig());
      return { serverSynced: true };
    } catch (error: any) {
      const status = error?.response?.status;
      if (status === 404 || status === 405 || status === 501) {
        // Backend removal API may be unavailable; allow UI-only removal flow.
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
    const list = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.items)
      ? payload.items
      : [];

    return list.map(normalizePreceptor);
  },
};

export default studentService;
