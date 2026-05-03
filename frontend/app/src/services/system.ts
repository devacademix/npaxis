import api from './auth';

export interface SystemHealth {
  service?: string;
  status?: string;
  auth?: string;
  health?: string;
}

const unwrapApiData = <T>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response.data as T;
};

export const systemService = {
  getHealth: async (): Promise<SystemHealth> => {
    const response = await api.get('/');
    return unwrapApiData<SystemHealth>(response);
  },
};

export default systemService;
