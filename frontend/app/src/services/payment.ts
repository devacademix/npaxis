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

export interface SubscriptionEventItem {
  eventId: number;
  subscriptionId?: number;
  eventType: string;
  status: string;
  createdAt?: string;
  stripeEventId?: string;
  errorMessage?: string;
}

interface CheckoutSessionResponse {
  sessionId?: string;
  checkoutUrl?: string;
  customerId?: string;
}

interface PortalSessionResponse {
  portalUrl?: string;
}

interface LegacyCheckoutPayload {
  preceptorId: number;
  billingCycle: string;
  successUrl: string;
  cancelUrl: string;
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
const LEGACY_PAYMENT_API_PREFIX = '/api/v1/payments';

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
  date: String(item?.paidDate ?? item?.invoiceDate ?? item?.currentPeriodEnd ?? item?.endDate ?? item?.startDate ?? item?.createdAt ?? item?.date ?? ''),
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

  getSubscriptionEvents: async (): Promise<SubscriptionEventItem[]> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/events`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map((item) => ({
      eventId: Number(item?.eventId ?? 0),
      subscriptionId: item?.subscriptionId != null ? Number(item.subscriptionId) : undefined,
      eventType: String(item?.eventType ?? 'UNKNOWN'),
      status: String(item?.status ?? 'UNKNOWN'),
      createdAt: item?.createdAt ? String(item.createdAt) : undefined,
      stripeEventId: item?.stripeEventId ? String(item.stripeEventId) : undefined,
      errorMessage: item?.errorMessage ? String(item.errorMessage) : undefined,
    }));
  },

  getPaymentHistory: async (_preceptorId?: number | string): Promise<PaymentHistoryItem[]> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/history`, authConfig());
    const payload = unwrapApiData<any>(response);
    return extractPageItems<any>(payload).map(formatHistoryItem);
  },

  createCheckoutSession: async (payload: {
    priceId?: number | null;
    preceptorId?: number | null;
    billingInterval?: string | null;
    successUrl?: string;
    cancelUrl?: string;
  }): Promise<string> => {
    if (payload.priceId != null) {
      const response = await api.post(`${SUBSCRIPTION_API_PREFIX}/checkout`, { priceId: payload.priceId }, authConfig());
      const data = unwrapApiData<CheckoutSessionResponse>(response);
      return data?.checkoutUrl || '';
    }

    if (payload.preceptorId != null && payload.billingInterval) {
      const legacyPayload: LegacyCheckoutPayload = {
        preceptorId: payload.preceptorId,
        billingCycle: String(payload.billingInterval).toUpperCase(),
        successUrl: payload.successUrl || `${window.location.origin}/preceptor/subscription`,
        cancelUrl: payload.cancelUrl || `${window.location.origin}/preceptor/subscription`,
      };
      const response = await api.post(`${LEGACY_PAYMENT_API_PREFIX}/create-checkout-session`, legacyPayload, authConfig());
      const data = unwrapApiData<CheckoutSessionResponse>(response);
      return data?.checkoutUrl || '';
    }

    throw new Error('No checkout configuration is available right now.');
  },

  createPortalSession: async (): Promise<string> => {
    const response = await api.get(`${SUBSCRIPTION_API_PREFIX}/billing-portal`, authConfig());
    const data = unwrapApiData<PortalSessionResponse>(response);
    return data?.portalUrl || '';
  },

  confirmCheckoutSession: async (sessionId: string): Promise<void> => {
    await api.get(`${SUBSCRIPTION_API_PREFIX}/checkout/confirm`, {
      ...authConfig(),
      params: { sessionId },
    });
  },

  cancelSubscription: async (): Promise<void> => {
    await api.post(`${SUBSCRIPTION_API_PREFIX}/cancel`, null, authConfig());
  },

  updateSubscription: async (priceId: number): Promise<void> => {
    await api.put(`${SUBSCRIPTION_API_PREFIX}/update`, { priceId }, authConfig());
  },
};

export default paymentService;
