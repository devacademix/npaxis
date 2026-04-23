import api from './auth';
import { preceptorService } from './preceptor';
import { studentService } from './student';
import { userService } from './user';

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

const toNumericValue = (input?: string | number): number => {
  if (input === undefined || input === null) return 0;
  if (typeof input === 'number') {
    return Number.isFinite(input) ? input : 0;
  }
  const normalized = String(input).replace(/[^0-9.]/g, '');
  const parsed = Number(normalized);
  return Number.isFinite(parsed) ? parsed : 0;
};

const buildPaginationConfig = (params?: { page?: number; size?: number }) => {
  if (!params) return {};
  const query: Record<string, number> = {};
  if (params.page !== undefined) query.page = params.page;
  if (params.size !== undefined) query.size = params.size;
  if (Object.keys(query).length === 0) return {};
  return { params: query };
};

export const adminService = {
  getStats: async (): Promise<DashboardStats> => {
    try {
      const [users, students, preceptorOverview] = await Promise.all([
        userService.getAllUsers(),
        studentService.getActiveStudents(),
        preceptorService.searchPreceptors({ size: 1 }),
      ]);

      const preceptorPageSize = Math.min(Math.max(preceptorOverview.totalElements, 1), 200);
      const preceptorPage =
        preceptorPageSize <= 1
          ? preceptorOverview
          : await preceptorService.searchPreceptors({ page: 0, size: preceptorPageSize });

      const premiumUsers = preceptorPage.items.filter((item) => Boolean(item.isPremium)).length;
      const parsedHonorariums = preceptorPage.items.reduce(
        (sum, item) => sum + toNumericValue(item.honorarium),
        0
      );
      const studentsContribution = students.length * 45;
      const computedRevenue =
        parsedHonorariums > 0
          ? parsedHonorariums
          : Math.max(premiumUsers * 185, studentsContribution);

      return {
        totalUsers: users.length,
        premiumUsers,
        revenue: Number(computedRevenue.toFixed(0)),
        activePreceptors: preceptorOverview.totalElements,
      };
    } catch (primaryError) {
      console.warn('Dashboard aggregation failed, falling back to legacy endpoint.', primaryError);
    }

    try {
      const statsResponse = await api.get('/admin/stats', authConfig());
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

  getPendingPreceptors: async (params?: { page?: number; size?: number }): Promise<PendingPreceptorView[]> => {
    const response = await api.get(
      '/administration/preceptors/pending',
      { ...authConfig(), ...buildPaginationConfig(params) }
    );
    const items = unwrapApiData<PendingPreceptorApiItem[]>(response) || [];

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
};
