import React, { useEffect, useRef, useState } from 'react';
import inquiryService from '../../services/inquiry';

const MESSAGE_LIMIT = 1000;

interface InquiryTarget {
  userId: number;
  displayName?: string;
  specialty?: string;
  location?: string;
}

interface InquiryModalProps {
  isOpen: boolean;
  preceptor: InquiryTarget | null;
  onClose: () => void;
  onSuccess: (message: string) => void;
  onError: (message: string) => void;
}

const InquiryModal: React.FC<InquiryModalProps> = ({ isOpen, preceptor, onClose, onSuccess, onError }) => {
  const [message, setMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  useEffect(() => {
    if (!isOpen) return;
    setMessage('');
    setLocalError(null);
    const frame = window.requestAnimationFrame(() => {
      textareaRef.current?.focus();
    });
    return () => window.cancelAnimationFrame(frame);
  }, [isOpen, preceptor?.userId]);

  if (!isOpen || !preceptor) return null;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLocalError(null);

    const trimmedMessage = message.trim();
    if (!trimmedMessage) {
      setLocalError('Message is required.');
      return;
    }

    try {
      setIsSubmitting(true);
      await inquiryService.sendInquiry({
        preceptorId: preceptor.userId,
        subject: `Inquiry for ${preceptor.displayName || `Preceptor #${preceptor.userId}`}`,
        message: trimmedMessage,
      });
      onSuccess('Inquiry sent successfully.');
      onClose();
    } catch (err: any) {
      const errMessage = err?.message || 'Failed to send inquiry.';
      setLocalError(errMessage);
      onError(errMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
      <div className="w-full max-w-xl rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-4">
          <h3 className="text-xl font-bold text-slate-900">Send Inquiry</h3>
          <p className="mt-1 text-sm text-slate-500">Contact this preceptor</p>
        </div>

        <div className="mb-4 rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-sm font-bold text-slate-900">{preceptor.displayName || 'Preceptor'}</p>
          <p className="mt-1 text-xs text-slate-600">
            {preceptor.specialty || 'Specialty not listed'} • {preceptor.location || 'Location unavailable'}
          </p>
        </div>

        {localError ? (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm font-medium text-red-700">
            {localError}
          </div>
        ) : null}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="mb-1.5 block text-xs font-bold uppercase tracking-wider text-slate-500">Message</label>
            <textarea
              ref={textareaRef}
              value={message}
              onChange={(event) => setMessage(event.target.value.slice(0, MESSAGE_LIMIT))}
              rows={6}
              placeholder="Write your inquiry message..."
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
              required
            />
            <p className="mt-1 text-right text-xs text-slate-500">
              {message.length}/{MESSAGE_LIMIT}
            </p>
          </div>

          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex items-center gap-2 rounded-lg bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                  Sending...
                </>
              ) : (
                'Send Inquiry'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default InquiryModal;
