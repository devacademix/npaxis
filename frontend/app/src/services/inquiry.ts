import api from './auth';

export interface InquiryRequest {
  preceptorId: number | string;
  subject: string;
  message: string;
}

export interface InquiryRecord {
  inquiryId: number;
  preceptorId?: number;
  studentName?: string;
  subject: string;
  message: string;
  status: string;
  createdAt?: string;
}

const INQUIRY_STATUSES = ['NEW', 'READ', 'REPLIED'] as const;

const normalizeStatus = (status?: string) => {
  const normalized = String(status || '').trim().toUpperCase();
  if (normalized === 'RESPONDED') return 'REPLIED';
  return normalized;
};

const sortByCreatedAtDesc = (items: InquiryRecord[]) =>
  [...items].sort((a, b) => {
    const first = a.createdAt ? new Date(a.createdAt).getTime() : 0;
    const second = b.createdAt ? new Date(b.createdAt).getTime() : 0;
    return second - first;
  });

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

const extractPageItems = <T>(payload: any): T[] => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.items)) return payload.items;
  if (Array.isArray(payload?.data)) return payload.data;
  return [];
};

const normalizeInquiryRecord = (payload: any): InquiryRecord => ({
  inquiryId: Number(payload?.inquiryId ?? payload?.id ?? 0),
  preceptorId: payload?.preceptorId != null ? Number(payload.preceptorId) : undefined,
  studentName: payload?.studentName ? String(payload.studentName) : payload?.student?.displayName ? String(payload.student.displayName) : undefined,
  subject: String(payload?.subject ?? 'Untitled Inquiry'),
  message: String(payload?.message ?? ''),
  status: normalizeStatus(payload?.status) || 'NEW',
  createdAt: payload?.createdAt ? String(payload.createdAt) : payload?.date ? String(payload.date) : undefined,
});

export const inquiryService = {
  sendInquiry: async (payload: InquiryRequest) => {
    const response = await api.post('/inquiries/send', payload, authConfig());
    return unwrapApiData<InquiryRecord>(response);
  },

  getMyInquiries: async (status?: string): Promise<InquiryRecord[]> => {
    const normalizedStatus = normalizeStatus(status);

    if (!normalizedStatus || normalizedStatus === 'ALL') {
      const responses = await Promise.all(
        INQUIRY_STATUSES.map((inquiryStatus) =>
          api.get('/inquiries/my-inquiries', {
            ...authConfig(),
            params: { inquiryStatus },
          })
        )
      );

      const merged = responses.flatMap((response) => {
        const payload = unwrapApiData<any>(response);
        return extractPageItems<any>(payload).map(normalizeInquiryRecord);
      });

      const unique = Array.from(
        new Map(merged.map((item) => [item.inquiryId, item])).values()
      );

      return sortByCreatedAtDesc(unique);
    }

    const response = await api.get('/inquiries/my-inquiries', {
      ...authConfig(),
      params: { inquiryStatus: normalizedStatus },
    });
    const payload = unwrapApiData<any>(response);
    return sortByCreatedAtDesc(extractPageItems<any>(payload).map(normalizeInquiryRecord));
  },

  hasExistingInquiryForPreceptor: async (preceptorId: number | string, preceptorDisplayName?: string): Promise<boolean> => {
    const inquiries = await inquiryService.getMyInquiries();
    const byIdSubject = `inquiry for preceptor #${preceptorId}`.toLowerCase();
    const byNameSubject = preceptorDisplayName ? `inquiry for ${preceptorDisplayName}`.toLowerCase() : '';

    return inquiries.some((item) => {
      const subject = String(item.subject || '').trim().toLowerCase();
      return subject === byIdSubject || (byNameSubject ? subject === byNameSubject : false);
    });
  },

  markAsRead: async (inquiryId: number | string) => {
    const response = await api.patch(`/inquiries/${inquiryId}/read`, null, authConfig());
    return unwrapApiData(response);
  },
};

export default inquiryService;
