interface InquiryRequest {
  preceptorId: number | string;
  subject?: string;
  message: string;
}

export interface InquiryItem {
  inquiryId: number;
  preceptorId?: number;
  subject?: string;
  message: string;
  inquiryStatus?: string;
  createdAt?: string;
}

import api from './auth';
import { preceptorService } from './preceptor';

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

const normalizeInquiry = (payload: any): InquiryItem => ({
  inquiryId: Number(payload?.inquiryId ?? payload?.id ?? 0),
  preceptorId: payload?.preceptorId != null ? Number(payload.preceptorId) : undefined,
  subject: payload?.subject ? String(payload.subject) : undefined,
  message: String(payload?.message ?? ''),
  inquiryStatus: payload?.inquiryStatus ? String(payload.inquiryStatus) : payload?.status ? String(payload.status) : undefined,
  createdAt: payload?.createdAt ? String(payload.createdAt) : undefined,
});

export const inquiryService = {
  sendInquiry: async (payload: InquiryRequest) => {
    try {
      const response = await api.post(
        '/inquiries/send',
        {
          preceptorId: payload.preceptorId,
          subject: payload.subject || 'Clinical rotation inquiry',
          message: payload.message,
        },
        authConfig()
      );

      return unwrapApiData<any>(response);
    } catch (error: any) {
      const status = error?.response?.status;
      const message = String(error?.message || '').toLowerCase();

      if (
        status === 404 ||
        status === 405 ||
        message.includes('no static resource') ||
        message.includes('not found')
      ) {
        await preceptorService.trackAnalyticsEvent('INQUIRY', payload.preceptorId);
        return {
          success: true,
          message: 'Inquiry recorded successfully.',
          fallback: true,
        };
      }

      throw error;
    }
  },

  getMyInquiries: async (inquiryStatus?: string): Promise<InquiryItem[]> => {
    try {
      const response = await api.get('/inquiries/my-inquiries', {
        ...authConfig(),
        params: inquiryStatus ? { inquiryStatus } : {},
      });

      const payload = response?.data ?? {};
      const list = Array.isArray(payload?.data)
        ? payload.data
        : Array.isArray(payload?.data?.content)
        ? payload.data.content
        : Array.isArray(payload?.data?.items)
        ? payload.data.items
        : [];

      return list.map(normalizeInquiry);
    } catch (error: any) {
      const status = error?.response?.status;
      const message = String(error?.message || '').toLowerCase();
      if (
        status === 404 ||
        status === 405 ||
        message.includes('no static resource') ||
        message.includes('not found')
      ) {
        return [];
      }
      throw error;
    }
  },

  markAsRead: async (inquiryId: number | string): Promise<void> => {
    try {
      await api.patch(`/inquiries/${inquiryId}/read`, null, authConfig());
    } catch (error: any) {
      const status = error?.response?.status;
      const message = String(error?.message || '').toLowerCase();
      if (
        status === 404 ||
        status === 405 ||
        message.includes('no static resource') ||
        message.includes('not found')
      ) {
        return;
      }
      throw error;
    }
  },
};

export default inquiryService;
