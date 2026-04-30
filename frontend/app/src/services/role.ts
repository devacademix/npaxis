import api from './auth';

export interface RoleSummary {
  roleId: number;
  roleName: string;
  description?: string;
}

export interface RoleDetail extends RoleSummary {
  permissions?: string[];
  createdAt?: string;
  updatedAt?: string;
}

const authConfig = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) return {};
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
};

const unwrapData = <T>(response: any): T => {
  if (response?.data?.data !== undefined) return response.data.data as T;
  return response.data as T;
};

export const roleService = {
  getAllRoles: async (): Promise<RoleSummary[]> => {
    const response = await api.get('/roles/active/all', authConfig());
    return unwrapData<RoleSummary[]>(response) || [];
  },

  getRoleDetail: async (roleId: number | string): Promise<RoleDetail> => {
    const response = await api.get(`/roles/active/role-${roleId}`, authConfig());
    return unwrapData<RoleDetail>(response);
  },
};

export default roleService;
