import React from 'react';

interface ActionButtonsProps {
  isSaved: boolean;
  isSaving: boolean;
  isRevealing: boolean;
  onSave: () => void;
  onSendInquiry: () => void;
  onReveal: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({
  isSaved,
  isSaving,
  isRevealing,
  onSave,
  onSendInquiry,
  onReveal,
}) => {
  return (
    <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <h3 className="text-lg font-bold text-slate-900">Actions</h3>
      <p className="mt-1 text-sm text-slate-500">Save this preceptor, send inquiry, or reveal contact details.</p>

      <div className="mt-4 space-y-3">
        <button
          type="button"
          onClick={onSave}
          disabled={isSaved || isSaving}
          className={`inline-flex w-full items-center justify-center gap-2 rounded-full px-4 py-2.5 text-sm font-bold transition-colors ${
            isSaved
              ? 'cursor-not-allowed bg-emerald-100 text-emerald-700'
              : 'bg-blue-700 text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60'
          }`}
        >
          {isSaving ? (
            <>
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
              Saving...
            </>
          ) : (
            <>
              <span className="material-symbols-outlined text-base">{isSaved ? 'check_circle' : 'bookmark_add'}</span>
              {isSaved ? 'Saved' : 'Save Preceptor'}
            </>
          )}
        </button>

        <button
          type="button"
          onClick={onSendInquiry}
          className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-slate-300 bg-white px-4 py-2.5 text-sm font-bold text-slate-700 transition-colors hover:bg-slate-50"
        >
          <span className="material-symbols-outlined text-base">send</span>
          Send Inquiry
        </button>

        <button
          type="button"
          onClick={onReveal}
          disabled={isRevealing}
          className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-blue-200 bg-blue-50 px-4 py-2.5 text-sm font-bold text-blue-700 transition-colors hover:bg-blue-100 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isRevealing ? (
            <>
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-blue-400/40 border-t-blue-700" />
              Revealing...
            </>
          ) : (
            <>
              <span className="material-symbols-outlined text-base">contact_phone</span>
              Reveal Contact
            </>
          )}
        </button>
      </div>
    </article>
  );
};

export default ActionButtons;
