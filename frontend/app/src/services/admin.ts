import api from './auth';

export interface PendingPreceptorView {
  id: number | string;
  name: string;
  email: string;
  credentials?: string;
  licenseNumber: string;
  licenseFileUrl?: string;
  dateSubmitted: string;
  submittedAtRaw: string | null;
  status: string;
  avatarUrl?: string;
}

export interface AdminUser {
  userId: number;
  displayName: string;
  email: string;
  role: string;
}

export interface RevenueStatsApi {
  totalUsers?: number;
  premiumCount?: number;
  premiumUsers?: number;
  revenue?: number;
  monthlyRevenue?: number;
  totalTransactions?: number;
  totalRevenue?: number;
  successfulTransactions?: number;
  failedTransactions?: number;
}

export interface PaymentHistoryItem {
  id: string | number;
  preceptorName: string;
  amount: number;
  status: string;
  date: string;
  invoiceUrl?: string;
}

export interface PreceptorAnalytics {
  profileViews?: number;
  contactReveals?: number;
  inquiries?: number;
  responseRate?: number;
  conversionRate?: number;
  successfulTransactions?: number;
  totalTransactions?: number;
  revenueTrend?: Array<{ label: string; value: number }>;
  transactionTrend?: Array<{ label: string; value: number }>;
}

export interface DashboardStats {
  totalUsers: number;
  premiumUsers: number;
  revenue: number;
  activePreceptors: number;
}

export interface AdminPreceptorDetail {
  userId: number;
  displayName: string;
  email: string;
  credentials?: string;
  specialty?: string;
  location?: string;
  phone?: string;
  honorarium?: string;
  requirements?: string;
  isVerified?: boolean;
  isPremium?: boolean;
  verificationStatus?: string;
}

export interface VerificationHistoryItem {
  auditId: number;
  previousStatus?: string;
  newStatus?: string;
  reviewerUserId?: number;
  reviewNote?: string;
  changeTimestamp?: string;
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

const buildPaginationConfig = (params?: Record<string, any>) => {
  if (!params) return {};
  return { params };
};

const extractPageItems = <T>(payload: any): T[] => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  return [];
};

const formatDate = (value?: string): string => {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'N/A';
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
  });
};

const normalizeStatus = (value?: string): string => {
  if (!value) return 'Pending Verification';
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
};

const mapPendingPreceptor = (item: any): PendingPreceptorView => ({
  id: item?.userId ?? item?.id ?? '',
  name: item?.displayName || item?.user?.displayName || 'Unknown Preceptor',
  email: item?.email || item?.user?.email || 'N/A',
  credentials: item?.credentials || 'N/A',
  licenseNumber: item?.licenseNumber || 'N/A',
  licenseFileUrl: item?.licenseFileUrl || undefined,
  dateSubmitted: formatDate(item?.verificationSubmittedAt),
  submittedAtRaw: item?.verificationSubmittedAt ?? null,
  status: normalizeStatus(item?.verificationStatus),
});

const mapRevenueTransaction = (item: any, index: number): PaymentHistoryItem => ({
  id: item?.transactionId ?? item?.id ?? index,
  preceptorName: item?.preceptorName ?? item?.displayName ?? `Preceptor ${index + 1}`,
  amount: Number(item?.amountInMinorUnits ?? item?.amount ?? 0) / 100,
  status: String(item?.status ?? 'PENDING'),
  date: String(item?.transactionAt ?? item?.createdAt ?? ''),
  invoiceUrl: item?.invoiceUrl ?? undefined,
});

const mapDashboardStats = (payload: any): DashboardStats => ({
  totalUsers: Number(payload?.totalUsers ?? 0),
  premiumUsers: Number(payload?.verifiedPreceptors ?? payload?.premiumUsers ?? payload?.premiumCount ?? 0),
  revenue: Number(payload?.revenueThisMonth ?? payload?.totalRevenue ?? 0),
  activePreceptors: Number(payload?.totalPreceptors ?? 0),
});

