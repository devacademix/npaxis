import React, { useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import FileUpload from '../../components/preceptor/FileUpload';
import StatusBadge from '../../components/preceptor/StatusBadge';
import { preceptorService, type VerificationStatus } from '../../services/preceptor';

const MAX_FILE_SIZE = 5 * 1024 * 1024;
const ALLOWED_TYPES = new Set(['application/pdf', 'image/png', 'image/jpeg']);

const formatDateTime = (value?: string) => {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'N/A';
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const License: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');

  const [userId, setUserId] = useState<number | null>(null);
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [verificationStatus, setVerificationStatus] = useState<VerificationStatus>('PENDING');
  const [submittedAt, setSubmittedAt] = useState<string | undefined>();
  const [reviewedAt, setReviewedAt] = useState<string | undefined>();
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadData = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const user = await preceptorService.getLoggedInUser();
        setUserId(user.userId);

        const profile = await preceptorService.getPreceptorById(user.userId);
        setVerificationStatus(profile.verificationStatus || 'PENDING');
        setSubmittedAt(profile.verificationSubmittedAt);
        setReviewedAt(profile.verificationReviewedAt);
      } catch (err: any) {
        setError(err?.message || 'Failed to load license verification data.');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [isPreceptor]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 3000);
    return () => window.clearTimeout(timer);
  }, [success]);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const onFileSelect = (selected: File | null) => {
    setError(null);
    setSuccess(null);
    setUploadProgress(0);

    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
      setPreviewUrl(null);
    }

    if (!selected) {
      setFile(null);
      return;
    }

    if (!ALLOWED_TYPES.has(selected.type)) {
      setError('Only PDF, JPG, and PNG files are allowed.');
      setFile(null);
      return;
    }

    if (selected.size > MAX_FILE_SIZE) {
      setError('File size must be less than 5MB.');
      setFile(null);
      return;
    }

    setFile(selected);
    if (selected.type.startsWith('image/')) {
      setPreviewUrl(URL.createObjectURL(selected));
    }
  };

  const onSubmit = async () => {
    if (!userId) {
      setError('Unable to identify your account. Please login again.');
      return;
    }
    if (!file) {
      setError('Please select a file before submitting.');
      return;
    }

    try {
      setIsSubmitting(true);
      setError(null);
      setSuccess(null);
      setUploadProgress(0);

      const updated = await preceptorService.submitLicense(
        userId,
        { file },
        (progressEvent) => {
          const total = progressEvent.total || file.size;
          if (!total) return;
          setUploadProgress(Math.min(100, Math.round((progressEvent.loaded * 100) / total)));
        }
      );

      setVerificationStatus(updated.verificationStatus || 'PENDING');
      setSubmittedAt(updated.verificationSubmittedAt);
      setReviewedAt(updated.verificationReviewedAt);
      setSuccess('License submitted successfully for verification.');
    } catch (err: any) {
      setError(err?.message || 'Failed to submit license for verification.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const content = useMemo(() => {
    if (isLoading) {
      return (
        <div className="space-y-4">
          <div className="h-20 animate-pulse rounded-2xl bg-slate-200/70" />
          <div className="h-[420px] animate-pulse rounded-2xl bg-slate-200/70" />
        </div>
      );
    }

    return (
      <div className="space-y-6">
        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{error}</div>
        ) : null}

        {success ? (
          <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {success}
          </div>
        ) : null}

        <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-bold text-slate-900">Upload License Document</h2>
          <p className="mt-1 text-sm text-slate-500">Upload your professional license using drag-and-drop or file browser.</p>

          <div className="mt-4">
            <FileUpload file={file} previewUrl={previewUrl} disabled={isSubmitting} onFileSelect={onFileSelect} />
          </div>

          {(isSubmitting || uploadProgress > 0) && (
            <div className="mt-4">
              <div className="mb-1 flex items-center justify-between text-xs font-semibold text-slate-600">
                <span>Upload Progress</span>
                <span>{uploadProgress}%</span>
              </div>
              <div className="h-2 rounded-full bg-slate-200">
                <div
                  className="h-2 rounded-full bg-blue-600 transition-all duration-200"
                  style={{ width: `${uploadProgress}%` }}
                />
              </div>
            </div>
          )}

          <button
            type="button"
            onClick={onSubmit}
            disabled={!file || isSubmitting}
            className="mt-5 inline-flex items-center gap-2 rounded-full bg-blue-700 px-6 py-3 text-sm font-bold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? (
              <>
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                Submitting...
              </>
            ) : (
              <>
                <span className="material-symbols-outlined text-base">upload</span>
                Submit for Verification
              </>
            )}
          </button>
        </section>

        <section className="grid grid-cols-1 gap-4 lg:grid-cols-3">
          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
            <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Current Status</p>
            <div className="mt-2">
              <StatusBadge status={verificationStatus} />
            </div>
          </div>

          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
            <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Submitted At</p>
            <p className="mt-2 text-sm font-semibold text-slate-800">{formatDateTime(submittedAt)}</p>
          </div>

          <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
            <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Reviewed At</p>
            <p className="mt-2 text-sm font-semibold text-slate-800">{formatDateTime(reviewedAt)}</p>
          </div>
        </section>

        <section className="rounded-2xl border border-blue-200 bg-blue-50 p-5">
          <h3 className="text-sm font-bold uppercase tracking-wider text-blue-700">Why Verification Matters</h3>
          <p className="mt-2 text-sm text-blue-900">
            License verification helps ensure trust and safety for students and institutions. Most reviews complete within
            24 to 72 hours. You will see status updates here once reviewed.
          </p>
        </section>
      </div>
    );
  }, [error, file, isLoading, isSubmitting, previewUrl, reviewedAt, submittedAt, success, uploadProgress, verificationStatus]);

  return (
    <PreceptorLayout pageTitle="License Verification">
      <div className="mx-auto max-w-6xl">
        <div className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">License Verification</h1>
          <p className="mt-1 text-slate-500">Upload your professional license for verification.</p>
        </div>
        {content}
      </div>
    </PreceptorLayout>
  );
};

export default License;
