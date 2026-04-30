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

export interface UserUpdatePayload {
  fullName?: string;
  username?: string;
  password?: string;
  email?: string;
  roles?: number[];
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
  getUsers: async (): Promise<UserRecord[]> => {
    const response = await api.get('/users', authConfig());
    return unwrapApiData<UserRecord[]>(response) || [];
  },

  getActiveUsers: async (): Promise<UserRecord[]> => {
    const response = await api.get('/users/active/all', authConfig());
    return unwrapApiData<UserRecord[]>(response) || [];
  },

  getAllUsers: async (): Promise<UserRecord[]> => {
    const response = await api.get('/users/all', authConfig());
    return unwrapApiData<UserRecord[]>(response) || [];
  },

  getDeletedUsers: async (): Promise<UserRecord[]> => {
    const response = await api.get('/users/deleted/all', authConfig());
    return unwrapApiData<UserRecord[]>(response) || [];
  },

  getDeletedUserById: async (userId: number) => {
    const response = await api.get(`/users/deleted/user-${userId}`, authConfig());
    return unwrapApiData<UserRecord>(response);
  },

  updateUser: async (userId: number, payload: UserUpdatePayload) => {
    const response = await api.put(`/users/user-${userId}`, payload, authConfig());
    return unwrapApiData<UserRecord>(response);
  },

  uploadProfilePicture: async (userId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.put(`/users/user-${userId}/upload-profile-picture`, formData, {
      ...authConfig(),
      headers: {
        ...(authConfig() as any)?.headers,
        'Content-Type': 'multipart/form-data',
      },
    });
    return unwrapApiData<UserRecord>(response);
  },

  getProfilePictureBlob: async (userId: number) => {
    const response = await api.get(`/users/user-${userId}/profile-picture`, {
      ...authConfig(),
      responseType: 'blob',
    });
    return response.data as Blob;
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
