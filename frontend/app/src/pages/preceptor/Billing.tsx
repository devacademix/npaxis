import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PaymentTable from '../../components/preceptor/PaymentTable';
import subscriptionService, {
  type SubscriptionHistoryItem,
  type SubscriptionStatus,
} from '../../services/subscription';
import webhookService, { type WebhookEventItem } from '../../services/webhook';
import { preceptorService } from '../../services/preceptor';

const formatDate = (value?: string) => {
  if (!value) return 'Not available';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

const Billing: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');
  const navigate = useNavigate();

  const [subscriptionStatus, setSubscriptionStatus] = useState<SubscriptionStatus | null>(null);
  const [payments, setPayments] = useState<SubscriptionHistoryItem[]>([]);
  const [webhookEvents, setWebhookEvents] = useState<WebhookEventItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isPortalLoading, setIsPortalLoading] = useState(false);
  const [isUpgradeLoading, setIsUpgradeLoading] = useState(false);
  const [isCancelLoading, setIsCancelLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadBilling = async () => {
      try {
        setIsLoading(true);
        setError(null);
        await preceptorService.getLoggedInUser();

        const [history, currentStatus, recentEvents] = await Promise.all([
          subscriptionService.getHistory().catch(() => []),
          subscriptionService.getStatus(),
          webhookService.getEvents({ page: 0, size: 5 }).catch(() => []),
        ]);

        setPayments(history);
        setWebhookEvents(recentEvents);

        let mergedStatus = currentStatus;
        try {
          const hasAccess = await subscriptionService.checkAccess();
          if (mergedStatus && hasAccess) {
            mergedStatus = { ...mergedStatus, accessEnabled: true };
          }
        } catch {
          // Non-blocking.
        }

        setSubscriptionStatus(mergedStatus);
      } catch (err: any) {
        setError(err?.message || 'Failed to load billing data.');
      } finally {
        setIsLoading(false);
      }
    };

    loadBilling();
  }, [isPreceptor]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 2800);
    return () => window.clearTimeout(timer);
  }, [success]);

  const isPremium = useMemo(
    () =>
      Boolean(
        subscriptionStatus?.accessEnabled ||
          ['ACTIVE', 'TRIALING'].includes(String(subscriptionStatus?.status || '').toUpperCase())
      ),
    [subscriptionStatus]
  );
  const planName = subscriptionStatus?.planName || (isPremium ? 'Premium' : 'Free');
  const planStatus = subscriptionStatus?.status || (isPremium ? 'Active' : 'Inactive');
  const nextBillingDate = formatDate(subscriptionStatus?.currentPeriodEnd);
  const hasActiveSubscription = Boolean(subscriptionStatus?.subscriptionId);
  const cancellationScheduled = Boolean(subscriptionStatus?.cancelAtPeriodEnd);
  const mappedPayments = payments.map((payment) => ({
    transactionId: String(payment.subscriptionId),
    amount: 0,
    status: String(payment.status || 'PENDING').toUpperCase(),
    date: String(payment.startDate || payment.endDate || ''),
    invoiceUrl: undefined,
  }));

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const openBillingPortal = async () => {
    try {
      setIsPortalLoading(true);
      setError(null);
      const portalUrl = await subscriptionService.createPortalSession();
      if (!portalUrl) {
        throw new Error('Billing portal URL was not returned.');
      }
      setSuccess('Redirecting to billing portal...');
      window.location.assign(portalUrl);
    } catch (err: any) {
      setError(err?.message || 'Unable to open billing portal.');
    } finally {
      setIsPortalLoading(false);
    }
  };

  const upgradePlan = async () => {
    try {
      setIsUpgradeLoading(true);
      setError(null);
      navigate('/preceptor/subscription');
    } catch (err: any) {
      setError(err?.message || 'Unable to start checkout.');
    } finally {
      setIsUpgradeLoading(false);
    }
  };

  const onViewInvoice = () => {
    setSuccess('Invoice download is not exposed by this backend route yet.');
  };

  const cancelSubscription = async () => {
    try {
      setIsCancelLoading(true);
      setError(null);
      await subscriptionService.cancelSubscription();
      const refreshedStatus = await subscriptionService.getStatus();
      setSubscriptionStatus(refreshedStatus);
      setSuccess('Your subscription will be canceled at the end of the current billing period.');
    } catch (err: any) {
      setError(err?.message || 'Unable to cancel subscription right now.');
    } finally {
      setIsCancelLoading(false);
    }
  };

  return (
    <PreceptorLayout pageTitle="Billing & Payments">
      <div className="mx-auto max-w-6xl">
        <section className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">Billing & Payments</h1>
          <p className="mt-1 text-slate-500">Manage your subscription and view transaction history.</p>
        </section>

        {error ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {success ? (
          <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {success}
          </div>
        ) : null}

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          {isLoading ? (
            <div className="h-20 animate-pulse rounded-xl bg-slate-200/70" />
          ) : (
            <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Plan Name</p>
                <p className="mt-1 text-xl font-black text-slate-900">{planName}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Status</p>
                <span
                  className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                    isPremium ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'
                  }`}
                >
                  {planStatus}
                </span>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Next Billing Date</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{nextBillingDate}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Access Check</p>
                <span
                  className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                    isPremium ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                  }`}
                >
                  {isPremium ? 'Enabled' : 'Limited'}
                </span>
              </div>
            </div>
          )}
        </section>

        {!isLoading && cancellationScheduled ? (
          <div className="mb-6 rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm font-medium text-amber-800">
            Your subscription is set to cancel at the end of the current billing period. Premium access will remain available until then.
          </div>
        ) : null}

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="mb-4 text-xl font-bold text-slate-900">Subscription History</h2>
          <PaymentTable payments={mappedPayments} isLoading={isLoading} onViewInvoice={onViewInvoice} />
        </section>

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold text-slate-900">Recent Stripe Events</h2>
              <p className="mt-1 text-sm text-slate-500">Latest webhook activity received by the backend for subscription processing.</p>
            </div>
          </div>

          {isLoading ? (
            <div className="mt-4 h-28 animate-pulse rounded-xl bg-slate-200/70" />
          ) : webhookEvents.length ? (
            <div className="mt-4 overflow-hidden rounded-xl border border-slate-200">
              <table className="min-w-full divide-y divide-slate-200">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Event</th>
                    <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                    <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Processed</th>
                    <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Retry Count</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 bg-white">
                  {webhookEvents.map((event) => (
                    <tr key={event.eventId}>
                      <td className="px-4 py-3 text-sm font-medium text-slate-700">{event.eventType || event.eventId}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{event.status || 'Unknown'}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{formatDate(event.processedAt)}</td>
                      <td className="px-4 py-3 text-sm text-slate-600">{event.retryCount ?? 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="mt-4 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
              No recent webhook events are available yet.
            </div>
          )}
        </section>

        {!isLoading && !hasActiveSubscription ? (
          <div className="mb-6 rounded-2xl border border-blue-200 bg-blue-50 px-5 py-4 text-sm font-medium text-blue-800">
            No active subscription is attached to this preceptor yet. Billing portal and live payment status will become available after the first successful checkout.
          </div>
        ) : null}

        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-bold text-slate-900">Actions</h2>
          <p className="mt-1 text-sm text-slate-500">Manage billing settings and subscription upgrades.</p>

          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={openBillingPortal}
              disabled={isPortalLoading || !hasActiveSubscription}
              className="inline-flex items-center gap-2 rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-bold text-slate-700 hover:bg-slate-50 disabled:opacity-60"
            >
              {isPortalLoading ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-400/40 border-t-slate-700" />
                  Opening...
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">account_balance_wallet</span>
                  Open Billing Portal
                </>
              )}
            </button>

            <button
              type="button"
              onClick={upgradePlan}
              disabled={isPremium || isUpgradeLoading}
              className="inline-flex items-center gap-2 rounded-full bg-blue-700 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isUpgradeLoading ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                  Redirecting...
                </>
              ) : isPremium ? (
                'Active Plan'
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">upgrade</span>
                  Upgrade Plan
                </>
              )}
            </button>

            <button
              type="button"
              onClick={cancelSubscription}
              disabled={!hasActiveSubscription || isCancelLoading}
              className="inline-flex items-center gap-2 rounded-full border border-rose-200 bg-white px-5 py-2.5 text-sm font-bold text-rose-600 hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isCancelLoading ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-rose-300/40 border-t-rose-600" />
                  Cancelling...
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">cancel</span>
                  Cancel Subscription
                </>
              )}
            </button>
          </div>
          {!isPremium ? (
            <button
              type="button"
              onClick={() => navigate('/preceptor/subscription')}
              className="mt-3 text-sm font-semibold text-blue-700 hover:underline"
            >
              Compare plans before upgrading
            </button>
          ) : null}
        </section>
      </div>
    </PreceptorLayout>
  );
};

export default Billing;
