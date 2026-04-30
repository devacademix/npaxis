import api from './auth';

export interface SubscriptionPrice {
  subscriptionPriceId: number;
  billingInterval: string;
  currency: string;
  amountInMinorUnits: number;
  active: boolean;
}

export interface SubscriptionPlan {
  subscriptionPlanId: number;
  code: string;
  name: string;
  description: string;
  active: boolean;
  prices: SubscriptionPrice[];
}

export interface SubscriptionStatus {
  subscriptionId: number;
  planCode: string;
  planName: string;
  billingInterval: string;
  amountInMinorUnits: number;
  currency: string;
  status: string;
  accessEnabled: boolean;
  currentPeriodStart?: string;
  currentPeriodEnd?: string;
  trialEndsAt?: string;
  cancelAtPeriodEnd: boolean;
  canceledAt?: string;
  canceledReason?: string;
}

export interface SubscriptionHistoryItem {
  subscriptionId: number;
  planCode: string;
  planName: string;
  status: string;
  startDate?: string;
  endDate?: string;
  cancelReason?: string;
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

export const subscriptionService = {
  getPlans: async (): Promise<SubscriptionPlan[]> => {
    const response = await api.get('/subscription-plans', authConfig());
    return unwrapApiData<SubscriptionPlan[]>(response) || [];
  },

  getStatus: async (): Promise<SubscriptionStatus | null> => {
    try {
      const response = await api.get('/api/subscriptions/status', authConfig());
      return unwrapApiData<SubscriptionStatus>(response);
    } catch (error: any) {
      if (error?.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  createCheckoutSession: async (priceId: number): Promise<string> => {
    const response = await api.post('/api/subscriptions/checkout', { priceId }, authConfig());
    const payload = unwrapApiData<any>(response);
    return String(payload?.checkoutUrl ?? '');
  },

  createPortalSession: async (): Promise<string> => {
    const response = await api.get('/api/subscriptions/portal', authConfig());
    const payload = unwrapApiData<any>(response);
    return String(payload?.portalUrl ?? '');
  },

  cancelSubscription: async (): Promise<void> => {
    await api.post('/api/subscriptions/cancel', null, authConfig());
  },

  updateSubscription: async (priceId: number): Promise<void> => {
    await api.put('/api/subscriptions/update', { priceId }, authConfig());
  },

  checkAccess: async (): Promise<boolean> => {
    const response = await api.get('/api/subscriptions/access-check', authConfig());
    const payload = unwrapApiData<any>(response);
    return Boolean(payload?.hasAccess);
  },

  getHistory: async (): Promise<SubscriptionHistoryItem[]> => {
    const response = await api.get('/api/subscriptions/history', authConfig());
    const payload = response?.data ?? {};
    const list = Array.isArray(payload?.data)
      ? payload.data
      : Array.isArray(payload?.data?.content)
      ? payload.data.content
      : Array.isArray(payload?.data?.items)
      ? payload.data.items
      : [];

    return list.map((item: any) => ({
      subscriptionId: Number(item?.subscriptionId ?? 0),
      planCode: String(item?.planCode ?? ''),
      planName: String(item?.planName ?? ''),
      status: String(item?.status ?? ''),
      startDate: item?.startDate ? String(item.startDate) : undefined,
      endDate: item?.endDate ? String(item.endDate) : undefined,
      cancelReason: item?.cancelReason ? String(item.cancelReason) : undefined,
    }));
  },
};

export default subscriptionService;
