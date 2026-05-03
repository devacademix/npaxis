import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PricingCard from '../../components/preceptor/PricingCard';
import paymentService, { type SubscriptionPlan, type SubscriptionStatus } from '../../services/payment';
import { preceptorService, type PreceptorProfile } from '../../services/preceptor';

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
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');
  const location = useLocation();

  const [userId, setUserId] = useState<number | null>(null);
  const [profile, setProfile] = useState<PreceptorProfile | null>(null);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [subscription, setSubscription] = useState<SubscriptionStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpgrading, setIsUpgrading] = useState(false);
  const [isCanceling, setIsCanceling] = useState(false);
  const [isSyncingCheckout, setIsSyncingCheckout] = useState(false);
  const [isActivationPending, setIsActivationPending] = useState(false);
  const [selectedBillingInterval, setSelectedBillingInterval] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadPlan = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const user = await preceptorService.getLoggedInUser();
        setUserId(user.userId);
        const [preceptor, planList, subscriptionStatus] = await Promise.all([
          preceptorService.getPreceptorById(user.userId),
          paymentService.getSubscriptionPlans().catch(() => []),
          paymentService.getSubscriptionStatus().catch(() => null),
        ]);
        setProfile(preceptor);
        setPlans(planList);
        setSubscription(subscriptionStatus);
        const hasPremiumAccess = Boolean(preceptor?.isPremium || subscriptionStatus?.accessEnabled);
        localStorage.setItem('isPremium', String(hasPremiumAccess));
        if (hasPremiumAccess) {
          localStorage.removeItem('premiumActivationPending');
          setIsActivationPending(false);
        } else {
          setIsActivationPending(localStorage.getItem('premiumActivationPending') === 'true');
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to load subscription data.');
      } finally {
        setIsLoading(false);
      }
    };

    loadPlan();
  }, [isPreceptor]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 2500);
    return () => window.clearTimeout(timer);
  }, [success]);

  useEffect(() => {
    if (!isPreceptor || !userId) return;

    const params = new URLSearchParams(location.search);
    const checkoutState = params.get('checkout');

    if (checkoutState === 'canceled') {
      localStorage.removeItem('premiumActivationPending');
      setIsActivationPending(false);
      setSuccess('Checkout was canceled. You can try again anytime.');
      return;
    }

    if (checkoutState !== 'success') return;

    let cancelled = false;

    const syncCheckoutState = async () => {
      setIsSyncingCheckout(true);
      setIsActivationPending(true);
      localStorage.setItem('premiumActivationPending', 'true');
      setError(null);
      setSuccess('Payment received. Confirming your subscription...');

      for (let attempt = 0; attempt < 6; attempt += 1) {
        if (cancelled) return;

        const [preceptorResult, statusResult, accessResult] = await Promise.all([
          preceptorService.getPreceptorById(userId).catch(() => null),
          paymentService.getSubscriptionStatus().catch(() => null),
          paymentService.checkPremiumAccess().catch(() => false),
        ]);

        if (cancelled) return;

        if (preceptorResult) {
          setProfile(preceptorResult);
        }
        if (statusResult) {
          setSubscription(statusResult);
        }

        const hasPremiumAccess = Boolean(preceptorResult?.isPremium || statusResult?.accessEnabled || accessResult);
        localStorage.setItem('isPremium', String(hasPremiumAccess));

        if (hasPremiumAccess) {
          localStorage.removeItem('premiumActivationPending');
          setIsActivationPending(false);
          setSuccess('Subscription activated successfully.');
          setIsSyncingCheckout(false);
          return;
        }

        await new Promise((resolve) => window.setTimeout(resolve, 3000));
      }

      setSuccess('Payment completed. Premium access is still syncing. Please refresh shortly.');
      setIsSyncingCheckout(false);
    };

    syncCheckoutState();

    return () => {
      cancelled = true;
    };
  }, [isPreceptor, location.search, userId]);

  const isPremium = useMemo(() => Boolean(profile?.isPremium || subscription?.accessEnabled), [profile?.isPremium, subscription?.accessEnabled]);
  const premiumPlan = useMemo(() => plans.find((plan) => plan.active && plan.prices?.length > 0) || null, [plans]);
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
    billingOptions.find((price) => String(price.billingInterval || '').toUpperCase() === selectedBillingInterval.toUpperCase()) || billingOptions[0];
  const canUpgrade = Boolean(selectedPrice && (selectedPrice.subscriptionPriceId != null || (userId != null && selectedPrice.billingInterval)));
  const currentPlan = subscription?.planName || (isPremium ? 'Premium' : isActivationPending ? 'Premium' : 'Free');
  const statusLabel = subscription?.status || (isPremium ? 'Active' : isActivationPending ? 'Activation Pending' : 'Inactive');
  const renewalDate = formatRenewalDate(subscription?.currentPeriodEnd);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const handleUpgrade = async () => {
    if (isPremium) return;

    try {
      setIsUpgrading(true);
      setError(null);
      setSuccess(null);

      if (!selectedPrice) {
        throw new Error('No active subscription plan is available right now.');
      }

      localStorage.setItem('premiumActivationPending', 'true');
      setIsActivationPending(true);
      const checkoutUrl = await paymentService.createCheckoutSession({
        priceId: selectedPrice.subscriptionPriceId,
        preceptorId: userId,
        billingInterval: selectedPrice.billingInterval,
        successUrl: `${window.location.origin}/preceptor/subscription`,
        cancelUrl: `${window.location.origin}/preceptor/subscription`,
      });
      if (!checkoutUrl) {
        throw new Error('Checkout URL not returned by server.');
      }

      setSuccess('Redirecting to checkout...');
      window.location.assign(checkoutUrl);
    } catch (err: any) {
      setError(err?.message || 'Unable to start checkout session.');
    } finally {
      setIsUpgrading(false);
    }
  };

  const handleCancel = async () => {
    try {
      setIsCanceling(true);
      setError(null);
      await paymentService.cancelSubscription();
      const refreshedStatus = await paymentService.getSubscriptionStatus().catch(() => null);
      setSubscription(refreshedStatus);
      setSuccess('Subscription cancellation has been scheduled successfully.');
    } catch (err: any) {
      setError(err?.message || 'Unable to cancel subscription.');
    } finally {
      setIsCanceling(false);
    }
  };

  const premiumPriceLabel = selectedPrice
    ? `${new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: String(selectedPrice.currency || 'USD').toUpperCase(),
        maximumFractionDigits: 0,
      }).format((selectedPrice.amountInMinorUnits || 0) / 100)} / ${formatPriceSuffix(selectedPrice.billingInterval)}`
    : 'Currently unavailable';

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
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Current Plan</p>
                <p className="mt-1 text-xl font-black text-slate-900">{currentPlan}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Status</p>
                <span className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${isPremium ? 'bg-emerald-100 text-emerald-700' : isActivationPending ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-700'}`}>
                  {statusLabel}
                </span>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Renewal Date</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{renewalDate}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Cancel At Period End</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{subscription?.cancelAtPeriodEnd ? 'Yes' : 'No'}</p>
              </div>
            </div>
          )}
        </section>

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
                ctaText={
                  isPremium
                    ? 'Active Plan'
                    : canUpgrade
                      ? `Upgrade ${formatBillingInterval(selectedPrice?.billingInterval)}`
                      : 'Plan Unavailable'
                }
                highlighted
                badgeText="Most Popular"
                disabled={isPremium || !canUpgrade || isSyncingCheckout}
                isLoading={isUpgrading}
                onCtaClick={handleUpgrade}
              />
            </>
          )}
        </section>

        {!isLoading && !isPremium && !canUpgrade ? (
          <section className="mb-6 rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 via-white to-amber-50 p-5 shadow-sm">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex gap-4">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-700">
                  <span className="material-symbols-outlined">info</span>
                </div>
                <div>
                  <p className="text-sm font-black uppercase tracking-[0.2em] text-amber-700">Checkout Unavailable</p>
                  <h3 className="mt-1 text-lg font-bold text-slate-900">Premium plan is visible, but billing is not ready yet.</h3>
                  <p className="mt-1 text-sm leading-6 text-slate-600">
                    We could not find an active purchasable subscription price from the backend, so checkout has been paused for now.
                  </p>
                </div>
              </div>
              <div className="rounded-2xl border border-amber-200 bg-white px-4 py-3 text-sm text-slate-600 sm:max-w-xs">
                <p className="font-semibold text-slate-900">What this means</p>
                <p className="mt-1">Your plan card is available, but the backend has not exposed a checkout-ready price yet.</p>
              </div>
            </div>
          </section>
        ) : null}

        <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <h2 className="text-xl font-bold text-slate-900">Feature Comparison</h2>
            {isPremium ? (
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
      </div>
    </PreceptorLayout>
  );
};

export default Subscription;
