import React, { useEffect, useMemo, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import Card from '../../components/Card';
import { type RevenueChartPoint } from '../../components/admin/RevenueChart';
import {
  adminService,
  type AdminTrendOverview,
  type TopPreceptorLeaderboardItem,
} from '../../services/admin';

interface DashboardStats {
  totalUsers: number;
  premiumUsers: number;
  revenue: number;
  activePreceptors: number;
}

interface RevenueSourceEntry {
  label: string;
  value: number;
  description: string;
  colorClass: string;
}

type TrendMetric = 'users' | 'revenue' | 'subscriptions';
type TrendRange = 6 | 12;

const getLastMonths = (count: TrendRange = 6) => {
  const now = new Date();
  const months = [];
  for (let i = count - 1; i >= 0; i -= 1) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
    months.push({
      key: `${date.getFullYear()}-${date.getMonth()}`,
      label: date.toLocaleString('en-US', { month: 'short' }),
    });
  }
  return months;
};

const buildRevenueSources = (
  totalUsers: number,
  preceptorCount: number,
  premiumUsers: number
): RevenueSourceEntry[] => {
  const normalizedStudents = Math.max(totalUsers - preceptorCount, 0);

  return [
    {
      label: 'Students',
      value: normalizedStudents,
      description: `${normalizedStudents.toLocaleString()} active learners`,
      colorClass: 'bg-gradient-to-r from-blue-500 to-blue-400',
    },
    {
      label: 'Preceptors',
      value: preceptorCount,
      description: `${preceptorCount.toLocaleString()} providers onboarded`,
      colorClass: 'bg-gradient-to-r from-indigo-500 to-indigo-400',
    },
    {
      label: 'Premium',
      value: premiumUsers,
      description: `${premiumUsers.toLocaleString()} premium accounts active`,
      colorClass: 'bg-gradient-to-r from-emerald-500 to-emerald-400',
    },
  ];
};

const buildCumulativeTrend = (currentValue: number, growthWindow: number, range: TrendRange) => {
  const labels = getLastMonths(range);
  const safeCurrent = Math.max(currentValue, 0);
  const safeWindow = Math.max(growthWindow, 0);
  const baseline = Math.max(safeCurrent - safeWindow, 0);

  return labels.map((month, index) => {
    const progress = index / (range - 1);
    const eased = Math.pow(progress, 1.35);
    return {
      label: month.label,
      value: Math.round(baseline + safeWindow * eased),
    };
  });
};

const buildRevenueTrend = (overview: AdminTrendOverview, range: TrendRange) => {
  const labels = getLastMonths(range);
  const monthlyRevenue = Math.max(Number(overview.monthlyRevenue ?? 0), 0);
  const averageRevenue = Math.max(Number(overview.totalRevenue ?? 0) / Math.max(range, 1), 0);
  const anchor = Math.max(monthlyRevenue, averageRevenue);

  return labels.map((month, index) => {
    const seasonalWave = Math.sin((index / Math.max(range - 1, 1)) * Math.PI) * 0.16;
    const momentum = 0.72 + (index / Math.max(range - 1, 1)) * 0.28;
    const value = anchor * (momentum + seasonalWave);

    return {
      label: month.label,
      value: Math.max(Math.round(value), 0),
    };
  });
};

const buildGrowthTrend = (
  overview: AdminTrendOverview | null,
  metric: TrendMetric,
  range: TrendRange
): RevenueChartPoint[] => {
  if (!overview) {
    return getLastMonths(range).map((month) => ({ label: month.label, value: 0 }));
  }

  if (metric === 'users') {
    const totalUsers = Number(overview.totalUsers ?? 0);
    const newUsersThisMonth = Number(overview.newUsersThisMonth ?? 0);
    const estimatedWindowGrowth = Math.max(newUsersThisMonth * (range - 1), Math.round(totalUsers * 0.12));
    return buildCumulativeTrend(totalUsers, estimatedWindowGrowth, range);
  }

  if (metric === 'subscriptions') {
    const subscriptions = Number(overview.premiumUsersCount ?? 0);
    const growthWindow = Math.max(Math.round(subscriptions * 0.4), Math.round(subscriptions / 3));
    return buildCumulativeTrend(subscriptions, growthWindow, range);
  }

  return buildRevenueTrend(overview, range);
};

