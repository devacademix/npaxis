import { useQuery } from '@tanstack/react-query';
import inquiryService, { type InquiryRecord, type InquiryStatusFilter } from '../services/inquiry';

const getErrorMessage = (error: any) =>
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  error?.message ||
  'Failed to load inquiries.';

export const inquiriesQueryKey = (scope: string, status: InquiryStatusFilter) => ['inquiries', scope, status] as const;

interface UseInquiriesOptions {
  scope: string;
  enabled?: boolean;
  refetchInterval?: number;
}

export const useInquiries = (
  status: InquiryStatusFilter,
  options: UseInquiriesOptions
) => {
  const { enabled = true, refetchInterval = 5000, scope } = options;

  return useQuery<InquiryRecord[], Error>({
    queryKey: inquiriesQueryKey(scope, status),
    queryFn: () => inquiryService.getMyInquiries(status, true),
    enabled,
    refetchInterval: enabled ? refetchInterval : false,
    placeholderData: (previousData) => previousData,
    meta: {
      errorMessage: getErrorMessage,
    },
  });
};

export default useInquiries;
