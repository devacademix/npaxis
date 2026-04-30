import api from './auth';

export interface WebhookEventItem {
  eventId: string;
  eventType: string;
  status: string;
  processedAt?: string;
  retryCount?: number;
  errorMessage?: string;
}

const unwrapApiData = <T>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response.data as T;
};

export const webhookService = {
  getEvents: async (params?: { page?: number; size?: number }): Promise<WebhookEventItem[]> => {
    const response = await api.get('/webhooks/events', {
      params: {
        page: params?.page ?? 0,
        size: params?.size ?? 20,
      },
    });

    const payload = unwrapApiData<any>(response);
    const list = Array.isArray(payload)
      ? payload
      : Array.isArray(payload?.content)
      ? payload.content
      : Array.isArray(payload?.items)
      ? payload.items
      : [];

    return list.map((item: any) => ({
      eventId: String(item?.eventId ?? ''),
      eventType: String(item?.eventType ?? ''),
      status: String(item?.status ?? ''),
      processedAt: item?.processedAt ? String(item.processedAt) : undefined,
      retryCount: item?.retryCount != null ? Number(item.retryCount) : undefined,
      errorMessage: item?.errorMessage ? String(item.errorMessage) : undefined,
    }));
  },
};

export default webhookService;
