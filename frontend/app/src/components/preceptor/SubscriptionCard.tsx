import React from 'react';

type SubscriptionTone = 'active' | 'inactive' | 'processing' | 'neutral';

interface SubscriptionCardProps {
  title: string;
  badgeLabel: string;
  subtitle: string;
  renewalDateLabel: string;
  cancelAtPeriodEndLabel: string;
  ctaLabel: string;
  onCtaClick: () => void;
  tone?: SubscriptionTone;
  note?: string;
  isLoading?: boolean;
  isProcessing?: boolean;
  ctaDisabled?: boolean;
}

const toneClasses: Record<SubscriptionTone, string> = {
  active: 'bg-emerald-100 text-emerald-700',
  inactive: 'bg-slate-100 text-slate-700',
  processing: 'bg-amber-100 text-amber-700',
  neutral: 'bg-blue-100 text-blue-700',
};

const SubscriptionCard: React.FC<SubscriptionCardProps> = ({
  title,
  badgeLabel,
  subtitle,
  renewalDateLabel,
  cancelAtPeriodEndLabel,
  ctaLabel,
  onCtaClick,
  tone = 'neutral',
  note,
  isLoading = false,
  isProcessing = false,
  ctaDisabled = false,
}) => {
  if (isLoading) {
    return (
      <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
        <div className="space-y-4">
          <div className="flex items-start justify-between gap-3">
            <div className="flex-1">
              <div className="h-3 w-28 animate-pulse rounded-full bg-slate-200/80" />
              <div className="mt-3 h-8 w-44 animate-pulse rounded-full bg-slate-200/80" />
            </div>
            <div className="h-7 w-24 animate-pulse rounded-full bg-slate-200/80" />
          </div>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div className="h-16 animate-pulse rounded-2xl bg-slate-100" />
            <div className="h-16 animate-pulse rounded-2xl bg-slate-100" />
          </div>
          <div className="h-12 animate-pulse rounded-full bg-slate-200/80" />
        </div>
      </section>
    );
  }

  return (
    <section className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-[0.22em] text-slate-500">Subscription Status</p>
          <h2 className="mt-2 text-2xl font-black tracking-tight text-slate-900">{title}</h2>
          <p className="mt-2 text-sm text-slate-500">{subtitle}</p>
        </div>
        <span className={`inline-flex rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${toneClasses[tone]}`}>
          {badgeLabel}
        </span>
      </div>

      <div className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2">
        <div className="rounded-2xl bg-slate-50 p-4">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Renewal Date</p>
          <p className="mt-2 text-sm font-semibold text-slate-800">{renewalDateLabel}</p>
        </div>
        <div className="rounded-2xl bg-slate-50 p-4">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Cancel At Period End</p>
          <p className="mt-2 text-sm font-semibold text-slate-800">{cancelAtPeriodEndLabel}</p>
        </div>
      </div>

      {note ? (
        <div className="mt-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">{note}</div>
      ) : null}

      <button
        type="button"
        onClick={onCtaClick}
        disabled={ctaDisabled || isProcessing}
        className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-full bg-blue-700 px-5 py-3 text-sm font-bold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isProcessing ? (
          <>
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
            Processing...
          </>
        ) : (
          <>
            <span className="material-symbols-outlined text-base">arrow_forward</span>
            {ctaLabel}
          </>
        )}
      </button>
    </section>
  );
};

export default SubscriptionCard;
