import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PricingCard from '../../components/preceptor/PricingCard';
import SubscriptionCard from '../../components/preceptor/SubscriptionCard';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import SkeletonBlock from '../../components/ui/SkeletonBlock';
import { useSession } from '../../context/SessionContext';
import useSubscriptionAudit from '../../hooks/useSubscriptionAudit';
import useSubscriptionStatus from '../../hooks/useSubscriptionStatus';
import paymentService, { type SubscriptionPlan } from '../../services/payment';

const formatRenewalDate = (value?: string) => {
  if (!value) return 'Not available';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';
  return date.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
};

const formatBillingInterval = (value?: string) => {
  if (!value) return 'Plan';
  const normalized = value.toUpperCase();
  if (normalized === 'MONTHLY') return 'Monthly';
  if (normalized === 'YEARLY' || normalized === 'ANNUAL') return 'Yearly';
  return `${value.charAt(0).toUpperCase()}${value.slice(1).toLowerCase()}`;
};

const formatPriceSuffix = (value?: string) => {
  if (!value) return 'plan';
  const normalized = value.toUpperCase();
  if (normalized === 'MONTHLY') return 'month';
  if (normalized === 'YEARLY' || normalized === 'ANNUAL') return 'year';
  return value.toLowerCase();
};

const Subscription: React.FC = () => {
  const { role, isLoading: isSessionLoading } = useSession();
  const isPreceptor = role === 'PRECEPTOR';
  const location = useLocation();
  const navigate = useNavigate();

  const [isPollingStatus, setIsPollingStatus] = useState(false);
  const [selectedBillingInterval, setSelectedBillingInterval] = useState('');
  const [pageError, setPageError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [isDebugOpen, setIsDebugOpen] = useState(false);
  const [hasHandledStripeReturn, setHasHandledStripeReturn] = useState(false);

  const {
    audit,
    runAudit,
    markCheckoutCalled,
    markRedirectedToStripe,
    markReturnedFromStripe,
    recordStatusCheck,
    setFinalStatus,
    setAuditError,
    resetAudit,
  } = useSubscriptionAudit();
  const {
    subscription,
    isActive,
    isLoading: isStatusLoading,
    error: statusError,
    refreshStatus,
    pollUntilActive,
    clearStatusError,
  } = useSubscriptionStatus();

  const plansQuery = useQuery<SubscriptionPlan[]>({
    queryKey: ['subscription', 'plans'],
    queryFn: async () => {
      const loadedPlans = await runAudit();
      return Array.isArray(loadedPlans) ? loadedPlans : [];
    },
    enabled: !isSessionLoading && isPreceptor,
    staleTime: 60_000,
    placeholderData: (previousData) => previousData,
  });

  const upgradeMutation = useMutation({
    mutationKey: ['subscription', 'checkout'],
    mutationFn: async (priceId: number) => {
      const checkoutUrl = await paymentService.createCheckoutSession({ priceId });
      if (!checkoutUrl) {
        throw new Error('Missing checkoutUrl');
      }
      return checkoutUrl;
    },
  });

  const cancelMutation = useMutation({
    mutationKey: ['subscription', 'cancel'],
    mutationFn: () => paymentService.cancelSubscription(),
  });

  useEffect(() => {
    if (!notice) return;
    const timer = window.setTimeout(() => setNotice(null), 3500);
    return () => window.clearTimeout(timer);
  }, [notice]);

  useEffect(() => {
    if (subscription === undefined) return;
    setFinalStatus(subscription);
  }, [setFinalStatus, subscription]);

  const startStatusPolling = useCallback(async () => {
    setIsPollingStatus(true);
    setPageError(null);
    clearStatusError();

    const result = await pollUntilActive({
      onCheck: (check) => {
        recordStatusCheck(check);
        if (check.raw !== undefined) {
          setFinalStatus(check.raw ?? null);
        }
      },
      onError: (message) => {
        setAuditError(message);
      },
    });

    if (result.finalStatus !== undefined) {
      setFinalStatus(result.finalStatus);
    }

    if (result.activated) {
      setNotice('Subscription activated successfully.');
    } else {
      setAuditError('Status did not become active within expected time window');
      setNotice('Payment is still processing. The dashboard will switch to Active as soon as backend status is updated.');
    }

    setIsPollingStatus(false);
  }, [clearStatusError, pollUntilActive, recordStatusCheck, setAuditError, setFinalStatus]);

  useEffect(() => {
    if (!isPreceptor) return;

    const params = new URLSearchParams(location.search);
    const checkoutState = params.get('checkout');

    if (!checkoutState || hasHandledStripeReturn) return;

    setHasHandledStripeReturn(true);

    markReturnedFromStripe();

    if (checkoutState === 'canceled') {
      setPageError(null);
      clearStatusError();
      setNotice('Payment canceled. Your subscription remains unchanged.');
      return;
    }

    if (checkoutState !== 'success') return;

    if (subscription?.accessEnabled === true) {
      setFinalStatus(subscription);
      setNotice('Subscription already active.');
      return;
    }

    setNotice('Payment processing. Waiting for backend confirmation...');
    startStatusPolling().catch((err: any) => {
      setAuditError(err?.message || 'Failed to poll subscription status.');
    });
  }, [
    clearStatusError,
    isPreceptor,
    location.search,
    markReturnedFromStripe,
    setAuditError,
    setFinalStatus,
    startStatusPolling,
    subscription,
    hasHandledStripeReturn,
  ]);

  const plans = plansQuery.data ?? [];
  const premiumPlan = useMemo(
    () => plans.find((plan) => plan.active && Array.isArray(plan.prices) && plan.prices.length > 0) || null,
    [plans]
  );

  const billingOptions = useMemo(() => {
    if (!premiumPlan?.prices?.length) return [];
    const activePrices = premiumPlan.prices.filter((price) => price.active);
    const source = activePrices.length > 0 ? activePrices : premiumPlan.prices;

    return [...source].sort((left, right) => {
      const order = ['MONTHLY', 'YEARLY', 'ANNUAL'];
      const leftIndex = order.indexOf(String(left.billingInterval || '').toUpperCase());
      const rightIndex = order.indexOf(String(right.billingInterval || '').toUpperCase());
      return (leftIndex === -1 ? 99 : leftIndex) - (rightIndex === -1 ? 99 : rightIndex);
    });
  }, [premiumPlan]);

  useEffect(() => {
    if (!billingOptions.length) {
      setSelectedBillingInterval('');
      return;
    }

    const availableIntervals = billingOptions.map((price) => String(price.billingInterval || '').toUpperCase());
    const preferredInterval = String(subscription?.billingInterval || '').toUpperCase();

    if (selectedBillingInterval && availableIntervals.includes(selectedBillingInterval.toUpperCase())) {
      return;
    }

    if (preferredInterval && availableIntervals.includes(preferredInterval)) {
      setSelectedBillingInterval(preferredInterval);
      return;
    }

    const monthlyOption = availableIntervals.find((interval) => interval === 'MONTHLY');
    setSelectedBillingInterval(monthlyOption || availableIntervals[0]);
  }, [billingOptions, selectedBillingInterval, subscription?.billingInterval]);

  const selectedPrice =
    billingOptions.find((price) => String(price.billingInterval || '').toUpperCase() === selectedBillingInterval.toUpperCase()) ||
    billingOptions[0];
  const canUpgrade = Boolean(selectedPrice?.subscriptionPriceId != null);
  const isProcessingReturn = audit.returnedFromStripe && isPollingStatus && !isActive;
  const statusResolved = subscription !== undefined;
  const diagnosticError =
    pageError || statusError || audit.error || upgradeMutation.error?.message || cancelMutation.error?.message || null;
  const isCheckingStatus = !statusResolved;

  const planTitle = isCheckingStatus
    ? 'Checking subscription status...'
    : isActive
    ? 'Active Plan'
    : isProcessingReturn
    ? 'Payment processing...'
    : 'No active plan';
  const planBadge = isCheckingStatus ? 'CHECKING' : isActive ? 'ACTIVE' : isProcessingReturn ? 'PROCESSING' : 'INACTIVE';
  const planSubtitle = isCheckingStatus
    ? 'Fetching live subscription truth from the backend status endpoint before rendering your plan state.'
    : isActive
    ? subscription?.planName || 'Your subscription is active and synced from the backend status endpoint.'
    : isProcessingReturn
    ? 'Stripe checkout is complete. Waiting for webhook confirmation from the backend.'
    : 'Upgrade to unlock premium visibility, analytics access, and better student conversion.';
  const planTone = isCheckingStatus ? 'neutral' : isActive ? 'active' : isProcessingReturn ? 'processing' : 'inactive';
  const planNote = diagnosticError
    ? diagnosticError
    : isProcessingReturn
    ? 'Status checks will continue for a short window so the UI does not show a false inactive state.'
    : undefined;
  const ctaLabel = isActive ? 'Manage Subscription' : 'Upgrade Plan';
  const renewalDate = formatRenewalDate(subscription?.currentPeriodEnd);
  const premiumPriceLabel = selectedPrice
    ? `${new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: String(selectedPrice.currency || 'USD').toUpperCase(),
        maximumFractionDigits: 0,
      }).format((selectedPrice.amountInMinorUnits || 0) / 100)} / ${formatPriceSuffix(selectedPrice.billingInterval)}`
    : 'Currently unavailable';

  const isPlansLoading = isSessionLoading || (plansQuery.isLoading && !plansQuery.data);
  const isUpgrading = upgradeMutation.isPending;
  const isCanceling = cancelMutation.isPending;

  if (!isSessionLoading && !isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const handleUpgrade = async () => {
    if (!selectedPrice?.subscriptionPriceId || isProcessingReturn || isActive) return;

    try {
      setPageError(null);
      setNotice(null);
      clearStatusError();
      resetAudit();
      await plansQuery.refetch();
      markCheckoutCalled();
      const checkoutUrl = await upgradeMutation.mutateAsync(selectedPrice.subscriptionPriceId);

      markRedirectedToStripe();
      setNotice('Redirecting to Stripe checkout...');
      window.location.assign(checkoutUrl);
    } catch (err: any) {
      const message = err?.message || 'Unable to start checkout session.';
      setPageError(message);
      setAuditError(message);
    }
  };

  const handleRetry = async () => {
    try {
      setPageError(null);
      clearStatusError();
      await plansQuery.refetch();
      const latestStatus = await refreshStatus(true);
      setFinalStatus(latestStatus);

      if (audit.returnedFromStripe && latestStatus?.accessEnabled !== true) {
        await startStatusPolling();
      }
    } catch (err: any) {
      const message = err?.message || 'Retry failed.';
      setPageError(message);
      setAuditError(message);
    }
  };

  const handleCancel = async () => {
    try {
      setPageError(null);
      clearStatusError();
      await cancelMutation.mutateAsync();
      const latestStatus = await refreshStatus(true);
      setFinalStatus(latestStatus);
      setNotice('Subscription cancellation has been scheduled successfully.');
    } catch (err: any) {
      const message = err?.message || 'Unable to cancel subscription.';
      setPageError(message);
      setAuditError(message);
    }
  };

  return (
    <PreceptorLayout pageTitle="Subscription Plans">
      <div className="mx-auto max-w-6xl">
        <section className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">Subscription Plans</h1>
          <p className="mt-1 text-slate-500">Audit checkout, monitor webhook confirmation, and manage your live subscription state.</p>
        </section>

        {diagnosticError ? (
          <div className="mb-4">
            <ErrorState message={diagnosticError} onRetry={() => void handleRetry()} />
          </div>
        ) : null}

        {notice ? (
          <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {notice}
          </div>
        ) : null}

        <div className="mb-6">
          <SubscriptionCard
            title={planTitle}
            badgeLabel={planBadge}
            subtitle={planSubtitle}
            renewalDateLabel={renewalDate}
            cancelAtPeriodEndLabel={subscription?.cancelAtPeriodEnd ? 'Yes' : 'No'}
            ctaLabel={ctaLabel}
            onCtaClick={isActive ? () => navigate('/billing') : handleUpgrade}
            tone={planTone}
            note={planNote}
            isLoading={isStatusLoading && !statusResolved}
            isProcessing={isProcessingReturn}
            ctaDisabled={isCheckingStatus || isProcessingReturn || (!isActive && !canUpgrade)}
          />
        </div>

        {!isActive && audit.returnedFromStripe ? (
          <div className="mb-6 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4 text-sm text-amber-800">
            Payment completed, but the backend has not confirmed activation yet. We are checking `/api/v1/subscriptions/status` with retries to avoid showing a false inactive state.
          </div>
        ) : null}

        <section className="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
          {isPlansLoading ? (
            <>
              <SkeletonBlock className="h-[360px]" />
              <SkeletonBlock className="h-[360px]" />
            </>
          ) : (
            <>
              <PricingCard
                title="Free Plan"
                price="$0"
                description="For new preceptors getting started"
                features={['Limited visibility', 'Contact hidden', 'Basic listing']}
                ctaText="Current Plan"
                disabled
                onCtaClick={() => undefined}
              />
              <PricingCard
                title={premiumPlan?.name || 'Premium Plan'}
                price={premiumPriceLabel}
                description={premiumPlan?.description || 'Built for maximum student reach and conversion'}
                children={
                  billingOptions.length > 1 ? (
                    <div>
                      <p className="mb-2 text-xs font-bold uppercase tracking-[0.2em] text-slate-500">Billing Cycle</p>
                      <div className="inline-flex rounded-full bg-white p-1 ring-1 ring-slate-200">
                        {billingOptions.map((price) => {
                          const interval = String(price.billingInterval || '').toUpperCase();
                          const isSelected = interval === selectedBillingInterval.toUpperCase();

                          return (
                            <button
                              key={`${price.subscriptionPriceId ?? interval}-${interval}`}
                              type="button"
                              onClick={() => setSelectedBillingInterval(interval)}
                              className={`rounded-full px-4 py-2 text-sm font-bold transition-colors ${
                                isSelected ? 'bg-blue-700 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'
                              }`}
                            >
                              {formatBillingInterval(interval)}
                            </button>
                          );
                        })}
                      </div>
                    </div>
                  ) : null
                }
                features={['Contact visibility', 'Higher ranking', 'Analytics access', 'Priority listing']}
                ctaText={isActive ? 'Active Plan' : canUpgrade ? `Upgrade ${formatBillingInterval(selectedPrice?.billingInterval)}` : 'Plan Unavailable'}
                highlighted
                badgeText="Most Popular"
                disabled={isActive || !canUpgrade || isProcessingReturn}
                isLoading={isUpgrading}
                onCtaClick={handleUpgrade}
              />
            </>
          )}
        </section>

        {!isPlansLoading && !isActive && !canUpgrade ? (
          <section className="mb-6 rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 via-white to-amber-50 p-5 shadow-sm">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex gap-4">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-700">
                  <span className="material-symbols-outlined">info</span>
                </div>
                <div>
                  <p className="text-sm font-black uppercase tracking-[0.2em] text-amber-700">Checkout Unavailable</p>
                  <h3 className="mt-1 text-lg font-bold text-slate-900">Premium plan is visible, but checkout is not ready yet.</h3>
                  <p className="mt-1 text-sm leading-6 text-slate-600">
                    The plans API loaded, but there is no active purchasable `priceId` available for checkout.
                  </p>
                </div>
              </div>
              <div className="rounded-2xl border border-amber-200 bg-white px-4 py-3 text-sm text-slate-600 sm:max-w-xs">
                <p className="font-semibold text-slate-900">Diagnostic</p>
                <p className="mt-1">`plansLoaded` is true, but no active plan price was returned for `POST /subscriptions/checkout`.</p>
              </div>
            </div>
          </section>
        ) : null}

        {!isPlansLoading && plans.length === 0 ? (
          <section className="mb-6">
            <EmptyState
              title="No subscription plans available"
              text="The backend did not return any active plans yet."
              actionLabel="Retry"
              onAction={() => void plansQuery.refetch()}
            />
          </section>
        ) : null}

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <h2 className="text-xl font-bold text-slate-900">Feature Comparison</h2>
            {isActive ? (
              <button
                type="button"
                onClick={handleCancel}
                disabled={isCanceling}
                className="rounded-full border border-red-200 px-4 py-2 text-sm font-bold text-red-600 hover:bg-red-50 disabled:opacity-60"
              >
                {isCanceling ? 'Scheduling cancel...' : 'Cancel Subscription'}
              </button>
            ) : null}
          </div>
          <div className="mt-4 overflow-hidden rounded-xl border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Feature</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Free</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Premium</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                <tr>
                  <td className="px-4 py-3 text-sm font-medium text-slate-700">Profile Visibility</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Basic</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Priority</td>
                </tr>
                <tr>
                  <td className="px-4 py-3 text-sm font-medium text-slate-700">Contact Reveal</td>
                  <td className="px-4 py-3 text-sm text-slate-600">No</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Yes</td>
                </tr>
                <tr>
                  <td className="px-4 py-3 text-sm font-medium text-slate-700">Analytics Dashboard</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Limited</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Full</td>
                </tr>
                <tr>
                  <td className="px-4 py-3 text-sm font-medium text-slate-700">Search Ranking</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Standard</td>
                  <td className="px-4 py-3 text-sm text-slate-600">Boosted</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section className="rounded-2xl bg-slate-950 p-5 text-slate-100 shadow-sm">
          <button
            type="button"
            onClick={() => setIsDebugOpen((current) => !current)}
            className="flex w-full items-center justify-between text-left"
          >
            <span className="text-lg font-bold">Subscription Debug</span>
            <span className="material-symbols-outlined text-slate-300">{isDebugOpen ? 'expand_less' : 'expand_more'}</span>
          </button>

          {isDebugOpen ? (
            <div className="mt-4 space-y-4">
              <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
                {[
                  ['Plans loaded', audit.plansLoaded],
                  ['Checkout called', audit.checkoutCalled],
                  ['Redirected to Stripe', audit.redirectedToStripe],
                  ['Returned from Stripe', audit.returnedFromStripe],
                ].map(([label, value]) => (
                  <div key={String(label)} className="rounded-2xl border border-slate-800 bg-slate-900 p-4">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-400">{label}</p>
                    <p className={`mt-2 text-sm font-bold ${value ? 'text-emerald-400' : 'text-rose-400'}`}>{value ? 'Yes' : 'No'}</p>
                  </div>
                ))}
              </div>

              <div className="rounded-2xl border border-slate-800 bg-slate-900 p-4">
                <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Status Checks</p>
                {audit.statusChecks.length > 0 ? (
                  <div className="mt-3 space-y-2">
                    {audit.statusChecks.map((check, index) => (
                      <div key={`${check.at}-${index}`} className="rounded-xl border border-slate-800 bg-slate-950/70 p-3 text-sm text-slate-300">
                        <p>{new Date(check.at).toLocaleTimeString('en-US')}</p>
                        <p className="mt-1">accessEnabled: {String(check.accessEnabled)}</p>
                        <pre className="mt-2 overflow-x-auto whitespace-pre-wrap text-xs text-slate-400">
                          {JSON.stringify(check.raw ?? null, null, 2)}
                        </pre>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="mt-3 text-sm text-slate-400">No status checks recorded yet.</p>
                )}
              </div>

              <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
                <div className="rounded-2xl border border-slate-800 bg-slate-900 p-4">
                  <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Final Status Payload</p>
                  <pre className="mt-3 overflow-x-auto whitespace-pre-wrap text-xs text-slate-300">
                    {JSON.stringify(audit.finalStatus ?? null, null, 2)}
                  </pre>
                </div>
                <div className="rounded-2xl border border-slate-800 bg-slate-900 p-4">
                  <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Error</p>
                  <pre className="mt-3 overflow-x-auto whitespace-pre-wrap text-xs text-rose-300">
                    {audit.error ? JSON.stringify(audit.error, null, 2) : 'null'}
                  </pre>
                </div>
              </div>
            </div>
          ) : null}
        </section>
      </div>
    </PreceptorLayout>
  );
};

export default Subscription;
