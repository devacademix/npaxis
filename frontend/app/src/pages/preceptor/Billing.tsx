import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PaymentTable from '../../components/preceptor/PaymentTable';
import useSubscriptionStatus from '../../hooks/useSubscriptionStatus';
import paymentService, { type PaymentHistoryItem, type SubscriptionEventItem, type SubscriptionPlan } from '../../services/payment';

const formatDate = (value?: string) => {
  if (!value) return 'Not available';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';
  return date.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
};

const Billing: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');
  const navigate = useNavigate();

  const [payments, setPayments] = useState<PaymentHistoryItem[]>([]);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [events, setEvents] = useState<SubscriptionEventItem[]>([]);
  const [hasPremiumAccess, setHasPremiumAccess] = useState<boolean | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isPortalLoading, setIsPortalLoading] = useState(false);
  const [isUpgradeLoading, setIsUpgradeLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const {
    subscription,
    isActive,
    isLoading: isStatusLoading,
    error: statusError,
    refreshStatus,
    clearStatusError,
  } = useSubscriptionStatus();

  useEffect(() => {
    if (!isPreceptor) return;

    let isCancelled = false;

    const loadBilling = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const [history, planList, eventList, premiumAccess] = await Promise.all([
          paymentService.getPaymentHistory().catch(() => []),
          paymentService.getSubscriptionPlans().catch(() => []),
          paymentService.getSubscriptionEvents().catch(() => []),
          paymentService.checkPremiumAccess().catch(() => null),
        ]);

        if (isCancelled) return;

        setPayments(history);
        setPlans(planList);
        setEvents(Array.isArray(eventList) ? eventList : []);
        setHasPremiumAccess(typeof premiumAccess === 'boolean' ? premiumAccess : null);
      } catch (err: any) {
        if (isCancelled) return;
        setError(err?.message || 'Failed to load billing data.');
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    loadBilling();

    return () => {
      isCancelled = true;
    };
  }, [isPreceptor]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 2800);
    return () => window.clearTimeout(timer);
  }, [success]);

  const availablePriceId = useMemo(() => {
    const activePlan = plans.find((plan) => plan.active && plan.prices?.length > 0);
    const activePrice = activePlan?.prices?.find((price) => price.active) || activePlan?.prices?.[0];
    return activePrice?.subscriptionPriceId ?? null;
  }, [plans]);

  const canUpgrade = Boolean(availablePriceId != null);
  const planName = subscription?.planName || (isActive ? 'Active plan' : 'No active plan');
  const planStatus = isActive ? 'Active' : 'Inactive';
  const nextBillingDate = formatDate(
    subscription?.currentPeriodEnd ?? payments.find((payment) => Boolean(payment.date))?.date
  );
  const diagnosticError = error || statusError;
  const recentEvents = events.slice(0, 5);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const openBillingPortal = async () => {
    try {
      setIsPortalLoading(true);
      setError(null);
      clearStatusError();
      const portalUrl = await paymentService.createPortalSession();
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
      clearStatusError();
      if (!availablePriceId) {
        throw new Error('No active subscription plan is available right now.');
      }
      const checkoutUrl = await paymentService.createCheckoutSession({
        priceId: availablePriceId,
      });
      if (!checkoutUrl) {
        throw new Error('Checkout URL not returned.');
      }
      setSuccess('Redirecting to checkout...');
      window.location.assign(checkoutUrl);
    } catch (err: any) {
      setError(err?.message || 'Unable to start checkout.');
    } finally {
      setIsUpgradeLoading(false);
    }
  };

  const handleRetryStatus = async () => {
    try {
      setError(null);
      clearStatusError();
      await refreshStatus(true);
    } catch (err: any) {
      setError(err?.message || 'Failed to refresh subscription status.');
    }
  };

  const onViewInvoice = (invoiceUrl?: string) => {
    if (invoiceUrl) {
      window.open(invoiceUrl, '_blank', 'noopener,noreferrer');
      return;
    }
    setSuccess('Invoice is not available for this transaction yet.');
  };

  return (
    <PreceptorLayout pageTitle="Billing & Payments">
      <div className="mx-auto max-w-6xl">
        <section className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">Billing & Payments</h1>
          <p className="mt-1 text-slate-500">Manage your subscription and view transaction history.</p>
        </section>

        {diagnosticError ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <span>{diagnosticError}</span>
              <button
                type="button"
                onClick={handleRetryStatus}
                className="inline-flex items-center justify-center rounded-full border border-red-200 bg-white px-4 py-2 text-sm font-semibold text-red-700 hover:bg-red-50"
              >
                Retry Status
              </button>
            </div>
          </div>
        ) : null}

        {success ? (
          <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {success}
          </div>
        ) : null}

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          {isLoading || (isStatusLoading && subscription === undefined) ? (
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
                    isActive ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'
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
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Premium Access</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">
                  {hasPremiumAccess == null ? 'Unknown' : hasPremiumAccess ? 'Granted' : 'Not Granted'}
                </p>
              </div>
            </div>
          )}
        </section>

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="mb-4 text-xl font-bold text-slate-900">Payment History</h2>
          <PaymentTable payments={payments} isLoading={isLoading} onViewInvoice={onViewInvoice} />
        </section>

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-xl font-bold text-slate-900">Subscription Event Audit</h2>
              <p className="mt-1 text-sm text-slate-500">Live events from the backend subscription audit trail.</p>
            </div>
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold uppercase tracking-wider text-slate-600">
              {events.length} event{events.length === 1 ? '' : 's'}
            </span>
          </div>

          {isLoading ? (
            <div className="mt-4 h-24 animate-pulse rounded-xl bg-slate-200/70" />
          ) : recentEvents.length === 0 ? (
            <div className="mt-4 rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-8 text-sm text-slate-500">
              No subscription events available yet.
            </div>
          ) : (
            <div className="mt-4 space-y-3">
              {recentEvents.map((event) => (
                <div key={event.eventId} className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                    <div>
                      <p className="text-sm font-semibold text-slate-900">{event.eventType}</p>
                      <p className="mt-1 text-xs text-slate-500">
                        {formatDate(event.createdAt)} {event.stripeEventId ? `• ${event.stripeEventId}` : ''}
                      </p>
                    </div>
                    <span className="rounded-full bg-slate-900 px-3 py-1 text-xs font-bold uppercase tracking-wider text-white">
                      {event.status}
                    </span>
                  </div>
                  {event.errorMessage ? (
                    <p className="mt-2 text-xs text-rose-600">{event.errorMessage}</p>
                  ) : null}
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-bold text-slate-900">Actions</h2>
          <p className="mt-1 text-sm text-slate-500">Manage billing settings and subscription upgrades.</p>

          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={openBillingPortal}
              disabled={isPortalLoading}
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
              disabled={isActive || isUpgradeLoading || !canUpgrade}
              className="inline-flex items-center gap-2 rounded-full bg-blue-700 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isUpgradeLoading ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                  Redirecting...
                </>
              ) : isActive ? (
                'Active Plan'
              ) : !canUpgrade ? (
                'Upgrade Unavailable'
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">upgrade</span>
                  Upgrade Plan
                </>
              )}
            </button>
          </div>

          {!isActive ? (
            <>
              <button
                type="button"
                onClick={() => navigate('/preceptor/subscription')}
                className="mt-3 text-sm font-semibold text-blue-700 hover:underline"
              >
                Compare plans before upgrading
              </button>
              {!canUpgrade ? (
                <div className="mt-4 rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 via-white to-amber-50 p-4">
                  <div className="flex gap-3">
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-700">
                      <span className="material-symbols-outlined text-base">info</span>
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-900">Upgrade temporarily unavailable</p>
                      <p className="mt-1 text-sm leading-6 text-slate-600">
                        Billing will become available once the backend returns an active subscription price for checkout.
                      </p>
                    </div>
                  </div>
                </div>
              ) : null}
            </>
          ) : null}
        </section>
      </div>
    </PreceptorLayout>
  );
};

export default Billing;
