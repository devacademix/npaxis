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

export interface PlatformSetting {
  key: string;
  value: unknown;
  description?: string;
  settingType?: string;
}

const unwrapApiData = <T>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response.data as T;
};

const TEMP_SETTINGS_KEY = 'npaxis.admin.settings.draft';

const authConfig = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) return {};
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

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

  getPlatformSettings: async (): Promise<PlatformSetting[]> => {
    const response = await api.get('/administration/settings', authConfig());
    const payload = unwrapApiData<any>(response);
    const list = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.content)
      ? payload.content
      : Array.isArray(payload?.items)
      ? payload.items
      : [];

    return list.map((item: any) => ({
      key: String(item?.key ?? ''),
      value: item?.value,
      description: item?.description ? String(item.description) : undefined,
      settingType: item?.settingType ? String(item.settingType) : undefined,
    }));
  },

  getPlatformSetting: async (key: string): Promise<PlatformSetting> => {
    const response = await api.get(`/administration/settings/${key}`, authConfig());
    const item = unwrapApiData<any>(response) || {};
    return {
      key: String(item?.key ?? key),
      value: item?.value,
      description: item?.description ? String(item.description) : undefined,
      settingType: item?.settingType ? String(item.settingType) : undefined,
    };
  },

  updatePlatformSetting: async (key: string, value: unknown) => {
    const response = await api.put(`/administration/settings/${key}`, { value }, authConfig());
    return unwrapApiData(response);
  },

  getTemporarySettings: <T>() => {
    const raw = localStorage.getItem(TEMP_SETTINGS_KEY);
    if (!raw) return null as T | null;

    try {
      return JSON.parse(raw) as T;
    } catch {
      localStorage.removeItem(TEMP_SETTINGS_KEY);
      return null as T | null;
    }
  },

  saveTemporarySettings: async (settings: unknown) => {
    // Placeholder storage as backend settings APIs are not yet available.
    localStorage.setItem(TEMP_SETTINGS_KEY, JSON.stringify(settings));
    return true;
  },
};

export default settingsService;
