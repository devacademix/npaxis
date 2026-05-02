import React from 'react';

interface PricingCardProps {
  title: string;
  price: string;
  description: string;
  children?: React.ReactNode;
  features: string[];
  ctaText: string;
  highlighted?: boolean;
  badgeText?: string;
  disabled?: boolean;
  isLoading?: boolean;
  onCtaClick: () => void;
}

const PricingCard: React.FC<PricingCardProps> = ({
  title,
  price,
  description,
  children,
  features,
  ctaText,
  highlighted = false,
  badgeText,
  disabled = false,
  isLoading = false,
  onCtaClick,
}) => {
  return (
    <article
      className={`relative rounded-2xl p-6 shadow-sm ring-1 transition-all hover:-translate-y-0.5 ${
        highlighted
          ? 'bg-blue-50/70 ring-blue-300 shadow-blue-100'
          : 'bg-white ring-slate-200 hover:shadow-md'
      }`}
    >
      {badgeText ? (
        <span className="absolute -top-3 right-4 rounded-full bg-blue-700 px-3 py-1 text-xs font-bold uppercase tracking-wider text-white">
          {badgeText}
        </span>
      ) : null}

      <h3 className="text-2xl font-black tracking-tight text-slate-900">{title}</h3>
      <p className="mt-1 text-sm text-slate-500">{description}</p>
      <p className="mt-4 text-4xl font-black tracking-tight text-slate-900">{price}</p>
      {children ? <div className="mt-4">{children}</div> : null}

      <ul className="mt-5 space-y-2">
        {features.map((feature) => (
          <li key={feature} className="flex items-start gap-2 text-sm text-slate-700">
            <span className="material-symbols-outlined text-base text-blue-700">check_circle</span>
            <span>{feature}</span>
          </li>
        ))}
      </ul>

      <button
        type="button"
        onClick={onCtaClick}
        disabled={disabled || isLoading}
        className={`mt-6 inline-flex w-full items-center justify-center gap-2 rounded-full px-4 py-2.5 text-sm font-bold transition-all ${
          highlighted
            ? 'bg-blue-700 text-white hover:bg-blue-800'
            : 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50'
        } disabled:cursor-not-allowed disabled:opacity-60`}
      >
        {isLoading ? (
          <>
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
            Processing...
          </>
        ) : (
          ctaText
        )}
      </button>
    </article>
  );
};

export default PricingCard;
