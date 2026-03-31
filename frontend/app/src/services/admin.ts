import api from './auth';

interface UserSummary {
  userId: number;
  role?: string;
}

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

interface DashboardStats {
  totalUsers: number;
  premiumUsers: number;
  revenue: number;
  activePreceptors: number;
}

interface PendingPreceptorView {
  id: number | string;
  name: string;
  email: string;
  credentials?: string;
  licenseNumber: string;
  licenseFileUrl?: string;
  dateSubmitted: string;
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

export const adminService = {
  getStats: async (): Promise<DashboardStats> => {
    try {
      const statsResponse = await api.get('/analytics/stats', authConfig());
      const stats = unwrapApiData<any>(statsResponse) || {};
      return {
        totalUsers: Number(stats?.totalUsers ?? stats?.users ?? 0),
        premiumUsers: Number(stats?.premiumCount ?? stats?.premiumUsers ?? 0),
        revenue: Number(stats?.revenue ?? stats?.totalRevenue ?? 0),
        activePreceptors: Number(stats?.activePreceptors ?? stats?.preceptorCount ?? 0),
      };
    } catch {
      const usersResponse = await api.get('/users/all', authConfig());
      const users = unwrapApiData<UserSummary[]>(usersResponse) || [];

      const totalUsers = users.length;
      const activePreceptors = users.filter((user) => (user.role || '').includes('PRECEPTOR')).length;

      return {
        totalUsers,
        premiumUsers: activePreceptors,
        revenue: 0,
        activePreceptors,
      };
    }
  },

  getPendingPreceptors: async (): Promise<PendingPreceptorView[]> => {
    const response = await api.get('/administration/preceptors/pending', authConfig());
    const items = unwrapApiData<PendingPreceptorApiItem[]>(response) || [];

    return items.map((item) => ({
      id: item.userId ?? '',
      name: item.user?.displayName || 'Unknown Preceptor',
      email: item.user?.email || 'N/A',
      credentials: item.credentials || 'N/A',
      licenseNumber: item.licenseNumber || 'N/A',
      licenseFileUrl: item.licenseFileUrl || undefined,
      dateSubmitted: formatDate(item.verificationSubmittedAt),
      status: normalizeStatus(item.verificationStatus),
    }));
  },

  getAllUsers: async (): Promise<AdminUser[]> => {
    const response = await api.get('/users/all', authConfig());
    return unwrapApiData<AdminUser[]>(response) || [];
  },

  // Revenue dashboard APIs (kept exactly as requested)
  getRevenueStats: async (): Promise<RevenueStatsApi> => {
    const response = await api.get('/admin/stats', authConfig());
    return unwrapApiData<RevenueStatsApi>(response) || {};
  },

  getPaymentHistory: async (preceptorId: string | number): Promise<PaymentHistoryItem[]> => {
    const response = await api.get(`/payments/history/${preceptorId}`, authConfig());
    const payload = unwrapApiData<any>(response);

    const list = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.items)
      ? payload.items
      : [];

    return list.map((item: any, index: number) => ({
      id: item?.id ?? item?.paymentId ?? item?.transactionId ?? `${preceptorId}-${index}`,
      preceptorName:
        item?.preceptorName ??
        item?.preceptor?.displayName ??
        item?.preceptor?.name ??
        item?.name ??
        `Preceptor #${preceptorId}`,
      amount: Number(item?.amount ?? item?.paymentAmount ?? 0),
      status: String(item?.status ?? item?.paymentStatus ?? 'Pending'),
      date: String(item?.date ?? item?.createdAt ?? item?.paymentDate ?? ''),
      invoiceUrl: item?.invoiceUrl ?? item?.invoice?.url ?? undefined,
    }));
  },

  getPreceptorAnalytics: async (id: string | number): Promise<PreceptorAnalytics> => {
    const response = await api.get(`/preceptors/${id}/stats`, authConfig());
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
  }
};
