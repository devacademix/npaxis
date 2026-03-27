import api from './auth';

export interface UserRecord {
  userId: number;
  displayName: string;
  email: string;
  role: string;
  isEnabled?: boolean;
  enabled?: boolean;
  accountEnabled?: boolean;
  isDeleted?: boolean;
  deleted?: boolean;
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

export const userService = {
  getAllUsers: async (): Promise<UserRecord[]> => {
    const response = await api.get('/users/all', authConfig());
    return unwrapApiData<UserRecord[]>(response) || [];
  },

  toggleAccountStatus: async (userId: number, enabled: boolean) => {
    const response = await api.put(
      `/administration/user-${userId}/toggle-account?enabled=${enabled}`,
      null,
      authConfig()
    );
    return unwrapApiData<string | null>(response);
  },

  softDeleteUser: async (userId: number) => {
    const response = await api.delete(`/users/soft-delete/user-${userId}`, authConfig());
    return unwrapApiData<string | null>(response);
  },

  restoreUser: async (userId: number) => {
    const response = await api.put(`/users/restore/user-${userId}`, null, authConfig());
    return unwrapApiData<string | null>(response);
  },

  hardDeleteUser: async (userId: number) => {
    const response = await api.delete(`/users/hard-delete/user-${userId}`, authConfig());
    return unwrapApiData<string | null>(response);
  },
};

export default userService;
