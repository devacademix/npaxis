import type { InquiryRecord } from '../services/inquiry';
import type { SubscriptionStatus } from '../services/payment';
import type { PreceptorStatsResponse } from '../services/preceptor';

export interface DashboardKpiStats {
  profileViews: number;
  contactReveals: number;
  totalInquiries: number;
  conversionRate: number;
}

export interface DashboardActivityItem {
  id: string;
  studentName: string;
  subject: string;
  status: string;
  createdAt?: string;
}

export interface DashboardSubscriptionSummary {
  planName: string;
  status: 'Active' | 'Inactive';
  actionLabel: 'Upgrade Plan' | 'Manage Subscription';
  actionPath: '/subscription' | '/billing';
  isActive: boolean;
  isResolved: boolean;
  description: string;
}

const safeNumber = (value: unknown) => {
  const normalized = Number(value);
  return Number.isFinite(normalized) ? normalized : 0;
};

export const mapStatsToKpis = (stats: PreceptorStatsResponse | null | undefined): DashboardKpiStats => {
  const profileViews = safeNumber(stats?.profileViews);
  const contactReveals = safeNumber(stats?.contactReveals);
  const totalInquiries = safeNumber(stats?.inquiries);
  const conversionRate = profileViews > 0 ? (contactReveals / profileViews) * 100 : 0;

  return {
    profileViews,
    contactReveals,
    totalInquiries,
    conversionRate,
  };
};

export const mapRecentInquiries = (inquiries: InquiryRecord[]): DashboardActivityItem[] =>
  inquiries.slice(0, 5).map((item) => ({
    id: `inquiry-${item.inquiryId}`,
    studentName: item.studentName || 'Student inquiry',
    subject: item.subject || 'Untitled Inquiry',
    status: item.status || 'NEW',
    createdAt: item.createdAt,
  }));

export const mapSubscriptionSummary = (
  subscription: SubscriptionStatus | null | undefined
): DashboardSubscriptionSummary => {
  const isResolved = subscription !== undefined;
  const isActive = Boolean(subscription?.accessEnabled);
  const status = isActive ? 'Active' : 'Inactive';
  const normalizedPlanName = isActive
    ? subscription?.planName || subscription?.planCode || 'Active plan'
    : 'No active plan';

  return {
    planName: normalizedPlanName,
    status,
    actionLabel: isActive ? 'Manage Subscription' : 'Upgrade Plan',
    actionPath: isActive ? '/billing' : '/subscription',
    isActive,
    isResolved,
    description: isActive
      ? 'Your premium access is enabled and synced from the current subscription state.'
      : 'Upgrade your plan to unlock premium visibility and enhanced dashboard access.',
  };
};
