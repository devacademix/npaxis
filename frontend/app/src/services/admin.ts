import api from './auth';

interface PendingPreceptorApiItem {
  userId?: number | string;
  user?: {
    displayName?: string;
    email?: string;
  };
  credentials?: string;
  licenseNumber?: string;
  licenseFileUrl?: string;
  verificationSubmittedAt?: string;
  verificationStatus?: string;
}

interface AdminPreceptorApiItem {
  userId?: number | string;
  displayName?: string;
  name?: string;
  email?: string;
  specialty?: string;
  location?: string;
  isVerified?: boolean;
  isPremium?: boolean;
  verificationStatus?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface AdminStudentApiItem {
  userId?: number | string;
  displayName?: string;
  name?: string;
  email?: string;
  university?: string;
  program?: string;
  graduationYear?: string;
  phone?: string;
  isDeleted?: boolean;
  deleted?: boolean;
}

interface SystemSettingApi {
  key?: string;
  value?: unknown;
  description?: string;
  settingType?: string;
}

interface PaginationParams {
  [key: string]: unknown;
  page?: number;
  size?: number;
  sort?: string;
}

interface DashboardStats {
  totalUsers: number;
  premiumUsers: number;
  revenue: number;
  activePreceptors: number;
}

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
  isEnabled?: boolean;
  enabled?: boolean;
}

export interface RevenueStatsApi {
  totalUsers?: number;
  premiumCount?: number;
  premiumUsers?: number;
  revenue?: number;
  monthlyRevenue?: number;
  totalTransactions?: number;
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
  revenueTrend?: Array<{ label: string; value: number }>;
  transactionTrend?: Array<{ label: string; value: number }>;
}

export interface AdminPreceptorSummary {
  userId: number;
  displayName: string;
  email: string;
  specialty?: string;
  location?: string;
  isVerified: boolean;
  isPremium: boolean;
  verificationStatus?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AdminStudentSummary {
  userId: number;
  displayName: string;
  email: string;
  university?: string;
  program?: string;
  graduationYear?: string;
  phone?: string;
  isDeleted?: boolean;
}

export interface VerificationHistoryItem {
  [key: string]: unknown;
}

export interface SystemSetting {
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

const authConfig = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) return {};
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
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

const toNumber = (value: unknown, fallback = 0): number => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

const buildQueryConfig = (params?: Record<string, unknown>) => {
  const cleaned = Object.entries(params ?? {}).reduce<Record<string, unknown>>((acc, [key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      acc[key] = value;
    }
    return acc;
  }, {});

  if (Object.keys(cleaned).length === 0) return {};
  return { params: cleaned };
};

const extractList = <T>(payload: any): T[] => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  return [];
};

const normalizePreceptor = (item: AdminPreceptorApiItem): AdminPreceptorSummary => ({
  userId: toNumber(item.userId),
  displayName: String(item.displayName ?? item.name ?? 'Preceptor'),
  email: String(item.email ?? ''),
  specialty: item.specialty ? String(item.specialty) : undefined,
  location: item.location ? String(item.location) : undefined,
  isVerified: Boolean(item.isVerified),
  isPremium: Boolean(item.isPremium),
  verificationStatus: item.verificationStatus ? String(item.verificationStatus) : undefined,
  createdAt: item.createdAt ? String(item.createdAt) : undefined,
  updatedAt: item.updatedAt ? String(item.updatedAt) : undefined,
});

const normalizeStudent = (item: AdminStudentApiItem): AdminStudentSummary => ({
  userId: toNumber(item.userId),
  displayName: String(item.displayName ?? item.name ?? 'Student'),
  email: String(item.email ?? ''),
  university: item.university ? String(item.university) : undefined,
  program: item.program ? String(item.program) : undefined,
  graduationYear: item.graduationYear ? String(item.graduationYear) : undefined,
  phone: item.phone ? String(item.phone) : undefined,
  isDeleted: Boolean(item.isDeleted ?? item.deleted),
});

