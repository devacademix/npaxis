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
  isEnabled?: boolean;
  enabled?: boolean;
  accountEnabled?: boolean;
  isDeleted?: boolean;
  deleted?: boolean;
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

export interface PreceptorRevenueItem {
  userId: number;
  displayName: string;
  subscriptionTier: string;
  successfulRevenue: number;
  failedRevenue: number;
  lastTransactionDate?: string;
}

export interface AdminPreceptorBillingInfo {
  userId: number;
  displayName: string;
  subscriptionStatus?: string;
  subscriptionPlan?: string;
  monthlyRevenue?: number;
  totalRevenue?: number;
  activeMonths?: number;
  subscriptionStartDate?: string;
  subscriptionEndDate?: string;
  lastPaymentStatus?: string;
  lastPaymentDate?: string;
  totalPayments?: number;
  failedPayments?: number;
}

export interface AdminPreceptorContactInfo {
  phone?: string;
  email?: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface TopPreceptorLeaderboardItem {
  userId?: number;
  displayName: string;
  profileViews?: number;
  conversionRate?: number;
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

export interface AdminTrendOverview {
  totalUsers?: number;
  totalStudents?: number;
  totalPreceptors?: number;
  newUsersThisMonth?: number;
  premiumUsersCount?: number;
  totalRevenue?: number;
  monthlyRevenue?: number;
  totalProfileViews?: number;
  totalContactReveals?: number;
  totalInquiries?: number;
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

export interface AdminPreceptorListItem {
  userId: number;
  displayName: string;
  email: string;
  specialty?: string;
  location?: string;
  verificationStatus?: string;
  isPremium?: boolean;
}

interface AdminPreceptorListApi {
  userId?: number;
  displayName?: string;
  email?: string;
  specialties?: string[];
  location?: string;
  verificationStatus?: string;
  isPremium?: boolean;
}

interface AdminPreceptorDetailApi {
  userId?: number;
  displayName?: string;
  email?: string;
  credentials?: string[];
  specialty?: string[];
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

export interface WebhookEventHistoryItem {
  eventId: string;
  eventType: string;
  status: string;
  processedAt?: string;
  retryCount: number;
  errorMessage?: string;
}

export interface WebhookEventDetail {
  eventId: string;
  eventType: string;
  status: string;
  eventPayload?: string;
  retryCount: number;
  eventDate?: string;
  lastUpdated?: string;
}

export interface WebhookMetrics {
  successfulCount: number;
  failedRetryingCount: number;
  failedCount: number;
  successRate: number;
  averageRetryCount: number;
  oldestPendingEventDate?: string;
  mostCommonEventType?: string;
  reportGeneratedAt?: string;
}

export interface AdminCatalogItem {
  id: number;
  name: string;
  description?: string;
  isPredefined: boolean;
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
const ADMIN_CATALOG_API_PREFIX = '/admin/credentials-specialties';

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

const mapPreceptorRevenueItem = (item: any): PreceptorRevenueItem => ({
  userId: Number(item?.userId ?? 0),
  displayName: String(item?.displayName ?? 'Unknown Preceptor'),
  subscriptionTier: String(item?.subscriptionPlan ?? 'Standard'),
  successfulRevenue: Number(item?.totalRevenue ?? 0),
  failedRevenue: Number(item?.monthlyRevenue ?? 0),
  lastTransactionDate: item?.lastPaymentDate ? String(item.lastPaymentDate) : undefined,
});

const mapAdminPreceptorBillingInfo = (item: any): AdminPreceptorBillingInfo => ({
  userId: Number(item?.userId ?? 0),
  displayName: String(item?.displayName ?? 'Unknown Preceptor'),
  subscriptionStatus: item?.subscriptionStatus ? String(item.subscriptionStatus) : undefined,
  subscriptionPlan: item?.subscriptionPlan ? String(item.subscriptionPlan) : undefined,
  monthlyRevenue: Number.isFinite(Number(item?.monthlyRevenue)) ? Number(item.monthlyRevenue) : undefined,
  totalRevenue: Number.isFinite(Number(item?.totalRevenue)) ? Number(item.totalRevenue) : undefined,
  activeMonths: Number.isFinite(Number(item?.activeMonths)) ? Number(item.activeMonths) : undefined,
  subscriptionStartDate: item?.subscriptionStartDate ? String(item.subscriptionStartDate) : undefined,
  subscriptionEndDate: item?.subscriptionEndDate ? String(item.subscriptionEndDate) : undefined,
  lastPaymentStatus: item?.lastPaymentStatus ? String(item.lastPaymentStatus) : undefined,
  lastPaymentDate: item?.lastPaymentDate ? String(item.lastPaymentDate) : undefined,
  totalPayments: Number.isFinite(Number(item?.totalPayments)) ? Number(item.totalPayments) : undefined,
  failedPayments: Number.isFinite(Number(item?.failedPayments)) ? Number(item.failedPayments) : undefined,
});

const mapAdminPreceptorContactInfo = (item: any): AdminPreceptorContactInfo => ({
  phone: item?.phone ? String(item.phone) : undefined,
  email: item?.email ? String(item.email) : undefined,
});

const mapDashboardStats = (payload: any): DashboardStats => ({
  totalUsers: Number(payload?.totalUsers ?? 0),
  premiumUsers: Number(payload?.verifiedPreceptors ?? payload?.premiumUsers ?? payload?.premiumCount ?? 0),
  revenue: Number(payload?.revenueThisMonth ?? payload?.totalRevenue ?? 0),
  activePreceptors: Number(payload?.totalPreceptors ?? 0),
});

const mapTrendOverview = (payload: any): AdminTrendOverview => ({
  totalUsers: Number(payload?.totalUsers ?? 0),
  totalStudents: Number(payload?.totalStudents ?? 0),
  totalPreceptors: Number(payload?.totalPreceptors ?? 0),
  newUsersThisMonth: Number(payload?.newUsersThisMonth ?? 0),
  premiumUsersCount: Number(payload?.premiumUsersCount ?? payload?.premiumUsers ?? 0),
  totalRevenue: Number(payload?.totalRevenue ?? 0),
  monthlyRevenue: Number(payload?.monthlyRevenue ?? payload?.revenueThisMonth ?? 0),
  totalProfileViews: Number(payload?.totalProfileViews ?? 0),
  totalContactReveals: Number(payload?.totalContactReveals ?? 0),
  totalInquiries: Number(payload?.totalInquiries ?? 0),
});

const mapTopPreceptorLeaderboardItem = (item: any): TopPreceptorLeaderboardItem => ({
  userId: Number.isFinite(Number(item?.userId ?? item?.preceptorId))
    ? Number(item?.userId ?? item?.preceptorId)
    : undefined,
  displayName: String(item?.displayName ?? item?.name ?? 'Unknown Preceptor'),
  profileViews: Number.isFinite(Number(item?.profileViews))
    ? Number(item.profileViews)
    : undefined,
  conversionRate: Number.isFinite(Number(item?.conversionRate))
    ? Number(item.conversionRate)
    : undefined,
});

const mapAdminUser = (item: any): AdminUser => ({
  userId: Number(item?.userId ?? 0),
  displayName: String(item?.displayName ?? 'Unknown User'),
  email: String(item?.email ?? 'N/A'),
  role: String(item?.role ?? 'N/A'),
  isEnabled: item?.isEnabled ?? item?.enabled ?? item?.accountEnabled,
  enabled: item?.enabled ?? item?.isEnabled ?? item?.accountEnabled,
  accountEnabled: item?.accountEnabled ?? item?.enabled ?? item?.isEnabled,
  isDeleted: item?.isDeleted ?? item?.deleted,
  deleted: item?.deleted ?? item?.isDeleted,
});

const mapWebhookEventHistoryItem = (item: any): WebhookEventHistoryItem => ({
  eventId: String(item?.eventId ?? ''),
  eventType: String(item?.eventType ?? 'Unknown'),
  status: String(item?.status ?? 'UNKNOWN'),
  processedAt: item?.processedAt ? String(item.processedAt) : undefined,
  retryCount: Number(item?.retryCount ?? 0),
  errorMessage: item?.errorMessage ? String(item.errorMessage) : undefined,
});

const mapWebhookEventDetail = (item: any): WebhookEventDetail => ({
  eventId: String(item?.eventId ?? ''),
  eventType: String(item?.eventType ?? 'Unknown'),
  status: String(item?.status ?? 'UNKNOWN'),
  eventPayload: item?.eventPayload ? String(item.eventPayload) : undefined,
  retryCount: Number(item?.retryCount ?? 0),
  eventDate: item?.eventDate ? String(item.eventDate) : undefined,
  lastUpdated: item?.lastUpdated ? String(item.lastUpdated) : undefined,
});

const mapWebhookMetrics = (item: any): WebhookMetrics => ({
  successfulCount: Number(item?.successfulCount ?? 0),
  failedRetryingCount: Number(item?.failedRetryingCount ?? 0),
  failedCount: Number(item?.failedCount ?? 0),
  successRate: Number(item?.successRate ?? 0),
  averageRetryCount: Number(item?.averageRetryCount ?? 0),
  oldestPendingEventDate: item?.oldestPendingEventDate ? String(item.oldestPendingEventDate) : undefined,
  mostCommonEventType: item?.mostCommonEventType ? String(item.mostCommonEventType) : undefined,
  reportGeneratedAt: item?.reportGeneratedAt ? String(item.reportGeneratedAt) : undefined,
});

const joinList = (value?: string[]) => (Array.isArray(value) && value.length > 0 ? value.join(', ') : undefined);

const mapAdminPreceptorList = (item: AdminPreceptorListApi) => ({
  userId: Number(item?.userId ?? 0),
  displayName: item?.displayName ?? 'Unknown Preceptor',
  email: item?.email ?? '',
  specialty: joinList(item?.specialties),
  location: item?.location ?? '',
  verificationStatus: item?.verificationStatus ?? '',
  isPremium: Boolean(item?.isPremium),
});

const mapAdminPreceptorDetail = (item: AdminPreceptorDetailApi): AdminPreceptorDetail => ({
  userId: Number(item?.userId ?? 0),
  displayName: item?.displayName ?? 'Unknown Preceptor',
  email: item?.email ?? 'N/A',
  credentials: joinList(item?.credentials),
  specialty: joinList(item?.specialty),
  location: item?.location ?? '',
  phone: item?.phone ?? '',
  honorarium: item?.honorarium ?? '',
  requirements: item?.requirements ?? '',
  isVerified: Boolean(item?.isVerified),
  isPremium: Boolean(item?.isPremium),
  verificationStatus: item?.verificationStatus ?? '',
});

export const adminService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/dashboard`, authConfig());
    return mapDashboardStats(unwrapApiData<any>(response));
  },

  getDashboardOverview: async (): Promise<AdminTrendOverview> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/analytics/overview`, authConfig());
    return mapTrendOverview(unwrapApiData<any>(response));
  },

  downloadDashboardReportPdf: async (): Promise<Blob> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/dashboard/report`, {
      ...authConfig(),
      responseType: 'blob',
    });

    return response.data as Blob;
  },

  getTopPreceptorsOverview: async (): Promise<TopPreceptorLeaderboardItem[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/analytics/top-preceptors`, authConfig());
    const payload = unwrapApiData<any>(response);
    const items = Array.isArray(payload?.topPreceptors)
      ? payload.topPreceptors
      : Array.isArray(payload)
        ? payload
        : [];
    return items.map(mapTopPreceptorLeaderboardItem);
  },

