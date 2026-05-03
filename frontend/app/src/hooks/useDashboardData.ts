import { useEffect, useMemo } from 'react';
import { useQueries } from '@tanstack/react-query';
import { useSession } from '../context/SessionContext';
import type { InquiryRecord } from '../services/inquiry';
import paymentService, { type SubscriptionStatus } from '../services/payment';
import { preceptorService, type LoggedInPreceptorUser, type PreceptorStatsResponse } from '../services/preceptor';
import { useInquiries } from './useInquiries';
import {
  mapRecentInquiries,
  mapStatsToKpis,
  mapSubscriptionSummary,
  type DashboardActivityItem,
  type DashboardKpiStats,
  type DashboardSubscriptionSummary,
} from '../utils/preceptorDashboard';

interface UseDashboardDataResult {
  user: LoggedInPreceptorUser | null;
  stats: DashboardKpiStats;
  recentInquiries: DashboardActivityItem[];
  allInquiries: InquiryRecord[];
  subscription: DashboardSubscriptionSummary;
  rawSubscription: SubscriptionStatus | null | undefined;
  isLoading: boolean;
  isSubscriptionLoading: boolean;
  isRefreshing: boolean;
  error: string | null;
}

const emptyStats: DashboardKpiStats = {
  profileViews: 0,
  contactReveals: 0,
  totalInquiries: 0,
  conversionRate: 0,
};

const getErrorMessage = (error: unknown) => {
  const normalized = error as any;
  return normalized?.message || 'Failed to load dashboard.';
};

export const useDashboardData = (): UseDashboardDataResult => {
  const { currentUser, role } = useSession();
  const inquiriesQuery = useInquiries('ALL', {
    scope: 'preceptor-dashboard',
    enabled: role === 'PRECEPTOR',
  });

  const [userQuery, statsQuery, subscriptionQuery] = useQueries({
    queries: [
      {
        queryKey: ['dashboard', 'preceptor', 'user', currentUser?.userId ?? 'session'],
        queryFn: async () => {
          if (currentUser) {
            return {
              userId: currentUser.userId,
              displayName: currentUser.displayName,
              email: currentUser.email,
            } satisfies LoggedInPreceptorUser;
          }

          return preceptorService.getLoggedInUser();
        },
        enabled: role === 'PRECEPTOR',
        staleTime: 60_000,
      },
      {
        queryKey: ['dashboard', 'preceptor', 'stats', currentUser?.userId ?? 'session'],
        queryFn: async () => {
          const resolvedUser =
            currentUser
              ? {
                  userId: currentUser.userId,
                  displayName: currentUser.displayName,
                  email: currentUser.email,
                }
              : await preceptorService.getLoggedInUser();

          return preceptorService.getStats(resolvedUser.userId).catch(() => null as PreceptorStatsResponse | null);
        },
        enabled: role === 'PRECEPTOR',
        refetchInterval: 5_000,
        placeholderData: (previousData: PreceptorStatsResponse | null | undefined) => previousData,
      },
      {
        queryKey: ['dashboard', 'preceptor', 'subscription'],
        queryFn: () => paymentService.getSubscriptionStatus().catch(() => null),
        enabled: role === 'PRECEPTOR',
        refetchInterval: 10_000,
        placeholderData: (previousData: SubscriptionStatus | null | undefined) => previousData,
      },
    ],
  });

  const inquiries = inquiriesQuery.data ?? [];
  const stats = useMemo(
    () => mapStatsToKpis((statsQuery.data as PreceptorStatsResponse | null) ?? null),
    [statsQuery.data]
  );
  const recentInquiries = useMemo(() => mapRecentInquiries(inquiries), [inquiries]);
  const subscription = useMemo(
    () => mapSubscriptionSummary((subscriptionQuery.data as SubscriptionStatus | null | undefined) ?? null),
    [subscriptionQuery.data]
  );

  useEffect(() => {
    if (role !== 'PRECEPTOR') return;
    console.log('Analytics:', statsQuery.data ?? null);
  }, [role, statsQuery.data]);

  useEffect(() => {
    if (role !== 'PRECEPTOR') return;
    console.log('Inquiries:', inquiries);
  }, [inquiries, role]);

  const error = [userQuery.error, statsQuery.error, subscriptionQuery.error, inquiriesQuery.error]
    .map((item) => (item ? getErrorMessage(item) : null))
    .find(Boolean) ?? null;

  return {
    user: (userQuery.data as LoggedInPreceptorUser | null) ?? null,
    stats: {
      ...emptyStats,
      ...stats,
    },
    recentInquiries,
    allInquiries: inquiries,
    subscription,
    rawSubscription: (subscriptionQuery.data as SubscriptionStatus | null | undefined) ?? undefined,
    isLoading:
      role === 'PRECEPTOR' &&
      ((userQuery.isLoading && !userQuery.data) ||
        (statsQuery.isLoading && !statsQuery.data) ||
        (inquiriesQuery.isLoading && !inquiries.length)),
    isSubscriptionLoading: role === 'PRECEPTOR' && subscriptionQuery.isLoading && !subscriptionQuery.data,
    isRefreshing:
      userQuery.isFetching || statsQuery.isFetching || subscriptionQuery.isFetching || inquiriesQuery.isFetching,
    error,
  };
};

export default useDashboardData;