export const adminService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/administration/dashboard', authConfig());
    const stats = unwrapApiData<any>(response) || {};

    return {
      totalUsers: Number(stats?.totalUsers ?? stats?.users ?? 0),
      premiumUsers: Number(stats?.premiumCount ?? stats?.premiumUsers ?? 0),
      revenue: Number(stats?.monthlyRevenue ?? stats?.revenue ?? stats?.totalRevenue ?? 0),
      activePreceptors: Number(stats?.activePreceptors ?? stats?.preceptorCount ?? 0),
    };
  },

  getAnalyticsOverview: async () => {
    const response = await api.get('/administration/analytics/overview', authConfig());
    return unwrapApiData<any>(response);
  },

  getAnalyticsTopPreceptors: async () => {
    const response = await api.get('/administration/analytics/top-preceptors', authConfig());
    return unwrapApiData<any>(response);
  },

  getAnalyticsTrends: async () => {
    const response = await api.get('/administration/analytics/trends', authConfig());
    return unwrapApiData<any>(response);
  },

  getPendingPreceptors: async (params?: PaginationParams): Promise<PendingPreceptorView[]> => {
    const response = await api.get(
      '/administration/preceptors/pending',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const items = extractList<PendingPreceptorApiItem>(unwrapApiData<any>(response));

    return items.map((item) => ({
      id: item.userId ?? '',
      name: item.user?.displayName || 'Unknown Preceptor',
      email: item.user?.email || 'N/A',
      credentials: item.credentials || 'N/A',
      licenseNumber: item.licenseNumber || 'N/A',
      licenseFileUrl: item.licenseFileUrl || undefined,
      dateSubmitted: formatDate(item.verificationSubmittedAt),
      submittedAtRaw: item.verificationSubmittedAt ?? null,
      status: normalizeStatus(item.verificationStatus),
    }));
  },

  getAdminPreceptors: async (params?: PaginationParams) => {
    const response = await api.get(
      '/administration/preceptors/list',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractList<AdminPreceptorApiItem>(payload).map(normalizePreceptor);
  },

  searchAdminPreceptors: async (params?: Record<string, unknown>) => {
    const response = await api.get(
      '/administration/preceptors/list/search',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractList<AdminPreceptorApiItem>(payload).map(normalizePreceptor);
  },

  getAdminPreceptorDetail: async (userId: number | string) => {
    const response = await api.get(`/administration/preceptors/detail-${userId}`, authConfig());
    return unwrapApiData<any>(response);
  },

  updateAdminPreceptor: async (userId: number | string, payload: Record<string, unknown>) => {
    const response = await api.put(`/administration/preceptors/update-${userId}`, payload, authConfig());
    return unwrapApiData<any>(response);
  },

  getApprovedPreceptors: async (params?: PaginationParams) => {
    const response = await api.get(
      '/administration/preceptors/verified/approved',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractList<AdminPreceptorApiItem>(payload).map(normalizePreceptor);
  },

  getRejectedPreceptors: async (params?: PaginationParams) => {
    const response = await api.get(
      '/administration/preceptors/verified/rejected',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractList<AdminPreceptorApiItem>(payload).map(normalizePreceptor);
  },

  getPreceptorVerificationHistory: async (userId: number | string): Promise<VerificationHistoryItem[]> => {
    const response = await api.get(`/administration/preceptors/${userId}/verification-history`, authConfig());
    return extractList<VerificationHistoryItem>(unwrapApiData<any>(response));
  },

  addPreceptorVerificationNote: async (userId: number | string, note: string, noteType: string) => {
    const response = await api.post(
      `/administration/preceptors/${userId}/verification-notes`,
      null,
      { ...authConfig(), ...buildQueryConfig({ note, noteType }) }
    );
    return unwrapApiData<any>(response);
  },

  getPreceptorBillingReport: async (userId: number | string) => {
    const response = await api.get(`/administration/preceptors/${userId}/billing`, authConfig());
    return unwrapApiData<any>(response);
  },

  getAdminPreceptorAnalytics: async (userId: number | string) => {
    const response = await api.get(`/administration/preceptors/${userId}/analytics`, authConfig());
    return unwrapApiData<any>(response);
  },

  getPreceptorLicenseUrls: (userId: number | string) => ({
    downloadUrl: `/api/v1/administration/preceptors/${userId}/license/download`,
    reviewUrl: `/api/v1/administration/preceptors/${userId}/license/review`,
  }),

  getAllUsers: async (): Promise<AdminUser[]> => {
    const response = await api.get('/users/all', authConfig());
    return unwrapApiData<AdminUser[]>(response) || [];
  },

  getAdminStudents: async (params?: PaginationParams) => {
    const response = await api.get(
      '/administration/students/list',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractList<AdminStudentApiItem>(payload).map(normalizeStudent);
  },

  searchAdminStudents: async (params?: Record<string, unknown>) => {
    const response = await api.get(
      '/administration/students/search',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    return extractList<AdminStudentApiItem>(payload).map(normalizeStudent);
  },

  getAdminStudentDetail: async (userId: number | string) => {
    const response = await api.get(`/administration/students/detail-${userId}`, authConfig());
    return unwrapApiData<any>(response);
  },

  getRevenueStats: async (): Promise<RevenueStatsApi> => {
    const response = await api.get('/administration/revenue/summary', authConfig());
    return unwrapApiData<RevenueStatsApi>(response) || {};
  },

  getPaymentHistory: async (preceptorId: string | number): Promise<PaymentHistoryItem[]> => {
    const response = await api.get('/administration/revenue/transactions', authConfig());
    const payload = unwrapApiData<any>(response);
    const list = extractList<any>(payload);

    return list.map((item: any, index: number) => ({
      id: item?.id ?? item?.paymentId ?? item?.transactionId ?? `${preceptorId}-${index}`,
      preceptorName:
        item?.preceptorName ??
        item?.preceptor?.displayName ??
        item?.displayName ??
        item?.preceptor?.name ??
        item?.name ??
        `Preceptor #${preceptorId}`,
      amount: Number(item?.amount ?? item?.paymentAmount ?? item?.totalAmount ?? 0),
      status: String(item?.status ?? item?.paymentStatus ?? 'Pending'),
      date: String(item?.date ?? item?.createdAt ?? item?.paymentDate ?? ''),
      invoiceUrl: item?.invoiceUrl ?? item?.invoice?.url ?? undefined,
    }));
  },

  getRevenueByPreceptor: async (params?: PaginationParams) => {
    const response = await api.get(
      '/administration/revenue/by-preceptor',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    return extractList<any>(unwrapApiData<any>(response));
  },

  getPreceptorAnalytics: async (id: string | number): Promise<PreceptorAnalytics> => {
    const response = await api.get(`/analytics/preceptors/${id}/stats`, authConfig());
    return unwrapApiData<PreceptorAnalytics>(response) || {};
  },

  approvePreceptor: async (id: number | string) => {
    const response = await api.post(`/administration/preceptors/approve-${id}`, null, authConfig());
    return unwrapApiData(response);
  },

  rejectPreceptor: async (id: number | string) => {
    const response = await api.post(`/administration/preceptors/reject-${id}`, null, authConfig());
    return unwrapApiData(response);
  },

  createAdmin: async (payload: { email: string; password?: string; displayName: string }) => {
    const response = await api.post('/administration/add-admin', payload, authConfig());
    return unwrapApiData(response);
  },

  getAdminRoster: async (): Promise<AdminUser[]> => {
    const response = await api.get('/administration/all-admins', authConfig());
    return unwrapApiData<AdminUser[]>(response) || [];
  },

  toggleAdminAccount: async (userId: number | string, enabled: boolean) => {
    const response = await api.put(
      `/administration/user-${userId}/toggle-account?enabled=${enabled}`,
      null,
      authConfig()
    );
    return unwrapApiData(response);
  },

  getPlatformSettings: async (): Promise<SystemSetting[]> => {
    const response = await api.get('/administration/settings', authConfig());
    return extractList<SystemSettingApi>(unwrapApiData<any>(response)).map((item) => ({
      key: String(item.key ?? ''),
      value: item.value,
      description: item.description ? String(item.description) : undefined,
      settingType: item.settingType ? String(item.settingType) : undefined,
    }));
  },

  getPlatformSetting: async (key: string): Promise<SystemSetting> => {
    const response = await api.get(`/administration/settings/${key}`, authConfig());
    const item = unwrapApiData<SystemSettingApi>(response) || {};
    return {
      key: String(item.key ?? key),
      value: item.value,
      description: item.description ? String(item.description) : undefined,
      settingType: item.settingType ? String(item.settingType) : undefined,
    };
  },

  updatePlatformSetting: async (key: string, value: unknown) => {
    const response = await api.put(`/administration/settings/${key}`, { value }, authConfig());
    return unwrapApiData<any>(response);
  },

  getWebhookHistory: async (params?: PaginationParams) => {
    const response = await api.get(
      '/administration/webhooks/history',
      { ...authConfig(), ...buildQueryConfig(params) }
    );
    return extractList<any>(unwrapApiData<any>(response));
  },

  retryWebhookEvent: async (eventId: string | number) => {
    const response = await api.post(`/administration/webhooks/event-${eventId}/retry`, null, authConfig());
    return unwrapApiData<any>(response);
  },

  getWebhookEvent: async (eventId: string | number) => {
    const response = await api.get(`/administration/webhooks/event-${eventId}`, authConfig());
    return unwrapApiData<any>(response);
  },

  getHealth: async () => {
    const response = await api.get('/');
    return unwrapApiData<any>(response);
  },
};

export default adminService;