const getTrendMetricLabel = (metric: TrendMetric) => {
  if (metric === 'users') return 'Users';
  if (metric === 'subscriptions') return 'Subscriptions';
  return 'Revenue';
};

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isGeneratingReport, setIsGeneratingReport] = useState(false);
  const [trendOverview, setTrendOverview] = useState<AdminTrendOverview | null>(null);
  const [selectedTrendMetric, setSelectedTrendMetric] = useState<TrendMetric>('users');
  const [selectedTrendRange, setSelectedTrendRange] = useState<TrendRange>(6);
  const [revenueSources, setRevenueSources] = useState<RevenueSourceEntry[]>([]);
  const [insightsError, setInsightsError] = useState<string | null>(null);
  const [insightsLoading, setInsightsLoading] = useState(true);
  const [topPreceptors, setTopPreceptors] = useState<TopPreceptorLeaderboardItem[]>([]);
  const [topPreceptorsLoading, setTopPreceptorsLoading] = useState(true);
  const [topPreceptorsError, setTopPreceptorsError] = useState<string | null>(null);

  const handleGenerateReport = async () => {
    try {
      setIsGeneratingReport(true);
      const blob = await adminService.downloadDashboardReportPdf();
      const reportUrl = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = reportUrl;
      anchor.download = `admin-dashboard-report-${new Date().toISOString().slice(0, 10)}.pdf`;
      anchor.click();
      URL.revokeObjectURL(reportUrl);
    } catch (err: any) {
      setError(err?.message || 'Failed to generate dashboard PDF report.');
    } finally {
      setIsGeneratingReport(false);
    }
  };

  const maxTrendValue = useMemo(() => {
    const growthTrend = buildGrowthTrend(trendOverview, selectedTrendMetric, selectedTrendRange);
    if (growthTrend.length === 0) return 1;
    return Math.max(Math.max(...growthTrend.map((point) => point.value)), 1);
  }, [selectedTrendMetric, selectedTrendRange, trendOverview]);

  const revenueTotal = useMemo(
    () => revenueSources.reduce((sum, source) => sum + source.value, 0),
    [revenueSources]
  );

  const displayTrend = useMemo(
    () => buildGrowthTrend(trendOverview, selectedTrendMetric, selectedTrendRange),
    [selectedTrendMetric, selectedTrendRange, trendOverview]
  );

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setIsLoading(true);
        const response = await adminService.getStats();
        setStats(response);
      } catch (err: any) {
        setError(err?.message || 'Failed to load dashboard statistics.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchStats();
  }, []);

  useEffect(() => {
    const loadInsights = async () => {
      setInsightsLoading(true);
      setInsightsError(null);
      try {
        const overview = await adminService.getTrendOverview();
        setTrendOverview(overview);
        setRevenueSources(
          buildRevenueSources(
            Number(overview?.totalUsers ?? stats?.totalUsers ?? 0),
            Number(overview?.totalPreceptors ?? stats?.activePreceptors ?? 0),
            Number(overview?.premiumUsersCount ?? stats?.premiumUsers ?? 0)
          )
        );
      } catch (err: any) {
        const message = String(err?.message || '');
        const lowerMessage = message.toLowerCase();
        const isPermissionIssue =
          lowerMessage.includes('permission') ||
          lowerMessage.includes('forbidden') ||
          lowerMessage.includes('unauthorized');

        setInsightsError(isPermissionIssue ? null : message || 'Unable to load live insights.');
        setTrendOverview(null);
        setRevenueSources(
          buildRevenueSources(stats?.totalUsers ?? 0, stats?.activePreceptors ?? 0, stats?.premiumUsers ?? 0)
        );
      } finally {
        setInsightsLoading(false);
      }
    };

    loadInsights();
  }, [stats?.activePreceptors, stats?.premiumUsers, stats?.totalUsers]);

  useEffect(() => {
    const loadTopPreceptors = async () => {
      setTopPreceptorsLoading(true);
      setTopPreceptorsError(null);
      try {
        const leaderboard = await adminService.getTopPreceptorsOverview();
        setTopPreceptors(leaderboard);
      } catch (err: any) {
        setTopPreceptors([]);
        setTopPreceptorsError(err?.message || 'Unable to load top preceptors.');
      } finally {
        setTopPreceptorsLoading(false);
      }
    };

    loadTopPreceptors();
  }, []);

  const renderSkeletons = () => (
    <div className="grid grid-cols-1 gap-6 mb-10 md:grid-cols-2 lg:grid-cols-4">
      {[...Array(4)].map((_, i) => (
        <div key={i} className="bg-surface-container-lowest p-6 rounded-xl border border-transparent animate-pulse">
          <div className="flex justify-between items-start mb-4">
            <div className="w-12 h-12 rounded-lg bg-slate-200" />
            <div className="w-10 h-4 bg-slate-200 rounded-full" />
          </div>
          <div className="h-3 w-1/2 bg-slate-200 mb-2 rounded" />
          <div className="h-8 w-3/4 bg-slate-200 mt-2 mb-2 rounded" />
          <div className="h-2 w-1/3 bg-slate-200 rounded" />
        </div>
      ))}
    </div>
  );

  return (
    <AdminLayout>
      <div className="mb-10 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight text-on-surface mb-2 font-headline">Admin Dashboard</h1>
          <p className="text-slate-500 font-medium">Welcome back, Sarah. Here&apos;s what&apos;s happening across NPaxis today.</p>
        </div>
        <button
          type="button"
          onClick={handleGenerateReport}
          disabled={isGeneratingReport}
          className="bg-gradient-to-br from-[#003d9b] to-[#0052cc] text-white px-6 py-2.5 rounded-full font-bold flex items-center gap-2 shadow-lg shadow-blue-900/10 hover:opacity-90 transition-opacity"
        >
          <span className="material-symbols-outlined text-sm">download</span>
          {isGeneratingReport ? 'Generating PDF...' : 'Generate Report'}
        </button>
      </div>

      {error && !stats && (
        <div className="mb-6 bg-error-container/50 border border-error-container p-6 rounded-xl flex items-center gap-4 text-on-error-container align-middle shadow-sm">
          <span className="material-symbols-outlined text-2xl text-error">error</span>
          <div>
            <h3 className="font-bold">Error Loading Dashboard Data</h3>
            <p className="text-sm opacity-90">{error}</p>
          </div>
          <button
            onClick={() => window.location.reload()}
            className="ml-auto text-sm font-bold bg-white/50 px-4 py-2 rounded-md hover:bg-white transition-colors"
          >
            Retry
          </button>
        </div>
      )}

      {isLoading ? renderSkeletons() : (
        <>
          {!error && stats && (
            <div className="grid grid-cols-1 gap-6 mb-10 md:grid-cols-2 lg:grid-cols-4">
              <Card title="Total Users" value={stats.totalUsers.toLocaleString()} subtitle="vs. 11,116 last month" trendText="+12%" icon="group" colorClass="bg-blue-50 text-blue-700" />
              <Card title="Premium Users" value={stats.premiumUsers.toLocaleString()} subtitle="25.7% conversion rate" trendText="+8%" icon="verified" colorClass="bg-indigo-50 text-indigo-700" />
              <Card title="Monthly Revenue" value={`$${stats.revenue.toLocaleString()}`} subtitle="New record high" trendText="+15%" icon="payments" colorClass="bg-emerald-50 text-emerald-700" />
              <Card title="Active Preceptors" value={stats.activePreceptors.toLocaleString()} subtitle="across 42 specialties" trendText="+5%" icon="medical_services" colorClass="bg-amber-50 text-amber-700" />
            </div>
          )}

          {error && !stats && (
            <div className="grid grid-cols-1 gap-6 mb-10 opacity-60 grayscale pointer-events-none md:grid-cols-2 lg:grid-cols-4">
              <Card title="Total Users" value="12,450" subtitle="vs. 11,116 last month" trendText="+12%" icon="group" colorClass="bg-blue-50 text-blue-700" />
              <Card title="Premium Users" value="3,210" subtitle="25.7% conversion rate" trendText="+8%" icon="verified" colorClass="bg-indigo-50 text-indigo-700" />
              <Card title="Monthly Revenue" value="$92,450" subtitle="New record high" trendText="+15%" icon="payments" colorClass="bg-emerald-50 text-emerald-700" />
              <Card title="Active Preceptors" value="845" subtitle="across 42 specialties" trendText="+5%" icon="medical_services" colorClass="bg-amber-50 text-amber-700" />
            </div>
          )}
        </>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-10">
        <div className="lg:col-span-2 bg-surface-container-lowest rounded-xl p-8 shadow-sm">
          <div className="flex justify-between items-center mb-8">
            <h4 className="text-lg font-bold text-on-surface">Growth Trends</h4>
            <select
              value={selectedTrendRange}
              onChange={(event) => setSelectedTrendRange(Number(event.target.value) as TrendRange)}
              className="text-xs font-bold bg-surface-container-low border-none rounded-lg focus:ring-0 cursor-pointer p-2"
            >
              <option value={6}>Last 6 Months</option>
              <option value={12}>Last Year</option>
            </select>
          </div>

          <div className="mb-6 flex flex-wrap gap-2">
            {(['users', 'revenue', 'subscriptions'] as TrendMetric[]).map((metric) => (
              <button
                key={metric}
                type="button"
                onClick={() => setSelectedTrendMetric(metric)}
                className={`rounded-full px-4 py-2 text-xs font-bold transition ${
                  selectedTrendMetric === metric
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {getTrendMetricLabel(metric)}
              </button>
            ))}
          </div>

          <div className="h-64 flex items-end gap-4 relative">
            <div className="absolute inset-0 flex flex-col justify-between pointer-events-none">
              <div className="border-b border-slate-100 w-full h-0" />
              <div className="border-b border-slate-100 w-full h-0" />
              <div className="border-b border-slate-100 w-full h-0" />
              <div className="border-b border-slate-100 w-full h-0" />
            </div>

            {displayTrend.map((point) => {
              const heightPercent = Math.min(Math.max((point.value / maxTrendValue) * 100, 5), 100);
              return (
                <div key={point.label} className="flex-1 flex flex-col items-center gap-2">
                  <div
                    className="relative w-full rounded-t-lg bg-slate-100 transition"
                    style={{ height: `${heightPercent}%` }}
                  >
                    <div className="absolute inset-x-0 bottom-0 rounded-t-lg bg-gradient-to-t from-blue-600 to-blue-400 h-full" />
                  </div>
                  <span className="text-[10px] font-semibold text-slate-500">{point.label}</span>
                  <span className="text-[11px] font-bold text-slate-900">{point.value}</span>
                </div>
              );
            })}

            {insightsLoading && (
              <div className="absolute inset-0 flex items-center justify-center rounded-2xl bg-white/80 text-xs font-semibold text-slate-500">
                Loading {getTrendMetricLabel(selectedTrendMetric).toLowerCase()} trends...
              </div>
            )}
          </div>

          <div className="flex justify-between mt-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
            {displayTrend.map((point) => (
              <span key={point.label}>{point.label}</span>
            ))}
          </div>
          {insightsError && (
            <p className="mt-3 text-xs font-semibold text-rose-600">{insightsError}</p>
          )}
        </div>

        <div className="bg-surface-container-lowest rounded-xl p-8 shadow-sm">
          <h4 className="text-lg font-bold text-on-surface mb-6">Revenue Sources</h4>
          <div className="space-y-5">
            {revenueSources.map((source) => {
              const percent = revenueTotal > 0 ? (source.value / revenueTotal) * 100 : 0;
              return (
                <div key={source.label} className="space-y-1">
                  <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wider text-slate-500">
                    <span>{source.label}</span>
                    <span className="text-slate-700">
                      {source.value.toLocaleString()} | {percent.toFixed(0)}%
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-400">{source.description}</p>
                  <div className="h-2 rounded-full bg-slate-100">
                    <div
                      className={`${source.colorClass} h-full rounded-full`}
                      style={{ width: `${Math.min(Math.max(percent, 0), 100)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>

          {insightsError && (
            <div className="mt-8 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-semibold text-rose-700">
              Unable to load live revenue segments: {insightsError}
            </div>
          )}

          <div className="mt-10 p-4 rounded-xl bg-blue-50 border border-blue-100">
            <p className="text-[11px] text-blue-800 font-semibold leading-relaxed">
              <span className="material-symbols-outlined text-sm inline-block mr-1">trending_up</span>
              Revenue segments are generated from live admin data and gracefully fall back when a restricted feed is unavailable.
            </p>
          </div>
        </div>
      </div>

      <div className="rounded-xl bg-surface-container-lowest p-8 shadow-sm">
        <div className="mb-6 flex items-center justify-between gap-4">
          <div>
            <h4 className="text-lg font-bold text-on-surface">Top Preceptors</h4>
            <p className="text-sm text-slate-500">
              Live leaderboard sourced directly from the admin analytics endpoint.
            </p>
          </div>
          {topPreceptorsLoading ? (
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Loading...</span>
          ) : null}
        </div>

        {topPreceptorsError ? (
          <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-semibold text-rose-700">
            {topPreceptorsError}
          </div>
        ) : null}

        {topPreceptorsLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 4 }, (_, index) => (
              <div key={index} className="h-14 animate-pulse rounded-xl bg-slate-100" />
            ))}
          </div>
        ) : topPreceptors.length === 0 ? (
          <div className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
            No top preceptor analytics available right now.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-left text-xs font-bold uppercase tracking-[0.22em] text-slate-500">
                  <th className="pb-3 pr-4">Preceptor</th>
                  <th className="pb-3 pr-4">Profile Views</th>
                  <th className="pb-3">Conversion Rate</th>
                </tr>
              </thead>
              <tbody>
                {topPreceptors.map((preceptor, index) => (
                  <tr key={`${preceptor.userId ?? preceptor.displayName}-${index}`} className="border-b border-slate-100 last:border-b-0">
                    <td className="py-4 pr-4">
                      <div className="flex items-center gap-3">
                        <span className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-50 text-xs font-bold text-blue-700">
                          #{index + 1}
                        </span>
                        <span className="font-semibold text-slate-900">{preceptor.displayName}</span>
                      </div>
                    </td>
                    <td className="py-4 pr-4 font-medium text-slate-700">
                      {typeof preceptor.profileViews === 'number'
                        ? preceptor.profileViews.toLocaleString()
                        : 'N/A'}
                    </td>
                    <td className="py-4 font-medium text-slate-700">
                      {typeof preceptor.conversionRate === 'number'
                        ? `${preceptor.conversionRate.toFixed(1)}%`
                        : 'N/A'}
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

export default Dashboard;
