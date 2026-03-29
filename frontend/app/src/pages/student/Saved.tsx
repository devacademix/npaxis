import React, { useEffect, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import PreceptorCard from '../../components/student/PreceptorCard';
import { studentService, type StudentPreceptor } from '../../services/student';

const Saved: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');
  const navigate = useNavigate();

  const [studentUserId, setStudentUserId] = useState<number | null>(null);
  const [savedPreceptors, setSavedPreceptors] = useState<StudentPreceptor[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    if (!isStudent) return;

    const loadSaved = async () => {
      try {
        setLoading(true);
        setError(null);
        const user = await studentService.getLoggedInUser();
        setStudentUserId(user.userId);

        const saved = await studentService.getSavedPreceptors(user.userId);
        setSavedPreceptors(saved);
      } catch (err: any) {
        setError(err?.message || 'Unable to load saved preceptors.');
      } finally {
        setLoading(false);
      }
    };

    loadSaved();
  }, [isStudent]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  const handleRemoveSaved = async (preceptor: StudentPreceptor) => {
    if (!studentUserId) {
      setError('Unable to identify your student account. Please login again.');
      return;
    }

    const previous = savedPreceptors;
    setRemovingId(preceptor.userId);
    setSavedPreceptors((current) => current.filter((item) => item.userId !== preceptor.userId));

    try {
      const result = await studentService.removeSavedPreceptor(studentUserId, preceptor.userId);
      if (result.serverSynced) {
        setToast({ type: 'success', message: 'Preceptor removed from saved list.' });
      } else {
        setToast({ type: 'success', message: 'Removed from this view. Delete API is not available on backend yet.' });
      }
    } catch (err: any) {
      setSavedPreceptors(previous);
      setToast({ type: 'error', message: err?.message || 'Failed to remove preceptor.' });
    } finally {
      setRemovingId(null);
    }
  };

  return (
    <StudentLayout pageTitle="Saved Preceptors">
      <div className="mx-auto max-w-7xl">
        <section className="mb-6">
          <h2 className="text-3xl font-black tracking-tight text-slate-900">Saved Preceptors</h2>
          <p className="mt-1 text-sm text-slate-500">Your bookmarked preceptors for future reference.</p>
        </section>

        {error ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {toast ? (
          <div
            className={`mb-4 rounded-xl px-4 py-3 text-sm font-medium ${
              toast.type === 'success'
                ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                : 'border border-red-200 bg-red-50 text-red-700'
            }`}
          >
            {toast.message}
          </div>
        ) : null}

        {loading ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {Array.from({ length: 6 }, (_, index) => (
              <div key={index} className="h-[320px] animate-pulse rounded-2xl bg-slate-200/70" />
            ))}
          </div>
        ) : savedPreceptors.length > 0 ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {savedPreceptors.map((preceptor) => (
              <PreceptorCard
                key={preceptor.userId}
                preceptor={preceptor}
                variant="full"
                onViewProfile={() => navigate(`/student/preceptor-detail/${preceptor.userId}`)}
                secondaryActionLabel="Remove"
                secondaryActionIcon="delete"
                onSecondaryAction={handleRemoveSaved}
                isSecondaryLoading={removingId === preceptor.userId}
                secondaryDisabled={Boolean(removingId && removingId !== preceptor.userId)}
              />
            ))}
          </div>
        ) : (
          <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-6 py-16 text-center">
            <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-slate-200 text-slate-500">
              <span className="material-symbols-outlined text-2xl">bookmark_removed</span>
            </div>
            <p className="text-lg font-semibold text-slate-700">No saved preceptors yet</p>
            <p className="mt-1 text-sm text-slate-500">Start browsing and bookmark preceptors to revisit later.</p>
            <button
              type="button"
              onClick={() => navigate('/student/browse')}
              className="mt-5 inline-flex items-center gap-2 rounded-full bg-blue-700 px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-blue-800"
            >
              <span className="material-symbols-outlined text-base">travel_explore</span>
              Browse Preceptors
            </button>
          </div>
        )}
      </div>
    </StudentLayout>
  );
};

export default Saved;
