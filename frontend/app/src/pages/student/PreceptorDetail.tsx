import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import ActionButtons from '../../components/student/ActionButtons';
import ContactCard from '../../components/student/ContactCard';
import InquiryModal from '../../components/student/InquiryModal';
import StudentLayout from '../../components/layout/StudentLayout';
import {
  preceptorService,
  type PreceptorContact,
  type PreceptorProfile,
} from '../../services/preceptor';
import { studentService } from '../../services/student';

const formatDay = (day: string) => day.charAt(0) + day.slice(1).toLowerCase();

const PreceptorDetail: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');
  const navigate = useNavigate();
  const { id } = useParams();

  const [preceptorData, setPreceptorData] = useState<PreceptorProfile | null>(null);
  const [contact, setContact] = useState<PreceptorContact | null>(null);
  const [studentUserId, setStudentUserId] = useState<number | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSaved, setIsSaved] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isRevealing, setIsRevealing] = useState(false);

  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [isInquiryOpen, setIsInquiryOpen] = useState(false);

  const preceptorId = useMemo(() => Number(id || 0), [id]);
  const isPremium = Boolean(preceptorData?.isPremium);

  useEffect(() => {
    if (!isStudent || !preceptorId) return;

    const loadProfile = async () => {
      try {
        setLoading(true);
        setError(null);
        setToast(null);

        const [profile, currentUser] = await Promise.all([
          preceptorService.getPreceptorById(preceptorId),
          studentService.getLoggedInUser().catch(() => null),
        ]);

        setPreceptorData(profile);
        if (currentUser) {
          setStudentUserId(currentUser.userId);

          try {
            const saved = await studentService.getSavedPreceptors(currentUser.userId);
            setIsSaved(saved.some((item) => item.userId === preceptorId));
          } catch {
            // Saved status is optional for page rendering.
          }
        }

        await preceptorService.trackAnalyticsEvent('PROFILE_VIEW', preceptorId);
      } catch (err: any) {
        setError(err?.message || 'Failed to load preceptor profile.');
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, [preceptorId, isStudent]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  if (!preceptorId) {
    return <Navigate to="/student/browse" replace />;
  }

  const handleRevealContact = async () => {
    try {
      setIsRevealing(true);
      setError(null);
      const data = await preceptorService.revealContact(preceptorId);
      setContact(data);
      await preceptorService.trackAnalyticsEvent('CONTACT_REVEAL', preceptorId);
      setToast({ type: 'success', message: 'Contact details unlocked successfully.' });
    } catch (err: any) {
      setError(err?.message || 'Unable to reveal contact details.');
    } finally {
      setIsRevealing(false);
    }
  };

  const handleSavePreceptor = async () => {
    if (!studentUserId) {
      setError('Unable to identify your student account. Please login again.');
      return;
    }

    try {
      setIsSaving(true);
      setError(null);
      await studentService.savePreceptor(studentUserId, preceptorId);
      setIsSaved(true);
      setToast({ type: 'success', message: 'Preceptor saved to your list.' });
    } catch (err: any) {
      setError(err?.message || 'Failed to save preceptor.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <StudentLayout
      pageTitle="Preceptor Detail"
      headerLeading={
        <button
          type="button"
          onClick={() => navigate('/student/browse')}
          className="inline-flex items-center gap-1 rounded-full border border-slate-300 px-3 py-1.5 text-sm font-semibold text-slate-700 transition-colors hover:bg-slate-50"
        >
          <span className="material-symbols-outlined text-base">arrow_back</span>
          Back
        </button>
      }
    >
      <div className="mx-auto max-w-7xl space-y-5">
        {toast ? (
          <div
            className={`rounded-xl px-4 py-3 text-sm font-medium ${
              toast.type === 'success'
                ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                : 'border border-red-200 bg-red-50 text-red-700'
            }`}
          >
            {toast.message}
          </div>
        ) : null}

        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {loading ? (
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
            <div className="space-y-4">
              <div className="h-44 animate-pulse rounded-2xl bg-slate-200/70" />
              <div className="h-64 animate-pulse rounded-2xl bg-slate-200/70" />
            </div>
            <div className="h-72 animate-pulse rounded-2xl bg-slate-200/70" />
          </div>
        ) : preceptorData ? (
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
            <section className="space-y-5">
              <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                <div className="mb-4 flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h2 className="text-3xl font-black tracking-tight text-slate-900">
                      {preceptorData.displayName || 'Preceptor'}
                    </h2>
                    <p className="mt-1 text-sm font-medium text-slate-500">
                      {preceptorData.credentials || 'Credentials not listed'}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider ${
                        preceptorData.isVerified ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                      }`}
                    >
                      {preceptorData.isVerified ? 'Verified' : 'Pending Verification'}
                    </span>
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-[11px] font-bold uppercase tracking-wider ${
                        isPremium ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {isPremium ? 'Premium' : 'Free'}
                    </span>
                  </div>
                </div>

                <div className="grid grid-cols-1 gap-4 text-sm md:grid-cols-2">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Specialty</p>
                    <p className="mt-1 text-slate-700">{preceptorData.specialty || 'Not listed'}</p>
                  </div>
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Location</p>
                    <p className="mt-1 text-slate-700">{preceptorData.location || 'Not listed'}</p>
                  </div>
                </div>
              </article>

              <article className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
                <h3 className="text-xl font-bold text-slate-900">Profile Details</h3>
                <div className="mt-4 grid grid-cols-1 gap-4 text-sm md:grid-cols-2">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Clinical Setting</p>
                    <p className="mt-1 text-slate-700">{preceptorData.setting || 'Not listed'}</p>
                  </div>
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Honorarium</p>
                    <p className="mt-1 text-slate-700">{preceptorData.honorarium || 'Not listed'}</p>
                  </div>
                  <div className="md:col-span-2">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Available Days</p>
                    <div className="mt-2 flex flex-wrap gap-2">
                      {(preceptorData.availableDays || []).length > 0 ? (
                        preceptorData.availableDays?.map((day) => (
                          <span
                            key={day}
                            className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700"
                          >
                            {formatDay(day)}
                          </span>
                        ))
                      ) : (
                        <span className="text-slate-600">Not provided</span>
                      )}
                    </div>
                  </div>
                </div>

                <div className="mt-5">
                  <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Requirements</p>
                  <p className="mt-1 whitespace-pre-line text-sm text-slate-700">
                    {preceptorData.requirements || 'No additional requirements shared yet.'}
                  </p>
                </div>
              </article>
            </section>

            <aside className="space-y-5 lg:sticky lg:top-20 lg:self-start">
              <ContactCard
                isPremium={isPremium}
                contact={contact}
                isRevealing={isRevealing}
                onReveal={handleRevealContact}
              />
              <ActionButtons
                isSaved={isSaved}
                isSaving={isSaving}
                isRevealing={isRevealing}
                onSave={handleSavePreceptor}
                onReveal={handleRevealContact}
                onSendInquiry={() => setIsInquiryOpen(true)}
              />
            </aside>
          </div>
        ) : (
          <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-12 text-center text-sm font-medium text-slate-500">
            Preceptor profile unavailable.
          </div>
        )}

        <InquiryModal
          isOpen={isInquiryOpen}
          preceptor={
            preceptorData
              ? {
                  userId: preceptorData.userId,
                  displayName: preceptorData.displayName,
                  specialty: preceptorData.specialty,
                  location: preceptorData.location,
                }
              : null
          }
          onClose={() => setIsInquiryOpen(false)}
          onSuccess={(message) => setToast({ type: 'success', message })}
          onError={(message) => setError(message)}
        />
      </div>
    </StudentLayout>
  );
};

export default PreceptorDetail;
