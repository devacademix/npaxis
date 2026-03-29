import api from './auth';

interface CheckoutSessionPayload {
  userId: number;
}

interface PortalSessionPayload {
  userId: number;
}

interface CheckoutSessionResponse {
  checkoutUrl?: string;
  url?: string;
  checkout_url?: string;
}

type PortalSessionResponse = CheckoutSessionResponse;

export interface PaymentHistoryItem {
  transactionId: string;
  amount: number;
  status: string;
  date: string;
  invoiceUrl?: string;
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

const extractUrl = (data: CheckoutSessionResponse | string): string => {
  if (typeof data === 'string') {
    return data;
  }
  return data.checkoutUrl || data.url || data.checkout_url || '';
};

export const paymentService = {
  getPaymentHistory: async (preceptorId: number | string): Promise<PaymentHistoryItem[]> => {
    const response = await api.get(`/payments/history/${preceptorId}`, authConfig());
    const payload = unwrapApiData<any>(response);

    const rows = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.items)
      ? payload.items
      : [];

    return rows.map((item: any, index: number) => ({
      transactionId: String(item?.transactionId ?? item?.id ?? item?.paymentId ?? `txn-${preceptorId}-${index + 1}`),
      amount: Number(item?.amount ?? item?.totalAmount ?? item?.paymentAmount ?? 0),
      status: String(item?.status ?? item?.paymentStatus ?? 'PENDING').toUpperCase(),
      date: String(item?.date ?? item?.createdAt ?? item?.paymentDate ?? ''),
      invoiceUrl: item?.invoiceUrl ?? item?.invoice?.url ?? undefined,
    }));
  },

  createCheckoutSession: async (payload: CheckoutSessionPayload): Promise<string> => {
    const response = await api.post('/payments/create-checkout-session', payload, authConfig());
    const data = unwrapApiData<CheckoutSessionResponse | string>(response);
    return extractUrl(data);
  },

  createPortalSession: async (payload: PortalSessionPayload): Promise<string> => {
    try {
      const response = await api.post('/payments/create-portal-session', payload, authConfig());
      const data = unwrapApiData<PortalSessionResponse | string>(response);
      return extractUrl(data);
    } catch {
      // Some backends infer user from token and accept empty body.
      const response = await api.post('/payments/create-portal-session', null, authConfig());
      const data = unwrapApiData<PortalSessionResponse | string>(response);
      return extractUrl(data);
    }
  },
};

export default paymentService;
