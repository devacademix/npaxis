import api from './auth';

export interface SubscriptionPlanPrice {
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
  description?: string;
  active: boolean;
  prices: SubscriptionPlanPrice[];
}

export interface SubscriptionStatus {
  subscriptionId?: number;
  planCode?: string;
  planName?: string;
  billingInterval?: string;
  amountInMinorUnits?: number;
  currency?: string;
  status?: string;
  accessEnabled?: boolean;
  currentPeriodStart?: string;
  currentPeriodEnd?: string;
  trialEndsAt?: string;
  cancelAtPeriodEnd?: boolean;
  canceledAt?: string;
  canceledReason?: string;
}

export interface PaymentHistoryItem {
  transactionId: string;
  amount: number;
  status: string;
  date: string;
  invoiceUrl?: string;
  planName?: string;
}

interface CheckoutSessionResponse {
  sessionId?: string;
  checkoutUrl?: string;
  customerId?: string;
}

interface PortalSessionResponse {
  portalUrl?: string;
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

const SUBSCRIPTION_API_PREFIX = '/api/v1/subscriptions';
const PLAN_API_PREFIX = '/api/v1/subscription-plans';

const extractPageItems = <T>(payload: any): T[] => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  return [];
};

const formatHistoryItem = (item: any, index: number): PaymentHistoryItem => ({
  transactionId: String(item?.subscriptionId ?? item?.transactionId ?? `sub-${index + 1}`),
  amount: Number(item?.amountInMinorUnits ?? item?.amount ?? 0) / 100,
  status: String(item?.status ?? 'PENDING').toUpperCase(),
  date: String(item?.endDate ?? item?.startDate ?? item?.createdAt ?? ''),
  invoiceUrl: item?.invoiceUrl ?? undefined,
  planName: item?.planName ?? item?.planCode ?? undefined,
});

export const paymentService = {
  getSubscriptionPlans: async (): Promise<SubscriptionPlan[]> => {
    const response = await api.get(PLAN_API_PREFIX, authConfig());
    return unwrapApiData<SubscriptionPlan[]>(response) || [];
  },

  getSubscriptionStatus: async (): Promise<SubscriptionStatus | null> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/status`, authConfig());
    return unwrapApiData<SubscriptionStatus>(response);
  },

  checkPremiumAccess: async (): Promise<boolean> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/access-check`, authConfig());
    const payload = unwrapApiData<{ hasAccess?: boolean }>(response);
    return Boolean(payload?.hasAccess);
  },

  getPaymentHistory: async (_preceptorId?: number | string): Promise<PaymentHistoryItem[]> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/history`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(formatHistoryItem);
  },

  createCheckoutSession: async (payload: { priceId: number }): Promise<string> => {
    const response = await api.post(`${SUBSCRIPTION_API_PREFIX}/checkout`, payload, authConfig());
    const data = unwrapApiData<CheckoutSessionResponse>(response);
    return data?.checkoutUrl || '';
  },

  createPortalSession: async (): Promise<string> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/billing-portal`, authConfig());
    const data = unwrapApiData<PortalSessionResponse>(response);
    return data?.portalUrl || '';
  },

  cancelSubscription: async (): Promise<void> => {
    await api.post(`${SUBSCRIPTION_API_PREFIX}/cancel`, null, authConfig());
  },

  updateSubscription: async (priceId: number): Promise<void> => {
    await api.put(`${SUBSCRIPTION_API_PREFIX}/update`, { priceId }, authConfig());
  },
};

export default paymentService;
