import { useMemo, useState } from 'react';
import { useSession } from '../context/SessionContext';
import inquiryService, { type InquiryRecord, type InquiryStatusFilter } from '../services/inquiry';
import { useInquiries } from './useInquiries';

export interface PreceptorInquiryDiagnostics {
  authRole: string | null;
  sessionRole: string | null;
  userId: number | null;
  apiUrl: string | string[];
  itemCount: number;
  tokenPresent: boolean;
  rawResponse: unknown;
  queryStatus: string;
}

interface UsePreceptorInquiriesOptions {
  initialStatus?: InquiryStatusFilter;
}

interface UsePreceptorInquiriesResult {
  inquiries: InquiryRecord[];
  loading: boolean;
  refreshing: boolean;
  error: string | null;
  status: InquiryStatusFilter;
  diagnostics: PreceptorInquiryDiagnostics;
  setStatus: (status: InquiryStatusFilter) => void;
  refetch: (statusOverride?: InquiryStatusFilter, forceRefresh?: boolean) => Promise<void>;
}

const buildApiUrl = (status: InquiryStatusFilter) => inquiryService.getDebugApiUrl(status);

const getErrorMessage = (error: any) =>
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  error?.message ||
  'Failed to load inquiries.';

export const usePreceptorInquiries = (
  options: UsePreceptorInquiriesOptions = {}
): UsePreceptorInquiriesResult => {
  const { currentUser, role, isLoading: isSessionLoading } = useSession();
  const [status, setStatus] = useState<InquiryStatusFilter>(options.initialStatus ?? 'NEW');
  const query = useInquiries(status, {
    scope: 'preceptor',
    enabled: !isSessionLoading && role === 'PRECEPTOR',
  });

  const inquiries = query.data ?? [];
  const loading = isSessionLoading || (query.isLoading && inquiries.length === 0);
  const refreshing = query.isFetching && !loading;
  const error = query.error ? getErrorMessage(query.error) : null;

  const diagnostics = useMemo<PreceptorInquiryDiagnostics>(() => {
    const authRole = typeof window !== 'undefined' ? localStorage.getItem('role') : null;
    const apiUrl = buildApiUrl(status);
    const tokenPresent = typeof window !== 'undefined' ? Boolean(localStorage.getItem('accessToken')) : false;

    console.log('Auth role:', authRole);
    console.log('Inquiries API URL:', apiUrl);
    console.log('Inquiries API raw:', query.data ?? query.error ?? null);

    return {
      authRole,
      sessionRole: role,
      userId: currentUser?.userId ?? null,
      apiUrl,
      itemCount: inquiries.length,
      tokenPresent,
      rawResponse: query.data ?? (query.error ? { message: getErrorMessage(query.error) } : null),
      queryStatus: query.status,
    };
  }, [currentUser?.userId, inquiries.length, query.data, query.error, query.status, role, status]);

  const refetch = async (statusOverride?: InquiryStatusFilter, forceRefresh = false) => {
    const resolvedStatus = statusOverride ?? status;

    if (forceRefresh) {
      inquiryService.invalidateCache();
    }

    if (resolvedStatus !== status) {
      setStatus(resolvedStatus);
      return;
    }

    await query.refetch();
  };

  return {
    inquiries,
    loading,
    refreshing,
    error,
    status,
    diagnostics,
    setStatus,
    refetch,
  };
};

export default usePreceptorInquiries;
