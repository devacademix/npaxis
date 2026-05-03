import { useEffect, useMemo, useState } from 'react';
import { useSession } from '../context/SessionContext';
import inquiryService, { type InquiryRecord } from '../services/inquiry';
import paymentService, { type SubscriptionStatus } from '../services/payment';
import { preceptorService, type LoggedInPreceptorUser, type PreceptorStatsResponse } from '../services/preceptor';
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
  error: string | null;
}

const emptyStats: DashboardKpiStats = {
  profileViews: 0,
  contactReveals: 0,
  totalInquiries: 0,
  conversionRate: 0,
};

export const useDashboardData = (): UseDashboardDataResult => {
  const { currentUser, role } = useSession();
  const [user, setUser] = useState<LoggedInPreceptorUser | null>(null);
  const [statsResponse, setStatsResponse] = useState<PreceptorStatsResponse | null>(null);
  const [inquiries, setInquiries] = useState<InquiryRecord[]>([]);
  const [subscriptionStatus, setSubscriptionStatus] = useState<SubscriptionStatus | null | undefined>(undefined);
  const [hasResolvedSubscription, setHasResolvedSubscription] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (role !== 'PRECEPTOR') return;

    let isCancelled = false;

    const loadDashboardData = async () => {
      try {
        setIsLoading(true);
        setError(null);
        setHasResolvedSubscription(false);
        setSubscriptionStatus(undefined);

        const resolvedUser = currentUser
          ? {
              userId: currentUser.userId,
              displayName: currentUser.displayName,
              email: currentUser.email,
            }
          : await preceptorService.getLoggedInUser();

        if (isCancelled) return;
        setUser(resolvedUser);

        const [stats, inquiryList, subscription] = await Promise.all([
          preceptorService.getStats(resolvedUser.userId).catch(() => null),
          inquiryService.getMyInquiries().catch(() => [] as InquiryRecord[]),
          paymentService.getSubscriptionStatus().catch(() => null),
        ]);

        if (isCancelled) return;

        setStatsResponse(stats);
        setInquiries(Array.isArray(inquiryList) ? inquiryList : []);
        setSubscriptionStatus(subscription);
        setHasResolvedSubscription(true);
      } catch (err: any) {
        if (isCancelled) return;
        setError(err?.message || 'Failed to load dashboard.');
        setStatsResponse(null);
        setInquiries([]);
        setSubscriptionStatus(undefined);
        setHasResolvedSubscription(true);
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    loadDashboardData();

    return () => {
      isCancelled = true;
    };
  }, [currentUser, role]);

  const stats = useMemo(() => mapStatsToKpis(statsResponse), [statsResponse]);
  const recentInquiries = useMemo(() => mapRecentInquiries(inquiries), [inquiries]);
  const subscription = useMemo(() => mapSubscriptionSummary(subscriptionStatus), [subscriptionStatus]);

  return {
    user,
    stats: {
      ...emptyStats,
      ...stats,
    },
    recentInquiries,
    allInquiries: inquiries,
    subscription,
    rawSubscription: subscriptionStatus,
    isLoading,
    isSubscriptionLoading: !hasResolvedSubscription,
    error,
  };
};

export default useDashboardData;
