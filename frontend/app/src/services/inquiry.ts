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

export type InquiryStatusFilter = 'ALL' | 'NEW' | 'READ' | 'REPLIED';
export type InquiryApiStatus = Exclude<InquiryStatusFilter, 'ALL'>;
const INQUIRY_CACHE_TTL_MS = 30_000;

const normalizeStatus = (status?: string): InquiryStatusFilter | '' => {
  const normalized = String(status || '').trim().toUpperCase();
  if (normalized === 'RESPONDED') return 'REPLIED';
  if (normalized === 'ALL' || normalized === 'NEW' || normalized === 'READ' || normalized === 'REPLIED') {
    return normalized;
  }
  return '';
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
  studentName: payload?.studentName
    ? String(payload.studentName)
    : payload?.student?.displayName
      ? String(payload.student.displayName)
      : undefined,
  subject: String(payload?.subject ?? 'Untitled Inquiry'),
  message: String(payload?.message ?? ''),
  status: normalizeStatus(payload?.status) || 'NEW',
  createdAt: payload?.createdAt ? String(payload.createdAt) : payload?.date ? String(payload.date) : undefined,
});

export const normalizeInquiryResponse = (response: any): InquiryRecord[] => {
  if (Array.isArray(response)) {
    return sortByCreatedAtDesc(response.map(normalizeInquiryRecord));
  }

  if (Array.isArray(response?.data)) {
    return sortByCreatedAtDesc(response.data.map(normalizeInquiryRecord));
  }

  if (Array.isArray(response?.data?.data)) {
    return sortByCreatedAtDesc(response.data.data.map(normalizeInquiryRecord));
  }

  const payload = unwrapApiData<any>(response);
  return sortByCreatedAtDesc(extractPageItems<any>(payload).map(normalizeInquiryRecord));
};

const mergeUniqueInquiries = (groups: InquiryRecord[][]): InquiryRecord[] => {
  const byId = new Map<number, InquiryRecord>();

  groups.flat().forEach((item) => {
    if (!item?.inquiryId) return;
    const existing = byId.get(item.inquiryId);
    if (!existing) {
      byId.set(item.inquiryId, item);
      return;
    }

    const existingTime = existing.createdAt ? new Date(existing.createdAt).getTime() : 0;
    const candidateTime = item.createdAt ? new Date(item.createdAt).getTime() : 0;
    byId.set(item.inquiryId, candidateTime >= existingTime ? { ...existing, ...item } : { ...item, ...existing });
  });

  return sortByCreatedAtDesc(Array.from(byId.values()));
};

const inquiryCache = new Map<InquiryStatusFilter, { expiresAt: number; data: InquiryRecord[] }>();
const inquiryInflight = new Map<InquiryStatusFilter, Promise<InquiryRecord[]>>();

const cloneCached = (items: InquiryRecord[]) => items.map((item) => ({ ...item }));

const getCachedInquiries = (status: InquiryStatusFilter) => {
  const cached = inquiryCache.get(status);
  if (!cached || cached.expiresAt <= Date.now()) {
    if (cached) inquiryCache.delete(status);
    return null;
  }

  return cloneCached(cached.data);
};

const setCachedInquiries = (status: InquiryStatusFilter, items: InquiryRecord[]) => {
  inquiryCache.set(status, {
    expiresAt: Date.now() + INQUIRY_CACHE_TTL_MS,
    data: cloneCached(items),
  });
};

const clearInquiryCache = () => {
  inquiryCache.clear();
  inquiryInflight.clear();
};

const runCachedRequest = async (
  status: InquiryStatusFilter,
  loader: () => Promise<InquiryRecord[]>,
  forceRefresh = false
): Promise<InquiryRecord[]> => {
  if (!forceRefresh) {
    const cached = getCachedInquiries(status);
    if (cached) {
      return cached;
    }

    const existingRequest = inquiryInflight.get(status);
    if (existingRequest) {
      return existingRequest.then(cloneCached);
    }
  }

  const request = loader()
    .then((items) => {
      const normalized = mergeUniqueInquiries([items]);
      setCachedInquiries(status, normalized);
      inquiryInflight.delete(status);
      return cloneCached(normalized);
    })
    .catch((error) => {
      inquiryInflight.delete(status);
      throw error;
    });

  inquiryInflight.set(status, request);
  return request.then(cloneCached);
};

export const inquiryService = {
  sendInquiry: async (payload: InquiryRequest) => {
    const response = await api.post('/inquiries/send', payload, authConfig());
    clearInquiryCache();
    return unwrapApiData<InquiryRecord>(response);
  },

  getInquiriesByStatusRaw: async (status: InquiryApiStatus) => {
    console.log('Calling API with status:', status);
    return api.get('/inquiries/my-inquiries', {
      ...authConfig(),
      params: { inquiryStatus: status },
    });
  },

  getInquiriesByStatus: async (status: InquiryApiStatus, forceRefresh = false): Promise<InquiryRecord[]> => {
    return runCachedRequest(
      status,
      async () => {
        const response = await inquiryService.getInquiriesByStatusRaw(status);
        return normalizeInquiryResponse(response);
      },
      forceRefresh
    );
  },

  getAllMyInquiries: async (forceRefresh = false): Promise<InquiryRecord[]> => {
    return runCachedRequest(
      'ALL',
      async () => {
        const groupedResponses = await Promise.all(
          (['NEW', 'READ', 'REPLIED'] as InquiryApiStatus[]).map((status) =>
            inquiryService.getInquiriesByStatus(status, forceRefresh)
          )
        );

        return mergeUniqueInquiries(groupedResponses);
      },
      forceRefresh
    );
  },

  getMyInquiries: async (status: InquiryStatusFilter = 'NEW', forceRefresh = false): Promise<InquiryRecord[]> => {
    const normalizedStatus = normalizeStatus(status);

    if (normalizedStatus === 'ALL') {
      return inquiryService.getAllMyInquiries(forceRefresh);
    }

    if (normalizedStatus === 'NEW' || normalizedStatus === 'READ' || normalizedStatus === 'REPLIED') {
      return inquiryService.getInquiriesByStatus(normalizedStatus, forceRefresh);
    }

    return inquiryService.getInquiriesByStatus('NEW', forceRefresh);
  },

  getDebugApiUrl: (status: InquiryStatusFilter) => {
    if (status === 'ALL') {
      return [
        '/api/v1/inquiries/my-inquiries?inquiryStatus=NEW',
        '/api/v1/inquiries/my-inquiries?inquiryStatus=READ',
        '/api/v1/inquiries/my-inquiries?inquiryStatus=REPLIED',
      ];
    }

    return `/api/v1/inquiries/my-inquiries?inquiryStatus=${status}`;
  },

  hasExistingInquiryForPreceptor: async (
    preceptorId: number | string,
    preceptorDisplayName?: string
  ): Promise<boolean> => {
    const inquiries = await inquiryService.getAllMyInquiries();
    const byIdSubject = `inquiry for preceptor #${preceptorId}`.toLowerCase();
    const byNameSubject = preceptorDisplayName ? `inquiry for ${preceptorDisplayName}`.toLowerCase() : '';

    return inquiries.some((item) => {
      const subject = String(item.subject || '').trim().toLowerCase();
      return subject === byIdSubject || (byNameSubject ? subject === byNameSubject : false);
    });
  },

  markAsRead: async (inquiryId: number | string) => {
    const response = await api.patch(`/inquiries/${inquiryId}/read`, null, authConfig());
    clearInquiryCache();
    return unwrapApiData(response);
  },

  invalidateCache: clearInquiryCache,
};

export default inquiryService;
