import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import inquiryService from '../../services/inquiry';
import { preceptorService, type PreceptorProfile } from '../../services/preceptor';

const MESSAGE_LIMIT = 1000;

const Inquiry: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preceptorId = Number(searchParams.get('preceptorId') || 0);

  const [preceptor, setPreceptor] = useState<PreceptorProfile | null>(null);
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isStudent || !preceptorId) return;

    const loadPreceptor = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const profile = await preceptorService.getPreceptorById(preceptorId);
        setPreceptor(profile);
      } catch (err: any) {
        setError(err?.message || 'Unable to load preceptor details.');
      } finally {
        setIsLoading(false);
      }
    };

    loadPreceptor();
  }, [isStudent, preceptorId]);

  const canSubmit = useMemo(
    () => Boolean(preceptorId && message.trim().length > 0 && !isSubmitting),
    [message, preceptorId, isSubmitting]
  );

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setSuccess(null);

    const trimmed = message.trim();
    if (!trimmed) {
      setError('Message is required.');
      return;
    }
    if (!preceptorId) {
      setError('Preceptor ID is missing.');
      return;
    }

    try {
      setIsSubmitting(true);
      await inquiryService.sendInquiry({
        preceptorId,
        message: trimmed,
      });
      setSuccess('Inquiry sent successfully.');
      setMessage('');
      window.setTimeout(() => navigate(`/student/preceptor-detail/${preceptorId}`), 1200);
    } catch (err: any) {
      setError(err?.message || 'Failed to send inquiry.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <StudentLayout pageTitle="Send Inquiry">
      <div className="mx-auto flex min-h-[70vh] max-w-3xl items-center justify-center">
        <section className="w-full rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 md:p-8">
          <div className="mb-5">
            <h2 className="text-2xl font-black tracking-tight text-slate-900">Send Inquiry</h2>
            <p className="mt-1 text-sm text-slate-500">Contact this preceptor</p>
          </div>

          {error ? (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm font-medium text-red-700">
              {error}
            </div>
          ) : null}
          {success ? (
            <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700">
              {success}
            </div>
          ) : null}

          {!preceptorId ? (
            <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-10 text-center">
              <p className="text-sm font-semibold text-slate-700">No preceptor selected.</p>
              <button
                type="button"
                onClick={() => navigate('/student/browse')}
                className="mt-4 inline-flex items-center gap-2 rounded-full bg-blue-700 px-4 py-2 text-sm font-bold text-white hover:bg-blue-800"
              >
                <span className="material-symbols-outlined text-base">travel_explore</span>
                Browse Preceptors
              </button>
            </div>
          ) : (
            <>
              <div className="mb-4 rounded-xl border border-slate-200 bg-slate-50 p-4">
                {isLoading ? (
                  <div className="space-y-2">
                    <div className="h-4 w-1/3 animate-pulse rounded bg-slate-200/80" />
                    <div className="h-3 w-2/3 animate-pulse rounded bg-slate-200/80" />
                  </div>
                ) : (
                  <>
                    <p className="text-sm font-bold text-slate-900">{preceptor?.displayName || `Preceptor #${preceptorId}`}</p>
                    <p className="mt-1 text-xs text-slate-600">
                      {preceptor?.specialty || 'Specialty not listed'} • {preceptor?.location || 'Location unavailable'}
                    </p>
                  </>
                )}
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="mb-1.5 block text-xs font-bold uppercase tracking-wider text-slate-500">Message</label>
                  <textarea
                    autoFocus
                    value={message}
                    onChange={(event) => setMessage(event.target.value.slice(0, MESSAGE_LIMIT))}
                    rows={6}
                    className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
                    placeholder="Write your inquiry..."
                    required
                  />
                  <p className="mt-1 text-right text-xs text-slate-500">
                    {message.length}/{MESSAGE_LIMIT}
                  </p>
                </div>

                <div className="flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => navigate(preceptorId ? `/student/preceptor-detail/${preceptorId}` : '/student/browse')}
                    className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={!canSubmit}
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
            </>
          )}
        </section>
      </div>
    </StudentLayout>
  );
};

export default Inquiry;
