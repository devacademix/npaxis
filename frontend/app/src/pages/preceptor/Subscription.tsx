import React, { useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PricingCard from '../../components/preceptor/PricingCard';
import { preceptorService } from '../../services/preceptor';
import paymentService from '../../services/payment';
import subscriptionService, {
  type SubscriptionPlan,
  type SubscriptionPrice,
  type SubscriptionStatus,
} from '../../services/subscription';

const SUBSCRIPTION_PENDING_SYNC_KEY = 'subscriptionPendingSync';
const FALLBACK_PREMIUM_PRICES: Record<'MONTHLY' | 'YEARLY', SubscriptionPrice> = {
  MONTHLY: {
    subscriptionPriceId: 0,
    billingInterval: 'MONTHLY',
    currency: 'USD',
    amountInMinorUnits: 999,
    active: true,
  },
  YEARLY: {
    subscriptionPriceId: 0,
    billingInterval: 'YEARLY',
    currency: 'USD',
    amountInMinorUnits: 9999,
    active: true,
  },
};

const formatRenewalDate = (value?: string) => {
  if (!value) return 'Not available';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

const getCurrencySymbol = (currency?: string) => {
  const normalized = String(currency || '').toUpperCase();
  if (normalized === 'USD') return '$';
  if (normalized === 'INR') return 'INR ';
  return normalized ? `${normalized} ` : '';
};

const formatPriceLabel = (price?: SubscriptionPrice) => {
  if (!price) return 'USD billed via Stripe';
  const currency = String(price.currency || 'USD').toUpperCase();
  const amount = (price.amountInMinorUnits / 100).toLocaleString('en-US', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  });
  return `${getCurrencySymbol(currency)}${amount} / ${price.billingInterval.toLowerCase()}`;
};

const normalizeSubscriptionPageError = (message?: string) => {
  const normalized = String(message || '').toLowerCase();
  if (normalized.includes('unexpected server error') || normalized.includes('contact admin')) {
    return 'We could not start Stripe checkout right now. Please try again in a few moments.';
  }
  return message || 'Unable to complete this request right now.';
};

const Subscription: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');

  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [subscriptionStatus, setSubscriptionStatus] = useState<SubscriptionStatus | null>(null);
  const [loggedInUserId, setLoggedInUserId] = useState<number | null>(null);
  const [billingCycle, setBillingCycle] = useState<'MONTHLY' | 'YEARLY'>('MONTHLY');
  const [isLoading, setIsLoading] = useState(true);
  const [isUpgrading, setIsUpgrading] = useState(false);
  const [isRefreshingStatus, setIsRefreshingStatus] = useState(false);
  const [isUpdatingPlan, setIsUpdatingPlan] = useState(false);
  const [loadNotice, setLoadNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadPlan = async () => {
      try {
        setIsLoading(true);
        setError(null);
        setLoadNotice(null);

        const user = await preceptorService.getLoggedInUser();
        setLoggedInUserId(user.userId);

        const [activePlansResult, currentStatusResult] = await Promise.allSettled([
          subscriptionService.getPlans(),
          subscriptionService.getStatus(),
        ]);

        if (activePlansResult.status === 'fulfilled') {
          setPlans(activePlansResult.value);
        } else {
          setPlans([]);
        }

        if (currentStatusResult.status === 'fulfilled') {
          setSubscriptionStatus(currentStatusResult.value);
        } else {
          setSubscriptionStatus(null);
        }

        try {
          const hasAccess = await subscriptionService.checkAccess();
          if (hasAccess) {
            setSubscriptionStatus((current) => (current ? { ...current, accessEnabled: true } : current));
          }
        } catch {
          // Access-check is non-blocking here.
        }

        if (activePlansResult.status === 'rejected' || currentStatusResult.status === 'rejected') {
          setLoadNotice('Live plan details are temporarily unavailable. You can still try Stripe checkout using the available upgrade option.');
        } else if (
          activePlansResult.status === 'fulfilled' &&
          !activePlansResult.value.some((plan) => Array.isArray(plan.prices) && plan.prices.length > 0)
        ) {
          setLoadNotice('Backend plan pricing is not available right now, so a temporary USD price is being shown for checkout.');
        }
      } catch {
        setPlans([]);
        setSubscriptionStatus(null);
        setLoadNotice('Subscription details could not be fully loaded right now. You can still continue with Stripe checkout.');
      } finally {
        setIsLoading(false);
      }
    };

    loadPlan();
  }, [isPreceptor]);

  useEffect(() => {
    if (!isPreceptor) return;

    const syncStartedAt = Number(localStorage.getItem(SUBSCRIPTION_PENDING_SYNC_KEY) || 0);
    if (!syncStartedAt) return;
    if (Date.now() - syncStartedAt > 1000 * 60 * 10) {
      localStorage.removeItem(SUBSCRIPTION_PENDING_SYNC_KEY);
      return;
    }

    let attempts = 0;
    let cancelled = false;
    setIsRefreshingStatus(true);
    setSuccess('We are confirming your Stripe checkout. Subscription status will refresh automatically.');

    const pollStatus = async () => {
      attempts += 1;
      try {
        const latestStatus = await subscriptionService.getStatus();
        if (cancelled) return;

        if (latestStatus) {
          setSubscriptionStatus(latestStatus);
        }

        const active = Boolean(
          latestStatus?.accessEnabled ||
            ['ACTIVE', 'TRIALING'].includes(String(latestStatus?.status || '').toUpperCase())
        );

        if (active) {
          localStorage.removeItem(SUBSCRIPTION_PENDING_SYNC_KEY);
          setIsRefreshingStatus(false);
          setSuccess('Your premium subscription is now active.');
          return;
        }

        if (attempts >= 6) {
          setIsRefreshingStatus(false);
          setSuccess('Checkout completed. If your premium status does not appear yet, please refresh again in a moment.');
          return;
        }

        window.setTimeout(pollStatus, 4000);
      } catch {
        if (cancelled) return;
        if (attempts >= 6) {
          setIsRefreshingStatus(false);
          return;
        }
        window.setTimeout(pollStatus, 4000);
      }
    };

    pollStatus();

    return () => {
      cancelled = true;
    };
  }, [isPreceptor]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 2500);
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

  const currentPlan = subscriptionStatus?.planName || (isPremium ? 'Premium' : 'Free');
  const statusLabel = subscriptionStatus?.status || (isPremium ? 'Active' : 'Inactive');
  const renewalDate = formatRenewalDate(subscriptionStatus?.currentPeriodEnd);
  const premiumPlan = plans.find((plan) => plan.active && plan.prices?.length) || plans.find((plan) => plan.prices?.length);
  const premiumPrices = premiumPlan?.prices || [];
  const apiPremiumPrice =
    premiumPrices.find((price) => price.active && String(price.billingInterval).toUpperCase() === billingCycle) ||
    premiumPrices.find((price) => String(price.billingInterval).toUpperCase() === billingCycle) ||
    premiumPrices.find((price) => price.active) ||
    premiumPrices[0];
  const premiumPrice = apiPremiumPrice || FALLBACK_PREMIUM_PRICES[billingCycle];
  const hasCatalogCheckout = Boolean(premiumPrice?.subscriptionPriceId);
  const canUseLegacyCheckout = Boolean(loggedInUserId);
  const hasConfigurablePlan = hasCatalogCheckout || canUseLegacyCheckout;
  const displayCurrency = premiumPrice?.currency || subscriptionStatus?.currency || 'USD';
  const currentBillingInterval = String(subscriptionStatus?.billingInterval || '').toUpperCase();
  const canUpdateExistingSubscription = Boolean(
    isPremium &&
      premiumPrice?.subscriptionPriceId &&
      billingCycle !== currentBillingInterval
  );

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const handleUpgrade = async () => {
    if (isPremium) return;

    try {
      setIsUpgrading(true);
      setError(null);
      setSuccess(null);

      let checkoutUrl = '';

      if (premiumPrice?.subscriptionPriceId) {
        try {
          checkoutUrl = await subscriptionService.createCheckoutSession(premiumPrice.subscriptionPriceId);
        } catch {
          checkoutUrl = '';
        }
      }

      if (!checkoutUrl && loggedInUserId) {
        checkoutUrl = await paymentService.createCheckoutSession({
          userId: loggedInUserId,
          billingCycle,
          successUrl: `${window.location.origin}/subscription/success`,
          cancelUrl: `${window.location.origin}/subscription/cancel`,
        });
      }

      if (!checkoutUrl && !loggedInUserId) {
        throw new Error('Unable to identify your account for Stripe checkout.');
      }

      if (!checkoutUrl) {
        throw new Error('Stripe checkout could not be started from the backend.');
      }

      setSuccess('Redirecting to checkout...');
      window.location.assign(checkoutUrl);
    } catch (err: any) {
      setError(normalizeSubscriptionPageError(err?.message || 'Unable to start checkout session.'));
    } finally {
      setIsUpgrading(false);
    }
  };

  const handlePlanUpdate = async () => {
    if (!premiumPrice?.subscriptionPriceId) return;

    try {
      setIsUpdatingPlan(true);
      setError(null);
      setSuccess(null);
      await subscriptionService.updateSubscription(premiumPrice.subscriptionPriceId);
      const refreshedStatus = await subscriptionService.getStatus();
      if (refreshedStatus) {
        setSubscriptionStatus(refreshedStatus);
      }
      setSuccess(`Your subscription has been updated to ${billingCycle.toLowerCase()} billing.`);
    } catch (err: any) {
      setError(normalizeSubscriptionPageError(err?.message || 'Unable to update your subscription.'));
    } finally {
      setIsUpdatingPlan(false);
    }
  };

  return (
    <PreceptorLayout pageTitle="Subscription Plans">
      <div className="mx-auto max-w-6xl">
        <section className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">Subscription Plans</h1>
          <p className="mt-1 text-slate-500">Upgrade to Premium to unlock full access.</p>
        </section>

        {error ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {loadNotice ? (
          <div className="mb-4 rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-medium text-blue-800">
            {loadNotice}
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
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Current Plan</p>
                <p className="mt-1 text-xl font-black text-slate-900">{currentPlan}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Status</p>
                <span
                  className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                    isPremium
                      ? 'bg-emerald-100 text-emerald-700'
                      : isRefreshingStatus
                      ? 'bg-blue-100 text-blue-700'
                      : 'bg-slate-100 text-slate-700'
                  }`}
                >
                  {isRefreshingStatus ? 'Refreshing' : statusLabel}
                </span>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Renewal Date</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{renewalDate}</p>
              </div>
            </div>
          )}
        </section>

        {!isLoading ? (
          <div className="mb-6 rounded-2xl border border-blue-200 bg-blue-50 px-5 py-4">
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div>
                <p className="text-sm font-semibold text-blue-900">Stripe checkout</p>
                <p className="mt-1 text-sm text-blue-800">
                  Premium subscriptions are billed in {String(displayCurrency).toUpperCase()} and redirected to Stripe for secure payment.
                </p>
              </div>
              <div className="inline-flex rounded-full bg-white p-1 ring-1 ring-blue-200">
                <button
                  type="button"
                  onClick={() => setBillingCycle('MONTHLY')}
                  className={`rounded-full px-4 py-2 text-sm font-bold transition ${
                    billingCycle === 'MONTHLY' ? 'bg-blue-700 text-white' : 'text-blue-800'
                  }`}
                >
                  Monthly
                </button>
                <button
                  type="button"
                  onClick={() => setBillingCycle('YEARLY')}
                  className={`rounded-full px-4 py-2 text-sm font-bold transition ${
                    billingCycle === 'YEARLY' ? 'bg-blue-700 text-white' : 'text-blue-800'
                  }`}
                >
                  Yearly
                </button>
              </div>
            </div>
          </div>
        ) : null}

        <section className="mb-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
          {isLoading ? (
            <>
              <div className="h-[360px] animate-pulse rounded-2xl bg-slate-200/70" />
              <div className="h-[360px] animate-pulse rounded-2xl bg-slate-200/70" />
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
                price={formatPriceLabel(premiumPrice)}
                description={premiumPlan?.description || 'Built for maximum student reach and conversion with secure Stripe checkout'}
                features={['Contact visibility', 'Higher ranking', 'Analytics access', 'Priority listing']}
                ctaText={
                  canUpdateExistingSubscription
                    ? `Switch to ${billingCycle.toLowerCase()}`
                    : isPremium
                    ? 'Active Plan'
                    : hasConfigurablePlan
                    ? 'Upgrade Now'
                    : 'Plan Unavailable'
                }
                highlighted
                badgeText="Most Popular"
                disabled={(!canUpdateExistingSubscription && (isPremium || !hasConfigurablePlan)) || isRefreshingStatus}
                isLoading={canUpdateExistingSubscription ? isUpdatingPlan : isUpgrading}
                onCtaClick={canUpdateExistingSubscription ? handlePlanUpdate : handleUpgrade}
              />
            </>
          )}
        </section>

        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-xl font-bold text-slate-900">Feature Comparison</h2>
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
      </div>
    </PreceptorLayout>
  );
};

export default Subscription;
