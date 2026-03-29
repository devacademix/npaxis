import React from 'react';
import type { StudentPreceptor } from '../../services/student';

interface PreceptorCardProps {
  preceptor: StudentPreceptor;
  onViewProfile: (preceptor: StudentPreceptor) => void;
  variant?: 'compact' | 'full';
  secondaryActionLabel?: string;
  secondaryActionIcon?: string;
  onSecondaryAction?: (preceptor: StudentPreceptor) => void;
  isSecondaryLoading?: boolean;
  secondaryDisabled?: boolean;
}

const PreceptorCard: React.FC<PreceptorCardProps> = ({
  preceptor,
  onViewProfile,
  variant = 'compact',
  secondaryActionLabel,
  secondaryActionIcon = 'delete',
  onSecondaryAction,
  isSecondaryLoading = false,
  secondaryDisabled = false,
}) => {
  const description =
    preceptor.requirements ||
    preceptor.setting ||
    'Experienced clinical preceptor available for mentorship and supervised practice.';
  const isCompact = variant === 'compact';
  const hasSecondaryAction = Boolean(secondaryActionLabel && onSecondaryAction);

  return (
    <article
      className={`rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200 transition-all duration-200 hover:-translate-y-1 hover:shadow-md ${
        isCompact ? 'min-w-[260px] max-w-[280px]' : 'h-full'
      }`}
    >
      <div className="mb-4 flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-lg font-black tracking-tight text-slate-900">{preceptor.displayName}</p>
          {preceptor.credentials ? (
            <p className="mt-1 truncate text-sm font-semibold text-slate-700">{preceptor.credentials}</p>
          ) : null}
          <p className="mt-1 truncate text-sm font-medium text-slate-500">{preceptor.specialty || 'Specialty not listed'}</p>
        </div>
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-blue-100 text-blue-700">
          <span className="material-symbols-outlined text-base">person</span>
        </div>
      </div>

      <div className="mb-4 flex items-center gap-2 text-sm text-slate-600">
        <span className="material-symbols-outlined text-base text-slate-400">location_on</span>
        <span className="truncate">{preceptor.location || 'Location unavailable'}</span>
      </div>

      {!isCompact ? (
        <p className="mb-4 line-clamp-2 text-sm text-slate-600">{description}</p>
      ) : null}

      <div className="mb-5 flex flex-wrap items-center gap-2">
        <span
          className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider ${
            preceptor.isVerified ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
          }`}
        >
          {preceptor.isVerified ? 'Verified' : 'Unverified'}
        </span>
        <span
          className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider ${
            preceptor.isPremium ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-700'
          }`}
        >
          {preceptor.isPremium ? 'Premium' : 'Free'}
        </span>
        {preceptor.honorarium ? (
          <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-700">
            {preceptor.honorarium}
          </span>
        ) : null}
      </div>

      {hasSecondaryAction ? (
        <div className="grid grid-cols-2 gap-2">
          <button
            type="button"
            onClick={() => onViewProfile(preceptor)}
            className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-4 py-2.5 text-sm font-bold text-blue-700 transition-colors hover:bg-blue-100"
          >
            <span className="material-symbols-outlined text-base">visibility</span>
            View Profile
          </button>
          <button
            type="button"
            onClick={() => onSecondaryAction?.(preceptor)}
            disabled={isSecondaryLoading || secondaryDisabled}
            className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-red-600 px-4 py-2.5 text-sm font-bold text-white transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSecondaryLoading ? (
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
            ) : (
              <span className="material-symbols-outlined text-base">{secondaryActionIcon}</span>
            )}
            {secondaryActionLabel}
          </button>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => onViewProfile(preceptor)}
          className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-blue-700 px-4 py-2.5 text-sm font-bold text-white transition-colors hover:bg-blue-800"
        >
          <span className="material-symbols-outlined text-base">visibility</span>
          View Profile
        </button>
      )}
    </article>
  );
};

export default PreceptorCard;
