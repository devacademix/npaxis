import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { preceptorService, type PreceptorSearchItem } from '../../services/preceptor';
import { adminService } from '../../services/admin';

type ManagementPreceptor = PreceptorSearchItem & {
  status: 'ACTIVE' | 'DELETED';
  verificationLabel: string;
  email?: string;
};

const PreceptorManagement: React.FC = () => {
  const [preceptors, setPreceptors] = useState<ManagementPreceptor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadPreceptors();
  }, []);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const loadPreceptors = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await adminService.getAdminPreceptors({ page: 0, size: 100 });
      const mapped = response.map((item) => ({
        ...item,
        status: 'ACTIVE' as const,
        verificationLabel: item.verificationStatus ?? (item.isVerified ? 'APPROVED' : 'PENDING'),
      }));
      setPreceptors(mapped);
    } catch (err: any) {
      setError(err?.message || 'Unable to load preceptors.');
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusBadge = (preceptor: ManagementPreceptor) => {
    if (preceptor.status === 'DELETED') {
      return 'Deleted';
    }
    if (preceptor.isVerified) {
      return 'Verified';
    }
    return preceptor.verificationLabel || 'Pending';
  };

  const handleVerify = async (preceptor: ManagementPreceptor) => {
    setActionLoadingId(preceptor.userId);
    try {
      await preceptorService.verifyPreceptor(preceptor.userId);
      setPreceptors((prev) =>
        prev.map((item) => (item.userId === preceptor.userId ? { ...item, isVerified: true, verificationLabel: 'APPROVED' } : item))
      );
      setToast({ type: 'success', message: 'Preceptor marked as verified.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Verification failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleSoftDelete = async (preceptor: ManagementPreceptor) => {
    setActionLoadingId(preceptor.userId);
    try {
      await preceptorService.softDeletePreceptor(preceptor.userId);
      setPreceptors((prev) =>
        prev.map((item) =>
          item.userId === preceptor.userId ? { ...item, status: 'DELETED' } : item
        )
      );
      setToast({ type: 'success', message: 'Preceptor soft deleted.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Delete failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleHardDelete = async (preceptor: ManagementPreceptor) => {
    if (!window.confirm('Permanently delete this preceptor?')) return;
    setActionLoadingId(preceptor.userId);
    try {
      await preceptorService.hardDeletePreceptor(preceptor.userId);
      setPreceptors((prev) => prev.filter((item) => item.userId !== preceptor.userId));
      setToast({ type: 'success', message: 'Preceptor permanently deleted.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Hard delete failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleRestore = async (preceptor: ManagementPreceptor) => {
    setActionLoadingId(preceptor.userId);
    try {
      await preceptorService.restorePreceptor(preceptor.userId);
      setPreceptors((prev) =>
        prev.map((item) => (item.userId === preceptor.userId ? { ...item, status: 'ACTIVE' } : item))
      );
      setToast({ type: 'success', message: 'Preceptor restored.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Restore failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const totalCount = preceptors.length;
  const deletedCount = preceptors.filter((preceptor) => preceptor.status === 'DELETED').length;

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Preceptor Management</p>
          <h1 className="text-3xl font-bold text-slate-900">Preceptor Oversight</h1>
          <p className="text-sm text-slate-500">Manage verification, deletion, and restoration of preceptor accounts.</p>
        </header>

        {toast && (
          <div className={`rounded-lg px-4 py-3 text-sm font-medium ${toast.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'}`}>
            {toast.message}
          </div>
        )}

        <section className="grid gap-5 md:grid-cols-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Total preceptors</p>
            <p className="text-4xl font-bold">{totalCount}</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Deleted accounts</p>
            <p className="text-4xl font-bold text-rose-500">{deletedCount}</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-gradient-to-r from-[#065f46] to-[#047857] p-5 shadow-lg text-white">
            <p className="text-xs uppercase tracking-[0.4em] text-emerald-200">Active verifications</p>
            <p className="text-4xl font-bold">
              {preceptors.filter((preceptor) => preceptor.isVerified).length}
            </p>
          </div>
        </section>

        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-900">Preceptor roster</h2>
            <button
              type="button"
              onClick={loadPreceptors}
              className="text-xs font-semibold text-blue-600 hover:text-blue-500"
            >
              Refresh
            </button>
          </div>

          {isLoading ? (
            <p className="mt-4 text-sm text-slate-500">Loading preceptors...</p>
          ) : error ? (
            <p className="mt-4 text-sm text-rose-600">{error}</p>
          ) : (
            <div className="mt-4 space-y-4">
              {preceptors.length === 0 && (
                <p className="py-6 text-center text-sm text-slate-500">No preceptors found.</p>
              )}
              {preceptors.map((preceptor) => {
                const isDeleted = preceptor.status === 'DELETED';
                const loading = actionLoadingId === preceptor.userId;
                return (
                  <div key={preceptor.userId} className="rounded-2xl border border-slate-100 bg-slate-50/80 p-4">
                    <div className="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                      <div>
                        <p className="text-base font-semibold text-slate-900">{preceptor.displayName}</p>
                        <p className="text-xs text-slate-500">{preceptor.email || 'No email provided'}</p>
                      </div>
                      <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.3em]">
                        <span
                          className={`rounded-full px-2 py-1 ${
                            isDeleted ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-600'
                          }`}
                        >
                          {getStatusBadge(preceptor)}
                        </span>
                        <span className="rounded-full bg-slate-200 px-2 py-1 text-slate-600">
                          {preceptor.isPremium ? 'Premium' : 'Standard'}
                        </span>
                      </div>
                    </div>
                    <div className="mt-4 flex flex-wrap items-center gap-2">
                      {!preceptor.isVerified && (
                        <button
                          type="button"
                          onClick={() => handleVerify(preceptor)}
                          disabled={loading}
                          className="rounded-full border border-emerald-500 px-3 py-1 text-xs font-semibold text-emerald-600 hover:bg-emerald-50 disabled:opacity-60"
                        >
                          {loading ? 'Processing...' : 'Verify'}
                        </button>
                      )}
                      {!isDeleted && (
                        <>
                          <button
                            type="button"
                            onClick={() => handleSoftDelete(preceptor)}
                            disabled={loading}
                            className="rounded-full border border-amber-300 px-3 py-1 text-xs font-semibold text-amber-600 hover:bg-amber-50 disabled:opacity-60"
                          >
                            Soft delete
                          </button>
                          <button
                            type="button"
                            onClick={() => handleHardDelete(preceptor)}
                            disabled={loading}
                            className="rounded-full border border-rose-200 px-3 py-1 text-xs font-semibold text-rose-600 hover:bg-rose-50 disabled:opacity-60"
                          >
                            Hard delete
                          </button>
                        </>
                      )}
                      {isDeleted && (
                        <button
                          type="button"
                          onClick={() => handleRestore(preceptor)}
                          disabled={loading}
                          className="rounded-full border border-emerald-200 px-3 py-1 text-xs font-semibold text-emerald-600 hover:bg-emerald-50 disabled:opacity-60"
                        >
                          Restore
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </div>
    </AdminLayout>
  );
};

export default PreceptorManagement;
