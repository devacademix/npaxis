import React, { useEffect, useMemo, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import Card from '../../components/Card';
import { type RevenueChartPoint } from '../../components/admin/RevenueChart';
import { adminService, type PendingPreceptorView } from '../../services/admin';
import { preceptorService } from '../../services/preceptor';
import { studentService, type StudentProfile } from '../../services/student';

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

const getLastMonths = (count = 6) => {
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

const buildGrowthTrend = (pendingPreceptors: PendingPreceptorView[]) => {
  const months = getLastMonths();
  const bucket: Record<string, number> = months.reduce((acc, month) => {
    acc[month.key] = 0;
    return acc;
  }, {} as Record<string, number>);

  pendingPreceptors.forEach((item) => {
    const raw = item.submittedAtRaw;
    if (!raw) return;
    const parsed = new Date(raw);
    if (Number.isNaN(parsed.getTime())) return;
    const key = `${parsed.getFullYear()}-${parsed.getMonth()}`;
    if (bucket[key] !== undefined) {
      bucket[key] += 1;
    }
  });

  return months.map((month) => ({
    label: month.label,
    value: bucket[month.key] ?? 0,
  }));
};

const buildRevenueSources = (students: StudentProfile[], preceptorCount: number): RevenueSourceEntry[] => {
  const normalizedStudents = students.length;
  const institutionCount =
    new Set(students.map((student) => student.university?.trim() || 'Unknown')).size || 1;

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
      label: 'Institutions',
      value: institutionCount,
      description: `${institutionCount.toLocaleString()} unique campuses`,
      colorClass: 'bg-gradient-to-r from-emerald-500 to-emerald-400',
    },
  ];
};

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [growthTrend, setGrowthTrend] = useState<RevenueChartPoint[]>([]);
  const [revenueSources, setRevenueSources] = useState<RevenueSourceEntry[]>([]);
  const [insightsError, setInsightsError] = useState<string | null>(null);
  const [insightsLoading, setInsightsLoading] = useState(true);
  const [topPreceptors, setTopPreceptors] = useState<any[]>([]);

  const handleGenerateReport = () => {
    const reportPayload = {
      generatedAt: new Date().toISOString(),
      dashboardStats: stats ?? {
        totalUsers: 0,
        premiumUsers: 0,
        revenue: 0,
        activePreceptors: 0,
      },
    };

    const blob = new Blob([JSON.stringify(reportPayload, null, 2)], {
      type: 'application/json',
    });
    const reportUrl = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = reportUrl;
    anchor.download = `admin-dashboard-report-${new Date().toISOString().slice(0, 10)}.json`;
    anchor.click();
    URL.revokeObjectURL(reportUrl);
  };

  const maxTrendValue = useMemo(() => {
    if (growthTrend.length === 0) return 1;
    return Math.max(Math.max(...growthTrend.map((point) => point.value)), 1);
  }, [growthTrend]);

  const revenueTotal = useMemo(
    () => revenueSources.reduce((sum, source) => sum + source.value, 0),
    [revenueSources]
  );

  const displayTrend = useMemo(() => {
    if (growthTrend.length > 0) return growthTrend;
    return getLastMonths().map((month) => ({ label: month.label, value: 0 }));
  }, [growthTrend]);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setIsLoading(true);
        try {
          const overview = await adminService.getAnalyticsOverview();
          setStats({
            totalUsers: Number(overview?.totalUsers ?? overview?.users ?? 0),
            premiumUsers: Number(overview?.premiumCount ?? overview?.premiumUsers ?? 0),
            revenue: Number(overview?.monthlyRevenue ?? overview?.revenue ?? overview?.totalRevenue ?? 0),
            activePreceptors: Number(overview?.activePreceptors ?? overview?.preceptorCount ?? 0),
          });
        } catch {
          const response = await adminService.getStats();
          setStats(response);
        }
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
        const [students, pending, preceptorOverview, trends] = await Promise.all([
          studentService.getActiveStudents(),
          adminService.getPendingPreceptors({ size: 120 }),
          preceptorService.searchPreceptors({ size: 1 }),
          adminService.getAnalyticsTrends().catch(() => null),
        ]);
        const topPreceptorsResponse = await adminService.getAnalyticsTopPreceptors().catch(() => null);

        const trendSource = Array.isArray((trends as any)?.growthTrend)
          ? (trends as any).growthTrend
          : Array.isArray((trends as any)?.items)
          ? (trends as any).items
          : null;

        if (trendSource) {
          setGrowthTrend(
            trendSource
              .map((item: any) => ({
                label: String(item?.label ?? item?.month ?? ''),
                value: Number(item?.value ?? item?.count ?? 0),
              }))
              .filter((item: { label: string; value: number }) => item.label)
          );
        } else {
          setGrowthTrend(buildGrowthTrend(pending));
        }
        setRevenueSources(buildRevenueSources(students, preceptorOverview.totalElements));
        const topList = Array.isArray((topPreceptorsResponse as any)?.topPreceptors)
          ? (topPreceptorsResponse as any).topPreceptors
          : Array.isArray(topPreceptorsResponse)
          ? topPreceptorsResponse
          : [];
        setTopPreceptors(topList.slice(0, 5));
      } catch (err: any) {
        setInsightsError(err?.message || 'Unable to load live insights.');
        setGrowthTrend(buildGrowthTrend([]));
        setRevenueSources(buildRevenueSources([], 0));
        setTopPreceptors([]);
      } finally {
        setInsightsLoading(false);
      }
    };

    loadInsights();
  }, []);

  const renderSkeletons = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
      {[...Array(4)].map((_, i) => (
        <div key={i} className="bg-surface-container-lowest p-6 rounded-xl border border-transparent animate-pulse">
            <div className="flex justify-between items-start mb-4">
              <div className="w-12 h-12 rounded-lg bg-slate-200"></div>
              <div className="w-10 h-4 bg-slate-200 rounded-full"></div>
            </div>
            <div className="h-3 w-1/2 bg-slate-200 mb-2 rounded"></div>
            <div className="h-8 w-3/4 bg-slate-200 mt-2 mb-2 rounded"></div>
            <div className="h-2 w-1/3 bg-slate-200 rounded"></div>
        </div>
      ))}
    </div>
  );

  return (
    <AdminLayout>
      <div className="mb-10 flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight text-on-surface mb-2 font-headline">Admin Dashboard</h1>
          <p className="text-slate-500 font-medium">Welcome back, Sarah. Here's what's happening across NPaxis today.</p>
        </div>
        <button
          type="button"
          onClick={handleGenerateReport}
          className="bg-gradient-to-br from-[#003d9b] to-[#0052cc] text-white px-6 py-2.5 rounded-full font-bold flex items-center gap-2 shadow-lg shadow-blue-900/10 hover:opacity-90 transition-opacity"
        >
          <span className="material-symbols-outlined text-sm">download</span>
          Generate Report
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
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
            <Card title="Total Users" value={stats.totalUsers.toLocaleString()} subtitle="vs. 11,116 last month" trendText="+12%" icon="group" colorClass="bg-blue-50 text-blue-700" />
            <Card title="Premium Users" value={stats.premiumUsers.toLocaleString()} subtitle="25.7% conversion rate" trendText="+8%" icon="verified" colorClass="bg-indigo-50 text-indigo-700" />
            <Card title="Monthly Revenue" value={`$${stats.revenue.toLocaleString()}`} subtitle="New record high" trendText="+15%" icon="payments" colorClass="bg-emerald-50 text-emerald-700" />
            <Card title="Active Preceptors" value={stats.activePreceptors.toLocaleString()} subtitle="across 42 specialties" trendText="+5%" icon="medical_services" colorClass="bg-amber-50 text-amber-700" />
          </div>
         )}
         
         {/* If backend api fails, we show mock data here just for layout demonstration */}
         {error && !stats && (
           <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10 opacity-60 grayscale pointer-events-none">
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
            <select className="text-xs font-bold bg-surface-container-low border-none rounded-lg focus:ring-0 cursor-pointer p-2">
              <option>Last 6 Months</option>
              <option>Last Year</option>
            </select>
          </div>
          
          <div className="h-64 flex items-end gap-4 relative">
            <div className="absolute inset-0 flex flex-col justify-between pointer-events-none">
              <div className="border-b border-slate-100 w-full h-0"></div>
              <div className="border-b border-slate-100 w-full h-0"></div>
              <div className="border-b border-slate-100 w-full h-0"></div>
              <div className="border-b border-slate-100 w-full h-0"></div>
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
                Loading growth data...
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
                      {source.value.toLocaleString()} · {percent.toFixed(0)}%
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
              Revenue segments are generated from remote data (learners, preceptors, and institutions) and update in real time.
            </p>
          </div>
        </div>
      </div>

      <div className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <div className="mb-4 flex items-center justify-between">
          <h4 className="text-lg font-bold text-slate-900">Top Preceptors</h4>
          <span className="text-xs font-semibold uppercase tracking-wide text-slate-500">{topPreceptors.length} ranked</span>
        </div>
        {topPreceptors.length === 0 ? (
          <p className="text-sm text-slate-500">No top-preceptor analytics available right now.</p>
        ) : (
          <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-5">
            {topPreceptors.map((item, index) => (
              <div key={item?.userId ?? item?.id ?? index} className="rounded-2xl border border-slate-100 bg-slate-50/80 p-4">
                <p className="text-xs font-bold uppercase tracking-[0.3em] text-slate-400">Rank #{index + 1}</p>
                <p className="mt-2 text-base font-semibold text-slate-900">
                  {item?.displayName ?? item?.name ?? item?.preceptorName ?? 'Preceptor'}
                </p>
                <p className="mt-1 text-xs text-slate-500">{item?.specialty ?? item?.location ?? 'No profile tag'}</p>
                <p className="mt-3 text-sm font-bold text-blue-700">
                  {item?.score ?? item?.profileViews ?? item?.engagement ?? item?.inquiries ?? 0}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default Dashboard;
