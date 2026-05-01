import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import ChartSection, { type InteractionsDataPoint, type ViewsDataPoint } from '../../components/preceptor/ChartSection';
import StatsCard from '../../components/preceptor/StatsCard';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import inquiryService, { type InquiryRecord } from '../../services/inquiry';
import { preceptorService, type LoggedInPreceptorUser, type PreceptorStatsResponse } from '../../services/preceptor';

interface DashboardStats {
  profileViews: number;
  contactReveals: number;
  inquiries: number;
}

interface InquiryActivity {
  id: string;
  message: string;
  date: string;
}

const formatter = new Intl.NumberFormat('en-IN');

const normalizeStats = (stats: PreceptorStatsResponse | null | undefined): DashboardStats => ({
  profileViews: Number(stats?.profileViews ?? 0),
  contactReveals: Number(stats?.contactReveals ?? 0),
  inquiries: Number(stats?.inquiries ?? 0),
});

const MONTH_LABELS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];

const buildViewsSeries = (totalViews: number): ViewsDataPoint[] => {
  const weights = [0.08, 0.12, 0.16, 0.18, 0.21, 0.25];
  return MONTH_LABELS.map((label, index) => ({
    label,
    views: totalViews <= 0 ? 0 : Math.max(1, Math.round(totalViews * weights[index])),
  }));
};

const buildInteractionsSeries = (reveals: number, inquiries: number): InteractionsDataPoint[] => {
  const revealWeights = [0.1, 0.12, 0.15, 0.18, 0.2, 0.25];
  const inquiryWeights = [0.14, 0.13, 0.15, 0.16, 0.18, 0.24];

  return MONTH_LABELS.map((label, index) => ({
    label,
    contactReveals: reveals <= 0 ? 0 : Math.max(0, Math.round(reveals * revealWeights[index])),
    inquiries: inquiries <= 0 ? 0 : Math.max(0, Math.round(inquiries * inquiryWeights[index])),
  }));
};

const buildRecentActivity = (items: InquiryRecord[]): InquiryActivity[] =>
  items.slice(0, 5).map((item) => ({
    id: `inquiry-${item.inquiryId}`,
    message: item.message,
    date: item.createdAt ? new Date(item.createdAt).toLocaleDateString() : 'N/A',
  }));

