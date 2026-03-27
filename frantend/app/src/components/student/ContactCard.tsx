import React from 'react';
import type { PreceptorContact } from '../../services/preceptor';

interface ContactCardProps {
  isPremium: boolean;
  contact: PreceptorContact | null;
  isRevealing: boolean;
  onReveal: () => void;
}

const ContactCard: React.FC<ContactCardProps> = ({ isPremium, contact, isRevealing, onReveal }) => {
  const hasContact = Boolean(contact?.phone || contact?.email);

  return (
    <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <h3 className="text-xl font-bold text-slate-900">Contact Details</h3>
      <p className="mt-1 text-sm text-slate-500">
        {isPremium
          ? 'This preceptor has contact visibility enabled.'
          : 'Upgrade to Premium to view contact details'}
      </p>

      <div className="mt-4 space-y-3">
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Phone</p>
          <p className={`mt-1 text-sm font-semibold text-slate-800 ${!isPremium ? 'select-none blur-sm' : ''}`}>
            {hasContact ? contact?.phone || 'Not available' : '+91 ••••• ••••'}
          </p>
        </div>
        <div className="rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Email</p>
          <p className={`mt-1 break-all text-sm font-semibold text-slate-800 ${!isPremium ? 'select-none blur-sm' : ''}`}>
            {hasContact ? contact?.email || 'Not available' : 'preceptor@••••.com'}
          </p>
        </div>
      </div>

      {!hasContact ? (
        <button
          type="button"
          onClick={onReveal}
          disabled={isRevealing}
          className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-full bg-blue-700 px-4 py-2.5 text-sm font-bold text-white transition-colors hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isRevealing ? (
            <>
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
              Unlocking...
            </>
          ) : (
            <>
              <span className="material-symbols-outlined text-base">lock_open</span>
              {isPremium ? 'Reveal Contact' : 'Unlock Contact'}
            </>
          )}
        </button>
      ) : null}
    </article>
  );
};

export default ContactCard;
