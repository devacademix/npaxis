import React, { useMemo } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import ResponsiveGrid from '../../components/layout/ResponsiveGrid';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import DashboardCard from '../../components/preceptor/DashboardCard';
import ChartSection, { type InteractionsDataPoint, type ViewsDataPoint } from '../../components/preceptor/ChartSection';
import { useSession } from '../../context/SessionContext';
import useDashboardData from '../../hooks/useDashboardData';
import { maskName } from '../../utils/maskName';

const formatter = new Intl.NumberFormat('en-IN');

const formatDate = (value?: string) => {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'N/A';
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
  });
};

const Dashboard: React.FC = () => {
  const { role, isLoading: isSessionLoading } = useSession();
  const navigate = useNavigate();
  const { user, stats, recentInquiries, subscription, isLoading, isSubscriptionLoading, error } = useDashboardData();

  const viewsData = useMemo<ViewsDataPoint[]>(
    () => [{ label: 'Current', views: stats.profileViews }],
    [stats.profileViews]
  );

  const interactionsData = useMemo<InteractionsDataPoint[]>(
    () => [
      {
        label: 'Current',
        contactReveals: stats.contactReveals,
        inquiries: stats.totalInquiries,
      },
    ],
    [stats.contactReveals, stats.totalInquiries]
  );

  const displayName = user?.displayName || localStorage.getItem('displayName') || 'Preceptor';

  if (!isSessionLoading && role !== 'PRECEPTOR') {
    return <Navigate to="/login" replace />;
  }

  return (
    <PreceptorLayout pageTitle="Dashboard">
      <div className="space-y-6">
        <section className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm">
          <div className="bg-[radial-gradient(circle_at_top_left,_rgba(37,99,235,0.18),_transparent_45%),linear-gradient(135deg,#0f172a_0%,#1e3a8a_55%,#2563eb_100%)] px-5 py-6 text-white sm:px-7 sm:py-8">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
              <div className="max-w-2xl">
                <p className="text-xs font-bold uppercase tracking-[0.28em] text-blue-100">Preceptor Control Center</p>
                <h1 className="mt-3 text-3xl font-black tracking-tight sm:text-4xl">Welcome back, {displayName}</h1>
                <p className="mt-3 text-sm text-blue-100 sm:text-base">
                  Track profile performance, monitor student interest, and manage subscription access from one dashboard.
                </p>
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <button
                  type="button"
                  onClick={() => navigate('/preceptor/inquiries')}
                  className="inline-flex items-center justify-center gap-2 rounded-full bg-white px-5 py-3 text-sm font-bold text-slate-900 hover:bg-slate-100"
                >
                  <span className="material-symbols-outlined text-base">mark_email_unread</span>
                  View Inquiries
                </button>
                <button
                  type="button"
                  onClick={() => navigate(subscription.actionPath)}
                  disabled={isSubscriptionLoading}
                  className="inline-flex items-center justify-center gap-2 rounded-full border border-white/30 bg-white/10 px-5 py-3 text-sm font-bold text-white hover:bg-white/20"
                >
                  <span className="material-symbols-outlined text-base">workspace_premium</span>
                  {isSubscriptionLoading ? 'Checking Plan...' : subscription.actionLabel}
                </button>
              </div>
            </div>
          </div>
        </section>

        {error ? (
          <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        <ResponsiveGrid mobileCols={1} tabletCols={2} desktopCols={4}>
          {isLoading ? (
            Array.from({ length: 4 }, (_, index) => (
              <div key={index} className="h-40 animate-pulse rounded-3xl bg-slate-200/70" />
            ))
          ) : (
            <>
              <DashboardCard
                label="Profile Views"
                value={formatter.format(stats.profileViews)}
                icon="visibility"
                helper="Total profile visibility"
              />
              <DashboardCard
                label="Contact Reveals"
                value={formatter.format(stats.contactReveals)}
                icon="contact_page"
                helper="Students unlocked contact details"
              />
              <DashboardCard
                label="Total Inquiries"
                value={formatter.format(stats.totalInquiries)}
                icon="mark_email_unread"
                helper="Inquiry requests received"
              />
              <DashboardCard
                label="Conversion Rate"
                value={`${stats.conversionRate.toFixed(1)}%`}
                icon="trending_up"
                helper="Contact reveals from total profile views"
              />
            </>
          )}
        </ResponsiveGrid>

        <div className="grid gap-6 xl:grid-cols-[minmax(0,2fr)_360px]">
          <section className="space-y-6">
            {isLoading ? (
              <div className="grid gap-6 xl:grid-cols-2">
                <div className="h-[360px] animate-pulse rounded-3xl bg-slate-200/70" />
                <div className="h-[360px] animate-pulse rounded-3xl bg-slate-200/70" />
              </div>
            ) : (
              <ChartSection viewsData={viewsData} interactionsData={interactionsData} />
            )}

            <article className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">Recent Inquiries</h2>
                  <p className="mt-1 text-sm text-slate-500">Latest student outreach from your inquiry feed.</p>
                </div>
                <button
                  type="button"
                  onClick={() => navigate('/preceptor/inquiries')}
                  className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 sm:w-auto"
                >
                  <span className="material-symbols-outlined text-base">visibility</span>
                  View All
                </button>
              </div>

              {isLoading ? (
                <div className="space-y-3">
                  {Array.from({ length: 5 }, (_, index) => (
                    <div key={index} className="h-20 animate-pulse rounded-2xl bg-slate-200/70" />
                  ))}
                </div>
              ) : recentInquiries.length > 0 ? (
                <div className="space-y-3">
                  {recentInquiries.map((item) => (
                    <div key={item.id} className="rounded-2xl border border-slate-100 bg-slate-50 p-4">
                      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                        <div className="min-w-0">
                          <p className="text-sm font-bold text-slate-900">{maskName(item.studentName)}</p>
                          <p className="mt-1 text-sm text-slate-700">{item.subject}</p>
                          <p className="mt-2 text-xs font-medium text-slate-500">{formatDate(item.createdAt)}</p>
                        </div>
                        <span className="inline-flex rounded-full bg-blue-100 px-3 py-1 text-xs font-bold uppercase tracking-wider text-blue-700">
                          {item.status}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-12 text-center text-sm font-medium text-slate-500">
                  No inquiries yet. Your latest student inquiries will appear here.
                </div>
              )}
            </article>
          </section>

          <aside className="space-y-6">
            <article className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              {isSubscriptionLoading ? (
                <div className="space-y-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1">
                      <div className="h-3 w-24 animate-pulse rounded-full bg-slate-200/80" />
                      <div className="mt-3 h-8 w-40 animate-pulse rounded-full bg-slate-200/80" />
                    </div>
                    <div className="h-7 w-20 animate-pulse rounded-full bg-slate-200/80" />
                  </div>
                  <div className="h-4 w-full animate-pulse rounded-full bg-slate-200/80" />
                  <div className="h-12 w-full animate-pulse rounded-full bg-slate-200/80" />
                </div>
              ) : (
                <>
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-xs font-bold uppercase tracking-[0.24em] text-slate-500">Subscription</p>
                      <h2 className="mt-2 text-2xl font-black tracking-tight text-slate-900">{subscription.planName}</h2>
                    </div>
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                        subscription.isActive
                          ? 'bg-emerald-100 text-emerald-700'
                          : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {subscription.status}
                    </span>
                  </div>

                  <p className="mt-3 text-sm text-slate-500">{subscription.description}</p>

                  <button
                    type="button"
                    onClick={() => navigate(subscription.actionPath)}
                    className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-full bg-blue-700 px-5 py-3 text-sm font-bold text-white hover:bg-blue-800"
                  >
                    <span className="material-symbols-outlined text-base">arrow_forward</span>
                    {subscription.actionLabel}
                  </button>
                </>
              )}
            </article>

            <article className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6">
              <h2 className="text-xl font-bold text-slate-900">Quick Actions</h2>
              <p className="mt-1 text-sm text-slate-500">Manage profile, verification, subscription, and billing.</p>

              <div className="mt-5 grid gap-3">
                {[
                  {
                    label: 'Edit Profile',
                    icon: 'person',
                    path: '/preceptor/profile',
                  },
                  {
                    label: 'Upload License',
                    icon: 'workspace_premium',
                    path: '/preceptor/license',
                  },
                  {
                    label: 'Upgrade Plan',
                    icon: 'rocket_launch',
                    path: '/subscription',
                  },
                  {
                    label: 'View Billing',
                    icon: 'payments',
                    path: '/billing',
                  },
                ].map((action) => (
                  <button
                    key={action.label}
                    type="button"
                    onClick={() => navigate(action.path)}
                    className="flex w-full items-center justify-between rounded-2xl border border-slate-200 px-4 py-3 text-left text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    <span className="inline-flex items-center gap-3">
                      <span className="material-symbols-outlined text-base text-slate-500">{action.icon}</span>
                      {action.label}
                    </span>
                    <span className="material-symbols-outlined text-base text-slate-400">chevron_right</span>
                  </button>
                ))}
              </div>
            </article>
          </aside>
        </div>
      </div>
    </PreceptorLayout>
  );
};

export default Dashboard;
