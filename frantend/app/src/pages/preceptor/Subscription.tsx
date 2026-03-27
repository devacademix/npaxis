import React, { useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import PricingCard from '../../components/preceptor/PricingCard';
import { preceptorService, type PreceptorProfile } from '../../services/preceptor';
import paymentService from '../../services/payment';

const formatRenewalDate = (value?: string) => {
  if (!value) return 'Not available';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Not available';
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

const Subscription: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');

  const [userId, setUserId] = useState<number | null>(null);
  const [profile, setProfile] = useState<PreceptorProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpgrading, setIsUpgrading] = useState(false);
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
        const preceptor = await preceptorService.getPreceptorById(user.userId);
        setProfile(preceptor);
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

  const isPremium = useMemo(() => Boolean(profile?.isPremium), [profile?.isPremium]);
  const currentPlan = isPremium ? 'Premium' : 'Free';
  const statusLabel = isPremium ? 'Active' : 'Inactive';
  const renewalDate = formatRenewalDate((profile as any)?.subscriptionRenewalDate);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const handleUpgrade = async () => {
    if (!userId) {
      setError('Unable to identify account. Please login again.');
      return;
    }
    if (isPremium) return;

    try {
      setIsUpgrading(true);
      setError(null);
      setSuccess(null);

      const checkoutUrl = await paymentService.createCheckoutSession({ userId });
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
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Current Plan</p>
                <p className="mt-1 text-xl font-black text-slate-900">{currentPlan}</p>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Status</p>
                <span
                  className={`mt-1 inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                    isPremium ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'
                  }`}
                >
                  {statusLabel}
                </span>
              </div>
              <div>
                <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Renewal Date</p>
                <p className="mt-1 text-sm font-semibold text-slate-800">{renewalDate}</p>
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
                price="₹0"
                description="For new preceptors getting started"
                features={['Limited visibility', 'Contact hidden', 'Basic listing']}
                ctaText="Current Plan"
                disabled
                onCtaClick={() => undefined}
              />
              <PricingCard
                title="Premium Plan"
                price="₹1,999 / month"
                description="Built for maximum student reach and conversion"
                features={['Contact visibility', 'Higher ranking', 'Analytics access', 'Priority listing']}
                ctaText={isPremium ? 'Active Plan' : 'Upgrade Now'}
                highlighted
                badgeText="Most Popular"
                disabled={isPremium}
                isLoading={isUpgrading}
                onCtaClick={handleUpgrade}
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