const Dashboard: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');
  const navigate = useNavigate();

  const [user, setUser] = useState<LoggedInPreceptorUser | null>(null);
  const [stats, setStats] = useState<DashboardStats>({ profileViews: 0, contactReveals: 0, inquiries: 0 });
  const [inquiries, setInquiries] = useState<InquiryRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadDashboard = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const currentUser = await preceptorService.getLoggedInUser();
        setUser(currentUser);

        const [statsResult, inquiriesResult] = await Promise.allSettled([
          preceptorService.getStats(currentUser.userId),
          inquiryService.getMyInquiries(),
        ]);

        if (statsResult.status === 'fulfilled') {
          setStats(normalizeStats(statsResult.value));
        } else {
          // Fallback to zeros if stats aren't found or API fails, avoiding dummy data
          setStats({
            profileViews: 0,
            contactReveals: 0,
            inquiries: 0,
          });
        }

        if (inquiriesResult.status === 'fulfilled') {
          setInquiries(inquiriesResult.value);
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to load dashboard data.');
        setStats({
          profileViews: 0,
          contactReveals: 0,
          inquiries: 0,
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboard();
  }, [isPreceptor]);

  const name = useMemo(
    () => user?.displayName || localStorage.getItem('displayName') || 'Preceptor',
    [user?.displayName]
  );

  const isPremium = useMemo(() => {
    const roleValue = String(role || '').toUpperCase();
    return roleValue.includes('PREMIUM') || localStorage.getItem('isPremium') === 'true';
  }, [role]);

  const viewsData = useMemo(() => buildViewsSeries(stats.profileViews), [stats.profileViews]);
  const interactionsData = useMemo(
    () => buildInteractionsSeries(stats.contactReveals, stats.inquiries),
    [stats.contactReveals, stats.inquiries]
  );
  const recentActivity = useMemo(() => buildRecentActivity(inquiries), [inquiries]);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  return (
    <PreceptorLayout pageTitle="Dashboard">
      <section className="mb-6">
        <h1 className="text-3xl font-black tracking-tight text-slate-900 md:text-4xl">Welcome back, {name}</h1>
        <p className="mt-2 text-slate-500">Here is how your profile is performing across inquiries and engagement.</p>
      </section>

      {error ? (
        <div className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
          {error}
        </div>
      ) : null}

      <section className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {isLoading ? (
          Array.from({ length: 4 }, (_, index) => (
            <div key={index} className="h-40 animate-pulse rounded-2xl bg-slate-200/70" />
          ))
        ) : (
          <>
            <StatsCard
              title="Profile Views"
              value={formatter.format(stats.profileViews)}
              subtitle="Total profile visibility"
              icon="visibility"
            />
            <StatsCard
              title="Contact Reveals"
              value={formatter.format(stats.contactReveals)}
              subtitle="Students unlocked contact details"
              icon="contact_page"
            />
            <StatsCard
              title="Total Inquiries"
              value={formatter.format(stats.inquiries)}
              subtitle="Inquiry requests received"
              icon="mark_email_unread"
            />
            <StatsCard
              title="Premium Status"
              value={isPremium ? 'Active' : 'Inactive'}
              subtitle={isPremium ? 'Premium features enabled' : 'Upgrade for better visibility'}
              icon="workspace_premium"
              badge={{ text: isPremium ? 'Active' : 'Inactive', tone: isPremium ? 'success' : 'neutral' }}
            />
          </>
        )}
      </section>

      <section className="mb-6">
        {isLoading ? (
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            <div className="h-[360px] animate-pulse rounded-2xl bg-slate-200/70" />
            <div className="h-[360px] animate-pulse rounded-2xl bg-slate-200/70" />
          </div>
        ) : (
          <ChartSection viewsData={viewsData} interactionsData={interactionsData} />
        )}
      </section>

      <section className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 xl:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-900">Recent Inquiries</h2>
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Latest Activity</span>
          </div>

          {isLoading ? (
            <div className="space-y-3">
              {Array.from({ length: 4 }, (_, index) => (
                <div key={index} className="h-14 animate-pulse rounded-xl bg-slate-200/70" />
              ))}
            </div>
          ) : recentActivity.length > 0 ? (
            <div className="overflow-hidden rounded-xl border border-slate-200">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Date</th>
                    <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Message</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {recentActivity.map((item) => (
                    <tr key={item.id} className="transition-colors hover:bg-slate-50">
                      <td className="px-4 py-3 text-sm font-semibold text-slate-700">{item.date}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{item.message}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-10 text-center text-sm font-medium text-slate-500">
              No inquiries yet. Your new inquiries will appear here.
            </div>
          )}
        </article>

        <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-xl font-bold text-slate-900">Quick Actions</h2>
          <p className="mt-1 text-sm text-slate-500">Manage profile, verification, and monetization.</p>

          <div className="mt-5 space-y-3">
            <button
              type="button"
              onClick={() => navigate('/preceptor/profile')}
              className="flex w-full items-center justify-between rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
            >
              Edit Profile
              <span className="material-symbols-outlined text-base">chevron_right</span>
            </button>
            <button
              type="button"
              onClick={() => navigate('/preceptor/license-verification')}
              className="flex w-full items-center justify-between rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
            >
              Upload License
              <span className="material-symbols-outlined text-base">chevron_right</span>
            </button>
            <button
              type="button"
              onClick={() => navigate('/preceptor/inquiries')}
              className="flex w-full items-center justify-between rounded-xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
            >
              Review Inquiries
              <span className="material-symbols-outlined text-base">chevron_right</span>
            </button>
            <button
              type="button"
              onClick={() => navigate('/preceptor/subscription')}
              className="flex w-full items-center justify-between rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-semibold text-blue-700 transition-colors hover:bg-blue-100"
            >
              Upgrade to Premium
              <span className="material-symbols-outlined text-base">chevron_right</span>
            </button>
          </div>
        </article>
      </section>
    </PreceptorLayout>
  );
};

export default Dashboard;