export const adminService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/dashboard`, authConfig());
    return mapDashboardStats(unwrapApiData<any>(response));
  },

  getDashboardOverview: async () => {
    const response = await api.get(`${ADMIN_API_PREFIX}/analytics/overview`, authConfig());
    return unwrapApiData<any>(response);
  },

  getTopPreceptorsOverview: async () => {
    const response = await api.get(`${ADMIN_API_PREFIX}/analytics/top-preceptors`, authConfig());
    return unwrapApiData<any>(response);
  },

  getTrendOverview: async () => {
    const response = await api.get(`${ADMIN_API_PREFIX}/analytics/trends`, authConfig());
    return unwrapApiData<any>(response);
  },

  getPendingPreceptors: async (params?: { page?: number; size?: number }): Promise<PendingPreceptorView[]> => {
    const response = await api.get(
      `${ADMIN_API_PREFIX}/preceptors/pending`,
      { ...authConfig(), ...buildPaginationConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(mapPendingPreceptor);
  },

  getAllUsers: async (): Promise<AdminUser[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/users`, authConfig());
    return unwrapApiData<AdminUser[]>(response) || [];
  },

  getRevenueStats: async (): Promise<RevenueStatsApi> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/revenue/summary`, authConfig());
    const payload = unwrapApiData<any>(response) || {};
    return {
      totalRevenue: Number(payload?.totalRevenue ?? 0),
      monthlyRevenue: Number(payload?.revenueThisMonth ?? 0),
      totalTransactions: Number(payload?.totalTransactions ?? 0),
      premiumCount: Number(payload?.successfulTransactions ?? 0),
      revenue: Number(payload?.totalRevenue ?? 0),
      successfulTransactions: Number(payload?.successfulTransactions ?? 0),
      failedTransactions: Number(payload?.failedTransactions ?? 0),
    };
  },

  getPaymentHistory: async (_preceptorId?: string | number): Promise<PaymentHistoryItem[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/revenue/transactions`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(mapRevenueTransaction);
  },

  getRevenueByPreceptor: async (): Promise<any[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/revenue/by-preceptor`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload);
  },

  getPreceptorAnalytics: async (id: string | number): Promise<PreceptorAnalytics> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/${id}/analytics`, authConfig());
    return unwrapApiData<PreceptorAnalytics>(response) || {};
  },

  approvePreceptor: async (id: number | string) => {
    const response = await api.post(`${ADMIN_API_PREFIX}/preceptors/approve-${id}`, null, authConfig());
    return unwrapApiData(response);
  },

  rejectPreceptor: async (id: number | string) => {
    const response = await api.post(`${ADMIN_API_PREFIX}/preceptors/reject-${id}`, null, authConfig());
    return unwrapApiData(response);
  },

  createAdmin: async (payload: { email: string; password?: string; displayName: string }) => {
    const response = await api.post(`${ADMIN_API_PREFIX}/add-admin`, payload, authConfig());
    return unwrapApiData(response);
  },

  getAdminRoster: async (): Promise<AdminUser[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/all-admins`, authConfig());
    return unwrapApiData<AdminUser[]>(response) || [];
  },

  toggleAdminAccount: async (userId: number | string, enabled: boolean) => {
    const response = await api.put(
      `${ADMIN_API_PREFIX}/user-${userId}/toggle-account?enabled=${enabled}`,
      null,
      authConfig()
    );
    return unwrapApiData(response);
  },

  getSettings: async (): Promise<SystemSetting[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/settings`, authConfig());
    return unwrapApiData<SystemSetting[]>(response) || [];
  },

  updateSetting: async (key: string, value: any) => {
    const response = await api.put(`${ADMIN_API_PREFIX}/settings/${key}`, value, authConfig());
    return unwrapApiData<SystemSetting>(response);
  },

  getAdminPreceptors: async (filters?: { specialty?: string; location?: string; verificationStatus?: string; page?: number; size?: number }) => {
    const hasFilters = Boolean(filters?.specialty || filters?.location || filters?.verificationStatus);
    const endpoint = hasFilters ? `${ADMIN_API_PREFIX}/preceptors/list/search` : `${ADMIN_API_PREFIX}/preceptors/list`;
    const response = await api.get(endpoint, {
      ...authConfig(),
      params: filters,
    });
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload);
  },

  getApprovedPreceptors: async () => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/verified/approved`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload);
  },

  getRejectedPreceptors: async () => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/verified/rejected`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload);
  },

  getAdminPreceptorDetail: async (userId: number | string): Promise<AdminPreceptorDetail> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/detail-${userId}`, authConfig());
    return unwrapApiData<AdminPreceptorDetail>(response);
  },

  updateAdminPreceptor: async (userId: number | string, payload: Record<string, any>) => {
    const response = await api.put(`${ADMIN_API_PREFIX}/preceptors/update-${userId}`, payload, authConfig());
    return unwrapApiData<AdminPreceptorDetail>(response);
  },

  getPreceptorVerificationHistory: async (userId: number | string): Promise<VerificationHistoryItem[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/${userId}/verification-history`, authConfig());
    return unwrapApiData<VerificationHistoryItem[]>(response) || [];
  },

  addPreceptorVerificationNote: async (userId: number | string, note: string, noteType = 'REVIEW') => {
    const response = await api.post(
      `${ADMIN_API_PREFIX}/preceptors/${userId}/verification-notes?note=${encodeURIComponent(note)}&noteType=${encodeURIComponent(noteType)}`,
      null,
      authConfig()
    );
    return unwrapApiData(response);
  },

  rejectPreceptorWithReason: async (userId: number | string, reason: string) => {
    const response = await api.post(
      `${ADMIN_API_PREFIX}/preceptors/detail-${userId}/reject?reason=${encodeURIComponent(reason)}`,
      null,
      authConfig()
    );
    return unwrapApiData(response);
  },

  getAdminPreceptorBilling: async (userId: number | string) => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/${userId}/billing`, authConfig());
    return unwrapApiData<any>(response);
  },

  getAdminPreceptorContact: async (userId: number | string) => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/detail-${userId}/contact`, authConfig());
    return unwrapApiData<any>(response);
  },

  getAdminLicenseDownloadUrl: (userId: number | string) => `/api/v1/api/v1/administration/preceptors/${userId}/license/download`,

  getAdminLicenseReviewUrl: (userId: number | string) => `/api/v1/api/v1/administration/preceptors/${userId}/license/review`,
};

export default adminService;
