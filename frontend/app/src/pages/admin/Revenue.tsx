import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import RevenueCard from '../../components/admin/RevenueCard';
import RevenueChart, { type RevenueChartPoint } from '../../components/admin/RevenueChart';
import { authService } from '../../services/auth';
import {
  adminService,
  type PaymentHistoryItem,
  type PreceptorAnalytics,
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

const dateRangeMap: Record<string, number> = {
  all: 0,
  '7d': 7,
  '30d': 30,
  '90d': 90,
};

const Revenue: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';
  const navigate = useNavigate();

  const [preceptorId, setPreceptorId] = useState('1');
  const [statusFilter, setStatusFilter] = useState('all');
  const [dateRange, setDateRange] = useState('30d');

  const [summary, setSummary] = useState<RevenueSummary>(defaultSummary);
  const [payments, setPayments] = useState<PaymentHistoryItem[]>([]);
  const [revenueByPreceptor, setRevenueByPreceptor] = useState<any[]>([]);
  const [revenueTrend, setRevenueTrend] = useState<RevenueChartPoint[]>([]);
  const [transactionTrend, setTransactionTrend] = useState<RevenueChartPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [inlineMessage, setInlineMessage] = useState<string | null>(null);

  const formatCurrency = (amount: number) =>
    new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount || 0);

  const formatDate = (dateValue: string) => {
    const date = new Date(dateValue);
    if (Number.isNaN(date.getTime())) return 'N/A';
    return date.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  const normalizeStatus = (status: string) => {
    const value = (status || '').toLowerCase();
    if (value.includes('paid')) return 'Paid';
    if (value.includes('pending')) return 'Pending';
    if (value.includes('fail')) return 'Failed';
    return status || 'Pending';
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
      label: item.date.toLocaleDateString('en-IN', { month: 'short' }),
      value: Number(item.revenue.toFixed(0)),
    }));

    const transactionData: RevenueChartPoint[] = sorted.map((item) => ({
      label: item.date.toLocaleDateString('en-IN', { month: 'short' }),
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

    const [statsResult, paymentsResult, analyticsResult] = await Promise.allSettled([
      adminService.getRevenueStats(),
      adminService.getPaymentHistory(preceptorId),
      adminService.getPreceptorAnalytics(preceptorId),
    ]);
    const revenueByPreceptorResult = await Promise.allSettled([
      adminService.getRevenueByPreceptor({ page: 0, size: 20 }),
    ]);

    const failedCalls: string[] = [];
    let stats: RevenueStatsApi = {};
    let paymentList: PaymentHistoryItem[] = [];
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

    if (analyticsResult.status === 'fulfilled') {
      analytics = analyticsResult.value;
    } else {
      failedCalls.push(analyticsResult.reason?.message || 'Analytics');
    }

    const summaryData = getSummaryFromData(stats, paymentList);
    const derivedTrends = buildTrendsFromPayments(paymentList);
    const revenueFromAnalytics = mapAnalyticsTrend(analytics, 'revenueTrend');
    const transactionsFromAnalytics = mapAnalyticsTrend(analytics, 'transactionTrend');

    setSummary(summaryData);
    setPayments(paymentList);
    setRevenueTrend(revenueFromAnalytics.length ? revenueFromAnalytics : derivedTrends.revenueData);
    setTransactionTrend(transactionsFromAnalytics.length ? transactionsFromAnalytics : derivedTrends.transactionData);
    if (revenueByPreceptorResult[0].status === 'fulfilled') {
      setRevenueByPreceptor(Array.isArray(revenueByPreceptorResult[0].value) ? revenueByPreceptorResult[0].value : []);
    }

    if (failedCalls.length > 0) {
      const normalized = failedCalls.map((message) => {
        const lower = String(message).toLowerCase();
        if (lower.includes('jwt expired') || lower.includes('session expired')) {
          return 'Session expired. Please login again.';
        }
        return String(message);
      });
      const uniqueFailures = Array.from(new Set(normalized));
      setError(`Some data could not be loaded: ${uniqueFailures.join(' | ')}`);
    }

    setIsLoading(false);
  };

  useEffect(() => {
    if (!isAdmin) return;
    loadRevenueData();
  }, [preceptorId, isAdmin]);

  const filteredPayments = useMemo(() => {
    return payments.filter((payment) => {
      const normalized = normalizeStatus(payment.status).toLowerCase();
      if (statusFilter !== 'all' && normalized !== statusFilter) return false;

      const days = dateRangeMap[dateRange] ?? 0;
      if (days <= 0) return true;

      const date = new Date(payment.date);
      if (Number.isNaN(date.getTime())) return false;

      const threshold = new Date();
      threshold.setDate(threshold.getDate() - days);
      return date >= threshold;
    });
  }, [payments, statusFilter, dateRange]);

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  const handleInvoiceAction = (invoiceUrl?: string, download?: boolean) => {
    if (!invoiceUrl) {
      setInlineMessage('Invoice is not available for this transaction.');
      return;
    }

    if (download) {
      const anchor = document.createElement('a');
      anchor.href = invoiceUrl;
      anchor.target = '_blank';
      anchor.rel = 'noopener noreferrer';
      anchor.download = 'invoice';
      anchor.click();
      return;
    }

    window.open(invoiceUrl, '_blank', 'noopener,noreferrer');
  };

  if (!isAdmin) {
    return <Navigate to="/login" replace />;
  }

  const showEmptyPayments = !isLoading && filteredPayments.length === 0;

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
            placeholder="Enter preceptor ID"
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
          <h3 className="text-lg font-bold text-slate-900">Payment History</h3>
          {!isLoading ? (
            <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">
              {filteredPayments.length} record(s)
            </span>
          ) : null}
        </div>

        {isLoading ? (
          <div className="flex items-center gap-3 py-10 text-sm text-slate-500">
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
            Loading payment history...
          </div>
        ) : showEmptyPayments ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 p-10 text-center">
            <p className="text-base font-semibold text-slate-700">No transactions found</p>
            <p className="mt-2 text-sm text-slate-500">Try adjusting date range/status filters or preceptor ID.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] text-left">
              <thead className="border-b border-slate-200">
                <tr>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Preceptor Name</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Amount</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Date</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredPayments.map((payment) => {
                  const status = normalizeStatus(payment.status);
                  const statusClass =
                    status === 'Paid'
                      ? 'bg-emerald-100 text-emerald-700'
                      : status === 'Failed'
                      ? 'bg-red-100 text-red-700'
                      : 'bg-amber-100 text-amber-700';

                  return (
                    <tr key={payment.id} className="transition-colors hover:bg-slate-50">
                      <td className="px-3 py-3 text-sm font-semibold text-slate-800">{payment.preceptorName}</td>
                      <td className="px-3 py-3 text-sm font-bold text-slate-900">{formatCurrency(payment.amount)}</td>
                      <td className="px-3 py-3">
                        <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${statusClass}`}>
                          {status}
                        </span>
                      </td>
                      <td className="px-3 py-3 text-sm text-slate-600">{formatDate(payment.date)}</td>
                      <td className="px-3 py-3">
                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => handleInvoiceAction(payment.invoiceUrl, false)}
                            className="rounded-md border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                          >
                            View
                          </button>
                          <button
                            type="button"
                            onClick={() => handleInvoiceAction(payment.invoiceUrl, true)}
                            className="rounded-md bg-blue-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-blue-700"
                          >
                            Download Invoice
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="mt-8 rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-bold text-slate-900">Revenue by Preceptor</h3>
          <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">
            {revenueByPreceptor.length} record(s)
          </span>
        </div>
        {revenueByPreceptor.length === 0 ? (
          <p className="text-sm text-slate-500">No preceptor revenue breakdown available.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left">
              <thead className="border-b border-slate-200">
                <tr>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Preceptor</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Revenue</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-3 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">Meta</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {revenueByPreceptor.map((item, index) => (
                  <tr key={item?.userId ?? item?.id ?? index}>
                    <td className="px-3 py-3 text-sm font-semibold text-slate-800">
                      {item?.displayName ?? item?.preceptorName ?? item?.name ?? `Preceptor ${index + 1}`}
                    </td>
                    <td className="px-3 py-3 text-sm font-bold text-slate-900">
                      {formatCurrency(Number(item?.amount ?? item?.revenue ?? item?.totalRevenue ?? 0))}
                    </td>
                    <td className="px-3 py-3 text-sm text-slate-600">
                      {String(item?.status ?? item?.verificationStatus ?? 'N/A')}
                    </td>
                    <td className="px-3 py-3 text-xs text-slate-500">
                      {item?.email ?? item?.location ?? item?.specialty ?? 'No extra metadata'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default Revenue;