  getTrendOverview: async (): Promise<AdminTrendOverview> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/analytics/trends`, authConfig());
    return mapTrendOverview(unwrapApiData<any>(response));
  },

  getPendingPreceptors: async (params?: { page?: number; size?: number }): Promise<PaginatedResponse<PendingPreceptorView>> => {
    const response = await api.get(
      `${ADMIN_API_PREFIX}/preceptors/pending`,
      { ...authConfig(), ...buildPaginationConfig(params) }
    );
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<any>(payload).map(mapPendingPreceptor),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? params?.page ?? 0),
      size: Number(meta?.size ?? params?.size ?? 10),
    };
  },

  getAllUsers: async (): Promise<AdminUser[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/users`, authConfig());
    const payload = unwrapApiData<any>(response);
    return (Array.isArray(payload) ? payload : []).map(mapAdminUser);
  },

  getAdminUserDetail: async (userId: number | string): Promise<AdminUser | null> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/user-${userId}`, authConfig());
    const payload = unwrapApiData<any>(response);
    if (!payload) return null;
    return mapAdminUser(payload);
  },

  searchUsers: async (params: { email?: string; displayName?: string }): Promise<AdminUser[]> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/users/search`, {
      ...authConfig(),
      params,
    });
    const payload = unwrapApiData<any>(response);
    return (Array.isArray(payload) ? payload : []).map(mapAdminUser);
  },

  getWebhookHistory: async (params?: { page?: number; size?: number }): Promise<PaginatedResponse<WebhookEventHistoryItem>> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/webhooks/history`, {
      ...authConfig(),
      ...buildPaginationConfig(params),
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<any>(payload).map(mapWebhookEventHistoryItem),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? params?.page ?? 0),
      size: Number(meta?.size ?? params?.size ?? 10),
    };
  },

  retryWebhookEvent: async (eventId: string) => {
    const response = await api.post(`${ADMIN_API_PREFIX}/webhooks/event-${eventId}/retry`, null, authConfig());
    return unwrapApiData<string | null>(response);
  },

  getWebhookEventDetail: async (eventId: string): Promise<WebhookEventDetail> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/webhooks/event-${eventId}`, authConfig());
    return mapWebhookEventDetail(unwrapApiData<any>(response));
  },

  getWebhookMetrics: async (): Promise<WebhookMetrics> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/webhooks/metrics`, authConfig());
    return mapWebhookMetrics(unwrapApiData<any>(response));
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

  getRevenueByPreceptor: async (params?: { page?: number; size?: number }): Promise<PaginatedResponse<PreceptorRevenueItem>> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/revenue/by-preceptor`, {
      ...authConfig(),
      ...buildPaginationConfig(params),
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<any>(payload).map(mapPreceptorRevenueItem),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? params?.page ?? 0),
      size: Number(meta?.size ?? params?.size ?? 10),
    };
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

  getAdminPreceptors: async (filters?: { specialty?: string; location?: string; verificationStatus?: string; page?: number; size?: number }): Promise<PaginatedResponse<AdminPreceptorListItem>> => {
    const hasFilters = Boolean(filters?.specialty || filters?.location || filters?.verificationStatus);
    const endpoint = hasFilters ? `${ADMIN_API_PREFIX}/preceptors/list/search` : `${ADMIN_API_PREFIX}/preceptors/list`;
    const response = await api.get(endpoint, {
      ...authConfig(),
      params: filters,
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<AdminPreceptorListApi>(payload).map(mapAdminPreceptorList),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? filters?.page ?? 0),
      size: Number(meta?.size ?? filters?.size ?? 10),
    };
  },

  getApprovedPreceptors: async (params?: { page?: number; size?: number }): Promise<PaginatedResponse<AdminPreceptorListItem>> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/verified/approved`, {
      ...authConfig(),
      ...buildPaginationConfig(params),
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<AdminPreceptorListApi>(payload).map(mapAdminPreceptorList),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? params?.page ?? 0),
      size: Number(meta?.size ?? params?.size ?? 10),
    };
  },

  getRejectedPreceptors: async (params?: { page?: number; size?: number }): Promise<PaginatedResponse<AdminPreceptorListItem>> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/verified/rejected`, {
      ...authConfig(),
      ...buildPaginationConfig(params),
    });
    const payload = unwrapApiData<any>(response);
    const meta = response?.data?.meta ?? {};

    return {
      items: extractPageItems<AdminPreceptorListApi>(payload).map(mapAdminPreceptorList),
      totalElements: Number(meta?.totalElements ?? 0),
      totalPages: Number(meta?.totalPages ?? 0),
      page: Number(meta?.page ?? params?.page ?? 0),
      size: Number(meta?.size ?? params?.size ?? 10),
    };
  },

  getAdminPreceptorDetail: async (userId: number | string): Promise<AdminPreceptorDetail> => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/detail-${userId}`, authConfig());
    return mapAdminPreceptorDetail(unwrapApiData<AdminPreceptorDetailApi>(response));
  },

  updateAdminPreceptor: async (userId: number | string, payload: Record<string, any>) => {
    const response = await api.put(`${ADMIN_API_PREFIX}/preceptors/update-${userId}`, payload, authConfig());
    return mapAdminPreceptorDetail(unwrapApiData<AdminPreceptorDetailApi>(response));
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
    return mapAdminPreceptorBillingInfo(unwrapApiData<any>(response));
  },

  getAdminPreceptorContact: async (userId: number | string) => {
    const response = await api.get(`${ADMIN_API_PREFIX}/preceptors/detail-${userId}/contact`, authConfig());
    return mapAdminPreceptorContactInfo(unwrapApiData<any>(response));
  },

  getAdminLicenseDownloadUrl: (userId: number | string) => `/api/v1/administration/preceptors/${userId}/license/download`,

  getAdminLicenseReviewUrl: (userId: number | string) => `/api/v1/administration/preceptors/${userId}/license/review`,

  getCredentials: async (): Promise<AdminCatalogItem[]> => {
    const response = await api.get(`${ADMIN_CATALOG_API_PREFIX}/credentials`, authConfig());
    const payload = unwrapApiData<any>(response);
    return (Array.isArray(payload) ? payload : []).map((item) => ({
      id: Number(item?.id ?? 0),
      name: String(item?.name ?? ''),
      description: item?.description ? String(item.description) : undefined,
      isPredefined: Boolean(item?.isPredefined),
    }));
  },

  createCredential: async (payload: { name: string; description?: string }) => {
    const response = await api.post(`${ADMIN_CATALOG_API_PREFIX}/credentials`, payload, authConfig());
    const data = unwrapApiData<any>(response);
    return {
      id: Number(data?.id ?? 0),
      name: String(data?.name ?? ''),
      description: data?.description ? String(data.description) : undefined,
      isPredefined: Boolean(data?.isPredefined),
    } satisfies AdminCatalogItem;
  },

  updateCredential: async (credentialId: number | string, payload: { name: string; description?: string }) => {
    const response = await api.put(`${ADMIN_CATALOG_API_PREFIX}/credentials/${credentialId}`, payload, authConfig());
    const data = unwrapApiData<any>(response);
    return {
      id: Number(data?.id ?? 0),
      name: String(data?.name ?? ''),
      description: data?.description ? String(data.description) : undefined,
      isPredefined: Boolean(data?.isPredefined),
    } satisfies AdminCatalogItem;
  },

  deleteCredential: async (credentialId: number | string) => {
    const response = await api.delete(`${ADMIN_CATALOG_API_PREFIX}/credentials/${credentialId}`, authConfig());
    return unwrapApiData(response);
  },

  getSpecialties: async (): Promise<AdminCatalogItem[]> => {
    const response = await api.get(`${ADMIN_CATALOG_API_PREFIX}/specialties`, authConfig());
    const payload = unwrapApiData<any>(response);
    return (Array.isArray(payload) ? payload : []).map((item) => ({
      id: Number(item?.id ?? 0),
      name: String(item?.name ?? ''),
      description: item?.description ? String(item.description) : undefined,
      isPredefined: Boolean(item?.isPredefined),
    }));
  },

  createSpecialty: async (payload: { name: string; description?: string }) => {
    const response = await api.post(`${ADMIN_CATALOG_API_PREFIX}/specialties`, payload, authConfig());
    const data = unwrapApiData<any>(response);
    return {
      id: Number(data?.id ?? 0),
      name: String(data?.name ?? ''),
      description: data?.description ? String(data.description) : undefined,
      isPredefined: Boolean(data?.isPredefined),
    } satisfies AdminCatalogItem;
  },

  updateSpecialty: async (specialtyId: number | string, payload: { name: string; description?: string }) => {
    const response = await api.put(`${ADMIN_CATALOG_API_PREFIX}/specialties/${specialtyId}`, payload, authConfig());
    const data = unwrapApiData<any>(response);
    return {
      id: Number(data?.id ?? 0),
      name: String(data?.name ?? ''),
      description: data?.description ? String(data.description) : undefined,
      isPredefined: Boolean(data?.isPredefined),
    } satisfies AdminCatalogItem;
  },

  deleteSpecialty: async (specialtyId: number | string) => {
    const response = await api.delete(`${ADMIN_CATALOG_API_PREFIX}/specialties/${specialtyId}`, authConfig());
    return unwrapApiData(response);
  },
};

export default adminService;
