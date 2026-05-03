import api from './auth';

export interface AdminCurrentUser {
  userId: number;
  username: string;
  name: string;
  email: string;
}

export interface AdminUpdateRequest {
  fullName: string;
  username: string;
  password: string;
  email: string;
  roles: number[];
}

export interface SystemSetting {
  settingKey: string;
  value: any;
  description?: string;
  isActive?: boolean;
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

const normalizeAdminUser = (payload: any, fallbackUserId?: number | null): AdminCurrentUser => {
  const userId = Number(payload?.userId ?? payload?.id ?? fallbackUserId ?? 0);
  const email = String(payload?.email ?? '').trim();
  const displayName = String(payload?.name ?? payload?.displayName ?? '').trim();
  const username = String(payload?.username ?? '').trim() || (email ? email.split('@')[0] : '');

  return {
    userId: Number.isFinite(userId) ? userId : 0,
    username,
    name: displayName,
    email,
  };
};

export const settingsService = {
  getCurrentAdmin: async (): Promise<AdminCurrentUser> => {
    const fallbackUserIdRaw = localStorage.getItem('userId');
    const fallbackUserId = fallbackUserIdRaw ? Number(fallbackUserIdRaw) : null;

    try {
      const response = await api.get('/users/user/me', authConfig());
      return normalizeAdminUser(unwrapApiData<any>(response), fallbackUserId);
    } catch (meError) {
      if (!fallbackUserId || Number.isNaN(fallbackUserId)) {
        throw meError;
      }
      const fallbackResponse = await api.get(`/users/active/user-${fallbackUserId}`, authConfig());
      return normalizeAdminUser(unwrapApiData<any>(fallbackResponse), fallbackUserId);
    }
  },

  updateAdminDetails: async (userId: number, payload: AdminUpdateRequest) => {
    const response = await api.put(`/users/user-${userId}`, payload, authConfig());
    return unwrapApiData(response);
  },

  getAllSettings: async (): Promise<SystemSetting[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/settings`, authConfig());
    return unwrapApiData<SystemSetting[]>(response) || [];
  },

  getSettingByKey: async (key: string): Promise<SystemSetting> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/settings/${key}`, authConfig());
    return unwrapApiData<SystemSetting>(response);
  },

  updateSetting: async (key: string, value: any): Promise<SystemSetting> => {
    const response = await api.put(`${ADMIN_API_PREFIX}/settings/${key}`, { value }, authConfig());
    return unwrapApiData<SystemSetting>(response);
  },
};

export default settingsService;
