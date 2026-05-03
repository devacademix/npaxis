import React, { useEffect, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import RevenueCard from '../../components/admin/RevenueCard';
import RevenueChart, { type RevenueChartPoint } from '../../components/admin/RevenueChart';
import { authService } from '../../services/auth';
import {
  adminService,
  type PaymentHistoryItem,
  type PreceptorAnalytics,
  type PreceptorRevenueItem,
  type RevenueStatsApi,
} from '../../services/admin';

interface RevenueSummary {
  totalRevenue: number;
  monthlyRevenue: number;
  totalTransactions: number;
  activePremiumUsers: number;
}

const defaultSummary: RevenueSummary = {
  totalRevenue: 0,
  monthlyRevenue: 0,
  totalTransactions: 0,
  activePremiumUsers: 0,
};

const Revenue: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';
  const navigate = useNavigate();

  const [preceptorId, setPreceptorId] = useState('');
  const [appliedPreceptorId, setAppliedPreceptorId] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [dateRange, setDateRange] = useState('30d');

  const [summary, setSummary] = useState<RevenueSummary>(defaultSummary);
  const [preceptorRevenue, setPreceptorRevenue] = useState<PreceptorRevenueItem[]>([]);
  const [revenuePage, setRevenuePage] = useState(0);
  const [revenuePageSize] = useState(10);
  const [revenueTotalPages, setRevenueTotalPages] = useState(0);
  const [revenueTotalElements, setRevenueTotalElements] = useState(0);
  const [revenueTrend, setRevenueTrend] = useState<RevenueChartPoint[]>([]);
  const [transactionTrend, setTransactionTrend] = useState<RevenueChartPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [inlineMessage, setInlineMessage] = useState<string | null>(null);

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0,
    }).format(amount || 0);

  const formatDate = (dateValue: string) => {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) return 'N/A';
    return date.toLocaleDateString('en-US', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const buildTrendsFromPayments = (paymentList: PaymentHistoryItem[]) => {
    const monthlyMap = new Map<string, { date: Date; revenue: number; transactions: number }>();

    paymentList.forEach((payment) => {
      const date = new Date(payment.date);
      if (Number.isNaN(date.getTime())) return;

      const monthStart = new Date(date.getFullYear(), date.getMonth(), 1);
      const key = `${monthStart.getFullYear()}-${monthStart.getMonth()}`;
      const existing = monthlyMap.get(key);

      if (existing) {
        existing.revenue += payment.amount || 0;
        existing.transactions += 1;
      } else {
        monthlyMap.set(key, {
          date: monthStart,
          revenue: payment.amount || 0,
          transactions: 1,
        });
      }
    });

    const sorted = Array.from(monthlyMap.values())
      .sort((a, b) => a.date.getTime() - b.date.getTime())
      .slice(-6);

    const revenueData: RevenueChartPoint[] = sorted.map((item) => ({
      label: item.date.toLocaleDateString('en-US', { month: 'short' }),
      value: Number(item.revenue.toFixed(0)),
    }));

    const transactionData: RevenueChartPoint[] = sorted.map((item) => ({
      label: item.date.toLocaleDateString('en-US', { month: 'short' }),
      value: item.transactions,
    }));

    return { revenueData, transactionData };
  };

  const getSummaryFromData = (stats: RevenueStatsApi, paymentList: PaymentHistoryItem[]): RevenueSummary => {
    const totalRevenue =
      Number(stats.revenue ?? 0) || paymentList.reduce((sum, payment) => sum + (payment.amount || 0), 0);

    const now = new Date();
    const monthlyRevenueFromPayments = paymentList.reduce((sum, payment) => {
      const date = new Date(payment.date);
      if (Number.isNaN(date.getTime())) return sum;
      if (date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear()) {
        return sum + (payment.amount || 0);
      }
      return sum;
    }, 0);

    return {
      totalRevenue,
      monthlyRevenue: Number(stats.monthlyRevenue ?? monthlyRevenueFromPayments ?? 0),
      totalTransactions: Number(stats.totalTransactions ?? paymentList.length ?? 0),
      activePremiumUsers: Number(stats.premiumCount ?? stats.premiumUsers ?? 0),
    };
  };

  const mapAnalyticsTrend = (
    analytics: PreceptorAnalytics,
    key: 'revenueTrend' | 'transactionTrend'
  ): RevenueChartPoint[] => {
    const rawTrend = analytics[key];
    if (!Array.isArray(rawTrend)) return [];

    return rawTrend
      .map((item: any) => ({
        label: String(item?.label ?? item?.month ?? item?.name ?? ''),
        value: Number(item?.value ?? item?.revenue ?? item?.transactions ?? 0),
      }))
      .filter((item) => item.label);
  };

  const loadRevenueData = async () => {
    setIsLoading(true);
    setError(null);
    setInlineMessage(null);

    const requests: Promise<any>[] = [
      adminService.getRevenueStats(),
      adminService.getPaymentHistory(appliedPreceptorId),
      adminService.getRevenueByPreceptor({ page: revenuePage, size: revenuePageSize }),
    ];

    if (appliedPreceptorId.trim()) {
      requests.push(adminService.getPreceptorAnalytics(appliedPreceptorId.trim()));
    }

    const results = await Promise.allSettled(requests);
    const [statsResult, paymentsResult, revenueByPreceptorResult, analyticsResult] = results;

    const failedCalls: string[] = [];
    let stats: RevenueStatsApi = {};
    let paymentList: PaymentHistoryItem[] = [];
    let revenueByPreceptor: { items: PreceptorRevenueItem[]; totalPages: number; totalElements: number } = {
      items: [],
      totalPages: 0,
      totalElements: 0,
    };
    let analytics: PreceptorAnalytics = {};

    if (statsResult.status === 'fulfilled') {
      stats = statsResult.value;
    } else {
      failedCalls.push(statsResult.reason?.message || 'Revenue stats');
    }

    if (paymentsResult.status === 'fulfilled') {
      paymentList = paymentsResult.value;
    } else {
      failedCalls.push(paymentsResult.reason?.message || 'Payment history');
    }

    if (revenueByPreceptorResult.status === 'fulfilled') {
      revenueByPreceptor = revenueByPreceptorResult.value;
    } else {
      failedCalls.push(revenueByPreceptorResult.reason?.message || 'Revenue by preceptor');
    }

    if (analyticsResult?.status === 'fulfilled') {
      analytics = analyticsResult.value;
    } else if (analyticsResult?.status === 'rejected') {
      failedCalls.push(analyticsResult.reason?.message || 'Analytics');
    }

    const summaryData = getSummaryFromData(stats, paymentList);
    const derivedTrends = buildTrendsFromPayments(paymentList);
    const revenueFromAnalytics = mapAnalyticsTrend(analytics, 'revenueTrend');
    const transactionsFromAnalytics = mapAnalyticsTrend(analytics, 'transactionTrend');

    setSummary(summaryData);
    setPreceptorRevenue(revenueByPreceptor.items);
    setRevenueTotalPages(revenueByPreceptor.totalPages);
    setRevenueTotalElements(revenueByPreceptor.totalElements);
    setRevenueTrend(revenueFromAnalytics.length ? revenueFromAnalytics : derivedTrends.revenueData);
    setTransactionTrend(transactionsFromAnalytics.length ? transactionsFromAnalytics : derivedTrends.transactionData);

    if (failedCalls.length > 0) {
      const normalized = failedCalls.map((message) => {
        const lower = String(message).toLowerCase();
        if (lower.includes('jwt expired') || lower.includes('session expired')) {
          return 'Session expired. Please login again.';
        }
        if (lower.includes('preceptor not found')) {
          return 'No analytics found for the selected preceptor ID.';
        }
        return String(message);
      });
      const uniqueFailures = Array.from(new Set(normalized));
      if (appliedPreceptorId.trim()) {
        setError(`Some data could not be loaded: ${uniqueFailures.join(' | ')}`);
      } else {
        setInlineMessage(uniqueFailures.join(' | '));
      }
    }

    setIsLoading(false);
  };

  useEffect(() => {
    if (!isAdmin) return;
    loadRevenueData();
  }, [appliedPreceptorId, isAdmin, revenuePage, revenuePageSize]);

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  if (!isAdmin) {
    return <Navigate to="/login" replace />;
  }

  const showEmptyPreceptorRevenue = !isLoading && preceptorRevenue.length === 0;

  return (
    <AdminLayout>
      <div className="mb-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight text-on-surface font-headline">Revenue Dashboard</h1>
          <p className="mt-2 text-slate-500">Platform revenue analytics, payment history, and financial insights.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">
            Admin: {localStorage.getItem('displayName') || 'System Admin'}
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-full bg-slate-900 px-5 py-2.5 text-sm font-bold text-white hover:bg-slate-800"
          >
            Logout
          </button>
        </div>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-3 md:grid-cols-3">
        <div className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Preceptor ID</label>
          <input
            type="text"
            value={preceptorId}
            onChange={(event) => setPreceptorId(event.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            placeholder="Optional: enter preceptor ID"
          />
        </div>

        <div className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Date Range</label>
          <select
            value={dateRange}
            onChange={(event) => setDateRange(event.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            <option value="all">All Time</option>
            <option value="7d">Last 7 days</option>
            <option value="30d">Last 30 days</option>
            <option value="90d">Last 90 days</option>
          </select>
        </div>

        <div className="rounded-xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Status</label>
          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            <option value="all">All</option>
            <option value="paid">Paid</option>
            <option value="pending">Pending</option>
            <option value="failed">Failed</option>
          </select>
        </div>
      </div>

      <div className="mb-6 flex items-center gap-3">
        <button
          type="button"
          onClick={() => setAppliedPreceptorId(preceptorId.trim())}
          className="rounded-full bg-blue-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-700"
        >
          Apply Filters
        </button>
        {preceptorId.trim() ? (
          <button
            type="button"
            onClick={() => {
              setPreceptorId('');
              setAppliedPreceptorId('');
              setInlineMessage(null);
              setError(null);
            }}
            className="rounded-full border border-slate-200 px-5 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            Clear Preceptor
          </button>
        ) : null}
      </div>

      {error ? (
        <div className="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
          {error}
        </div>
      ) : null}

      {inlineMessage ? (
        <div className="mb-6 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-800">
          {inlineMessage}
        </div>
      ) : null}

      {isLoading ? (
        <div className="mb-8 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          {[...Array(4)].map((_, index) => (
            <div key={index} className="h-36 animate-pulse rounded-xl bg-slate-200/70" />
          ))}
        </div>
      ) : (
        <div className="mb-8 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <RevenueCard
            title="Total Revenue"
            value={formatCurrency(summary.totalRevenue)}
            subtitle="Across all transactions"
            icon="savings"
            accentClass="bg-emerald-50 text-emerald-700"
          />
          <RevenueCard
            title="Monthly Revenue"
            value={formatCurrency(summary.monthlyRevenue)}
            subtitle="Current month earnings"
            icon="calendar_month"
            accentClass="bg-blue-50 text-blue-700"
          />
          <RevenueCard
            title="Total Transactions"
            value={summary.totalTransactions.toLocaleString()}
            subtitle="Payment records processed"
            icon="receipt_long"
            accentClass="bg-indigo-50 text-indigo-700"
          />
          <RevenueCard
            title="Active Premium Users"
            value={summary.activePremiumUsers.toLocaleString()}
            subtitle="Premium preceptors currently active"
            icon="workspace_premium"
            accentClass="bg-amber-50 text-amber-700"
          />
        </div>
      )}

      <div className="mb-8 grid grid-cols-1 gap-6 xl:grid-cols-2">
        <RevenueChart title="Revenue Over Time" type="line" data={revenueTrend} valuePrefix="₹" />
        <RevenueChart title="Transactions Per Month" type="bar" data={transactionTrend} />
      </div>

      <div className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-bold text-slate-900">Revenue By Preceptor</h3>
          {!isLoading ? (
            <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {revenueTotalElements} record(s)
            </span>
          ) : null}
        </div>

        {isLoading ? (
          <div className="flex items-center gap-3 py-10 text-sm text-slate-500">
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
            Loading preceptor revenue...
          </div>
        ) : showEmptyPreceptorRevenue ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 p-10 text-center">
            <p className="text-base font-semibold text-slate-700">No preceptor revenue found</p>
            <p className="mt-2 text-sm text-slate-500">The revenue-by-preceptor report is currently empty.</p>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="overflow-x-auto">
            <table className="w-full min-w-[860px] text-left">
              <thead className="border-b border-slate-200">
                <tr>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Preceptor Name</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Subscription Tier</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Successful Revenue</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Failed Revenue</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Last Transaction Date</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {preceptorRevenue.map((item) => {
                  return (
                    <tr key={`${item.userId}-${item.displayName}`} className="transition-colors hover:bg-slate-50">
                      <td className="px-3 py-3 text-sm font-semibold text-slate-800">{item.displayName}</td>
                      <td className="px-3 py-3 text-sm text-slate-600">{item.subscriptionTier}</td>
                      <td className="px-3 py-3 text-sm font-bold text-emerald-700">{formatCurrency(item.successfulRevenue)}</td>
                      <td className="px-3 py-3 text-sm font-bold text-rose-700">{formatCurrency(item.failedRevenue)}</td>
                      <td className="px-3 py-3 text-sm text-slate-600">{item.lastTransactionDate ? formatDate(item.lastTransactionDate) : 'N/A'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
            <div className="flex items-center justify-between gap-3">
              <p className="text-sm text-slate-500">
                Page {revenuePage + 1} of {Math.max(revenueTotalPages, 1)}
              </p>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={revenuePage <= 0}
                  onClick={() => setRevenuePage((current) => Math.max(current - 1, 0))}
                  className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-slate-50"
                >
                  Previous
                </button>
                <button
                  type="button"
                  disabled={revenuePage + 1 >= Math.max(revenueTotalPages, 1)}
                  onClick={() => setRevenuePage((current) => current + 1)}
                  className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-slate-50"
                >
                  Next
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default Revenue;
