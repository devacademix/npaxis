import React, { useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PricingCard from '../../components/preceptor/PricingCard';
import paymentService, { type SubscriptionPlan, type SubscriptionStatus } from '../../services/payment';
import { preceptorService, type PreceptorProfile } from '../../services/preceptor';

const formatRenewalDate = (value?: string) => {
  if (!value) return 'Not available';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

const Subscription: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');

  const [profile, setProfile] = useState<PreceptorProfile | null>(null);
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [subscription, setSubscription] = useState<SubscriptionStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpgrading, setIsUpgrading] = useState(false);
  const [isCanceling, setIsCanceling] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadPlan = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const user = await preceptorService.getLoggedInUser();
        const [preceptor, planList, subscriptionStatus] = await Promise.all([
          preceptorService.getPreceptorById(user.userId),
          paymentService.getSubscriptionPlans().catch(() => []),
          paymentService.getSubscriptionStatus().catch(() => null),
        ]);
        setProfile(preceptor);
        setPlans(planList);
        setSubscription(subscriptionStatus);
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

  const isPremium = useMemo(() => Boolean(profile?.isPremium || subscription?.accessEnabled), [profile?.isPremium, subscription?.accessEnabled]);
  const premiumPlan = useMemo(() => plans.find((plan) => plan.active && plan.prices?.length > 0) || null, [plans]);
  const selectedPrice = premiumPlan?.prices?.find((price) => price.active) || premiumPlan?.prices?.[0];
  const canUpgrade = Boolean(selectedPrice?.subscriptionPriceId);
  const currentPlan = subscription?.planName || (isPremium ? 'Premium' : 'Free');
  const statusLabel = subscription?.status || (isPremium ? 'Active' : 'Inactive');
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

      if (!selectedPrice?.subscriptionPriceId) {
        throw new Error('No active subscription plan is available right now.');
      }

      const checkoutUrl = await paymentService.createCheckoutSession({ priceId: selectedPrice.subscriptionPriceId });
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
    ? `${new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: String(selectedPrice.currency || 'INR').toUpperCase(),
        maximumFractionDigits: 0,
      }).format((selectedPrice.amountInMinorUnits || 0) / 100)} / ${selectedPrice.billingInterval?.toLowerCase()}`
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
                <span className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${isPremium ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'}`}>
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
                price="INR 0"
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
                features={['Contact visibility', 'Higher ranking', 'Analytics access', 'Priority listing']}
                ctaText={isPremium ? 'Active Plan' : canUpgrade ? 'Upgrade Now' : 'Plan Unavailable'}
                highlighted
                badgeText="Most Popular"
                disabled={isPremium || !canUpgrade}
                isLoading={isUpgrading}
                onCtaClick={handleUpgrade}
              />
            </>
          )}
        </section>

        {!isLoading && !isPremium && !canUpgrade ? (
          <div className="mb-6 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-800">
            Premium checkout is temporarily unavailable because no active subscription price was returned by the backend yet.
          </div>
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
