import React, { useEffect, useMemo, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { preceptorService, type PreceptorSearchItem } from '../../services/preceptor';
import { adminService } from '../../services/admin';

type ManagementPreceptor = PreceptorSearchItem & {
  status: 'ACTIVE' | 'DELETED';
  verificationLabel: string;
  email?: string;
};

type PreceptorTab = 'all' | 'approved' | 'rejected';

const PreceptorManagement: React.FC = () => {
  const [preceptors, setPreceptors] = useState<ManagementPreceptor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<PreceptorTab>('all');
  const [search, setSearch] = useState('');
  const [selectedId, setSelectedId] = useState<number | string | null>(null);
  const [selectedDetail, setSelectedDetail] = useState<any | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [history, setHistory] = useState<any[]>([]);
  const [billing, setBilling] = useState<any | null>(null);
  const [analytics, setAnalytics] = useState<any | null>(null);
  const [editForm, setEditForm] = useState({
    displayName: '',
    specialty: '',
    location: '',
    verificationStatus: '',
  });
  const [noteForm, setNoteForm] = useState({ note: '', noteType: 'GENERAL' });

  useEffect(() => {
    void loadPreceptors();
  }, [activeTab]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const mapPreceptors = (response: any[]) =>
    response.map((item) => ({
      ...item,
      status: 'ACTIVE' as const,
      verificationLabel: item.verificationStatus ?? (item.isVerified ? 'APPROVED' : 'PENDING'),
    }));

  const loadPreceptors = async () => {
    setIsLoading(true);
    setError(null);
    try {
      let response: any[] = [];
      if (activeTab === 'approved') {
        response = await adminService.getApprovedPreceptors({ page: 0, size: 100 });
      } else if (activeTab === 'rejected') {
        response = await adminService.getRejectedPreceptors({ page: 0, size: 100 });
      } else {
        response = await adminService.getAdminPreceptors({ page: 0, size: 100 });
      }
      setPreceptors(mapPreceptors(response));
    } catch (err: any) {
      setError(err?.message || 'Unable to load preceptors.');
    } finally {
      setIsLoading(false);
    }
  };

  const searchPreceptors = async () => {
    setIsLoading(true);
    setError(null);
    try {
      if (!search.trim()) {
        await loadPreceptors();
        return;
      }
      const response = await adminService.searchAdminPreceptors({ keyword: search.trim(), page: 0, size: 100 });
      setPreceptors(mapPreceptors(response));
    } catch (err: any) {
      setError(err?.message || 'Unable to search preceptors.');
    } finally {
      setIsLoading(false);
    }
  };

  const loadDetail = async (userId: number | string) => {
    setSelectedId(userId);
    setDetailLoading(true);
    setDetailError(null);
    try {
      const [detail, historyResponse, billingResponse, analyticsResponse] = await Promise.all([
        adminService.getAdminPreceptorDetail(userId),
        adminService.getPreceptorVerificationHistory(userId).catch(() => []),
        adminService.getPreceptorBillingReport(userId).catch(() => null),
        adminService.getAdminPreceptorAnalytics(userId).catch(() => null),
      ]);
      setSelectedDetail(detail);
      setEditForm({
        displayName: String(detail?.displayName ?? detail?.name ?? ''),
        specialty: String(detail?.specialty ?? ''),
        location: String(detail?.location ?? ''),
        verificationStatus: String(detail?.verificationStatus ?? ''),
      });
      setHistory(Array.isArray(historyResponse) ? historyResponse : []);
      setBilling(billingResponse);
      setAnalytics(analyticsResponse);
    } catch (err: any) {
      setDetailError(err?.message || 'Unable to load preceptor detail.');
    } finally {
      setDetailLoading(false);
    }
  };

  const getStatusBadge = (preceptor: ManagementPreceptor) => {
    if (preceptor.status === 'DELETED') return 'Deleted';
    if (preceptor.isVerified) return 'Verified';
    return preceptor.verificationLabel || 'Pending';
  };

  const openBlob = (blob: Blob, fileName: string, mode: 'view' | 'download') => {
    const objectUrl = URL.createObjectURL(blob);
    if (mode === 'view') {
      window.open(objectUrl, '_blank', 'noopener,noreferrer');
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 10000);
      return;
    }
    const anchor = document.createElement('a');
    anchor.href = objectUrl;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(objectUrl);
  };

  const handleLicenseAction = async (userId: number | string, mode: 'view' | 'download') => {
    setActionLoadingId(userId);
    try {
      const blob =
        mode === 'view'
          ? await adminService.reviewPreceptorLicenseBlob(userId)
          : await adminService.downloadPreceptorLicenseBlob(userId);
      openBlob(blob, `preceptor-license-${userId}`, mode);
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Unable to open license document.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleVerify = async (preceptor: ManagementPreceptor) => {
    setActionLoadingId(preceptor.userId);
    try {
      await preceptorService.verifyPreceptor(preceptor.userId);
      setPreceptors((prev) =>
        prev.map((item) =>
          item.userId === preceptor.userId ? { ...item, isVerified: true, verificationLabel: 'APPROVED' } : item
        )
      );
      if (selectedId === preceptor.userId) {
        await loadDetail(preceptor.userId);
      }
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
        prev.map((item) => (item.userId === preceptor.userId ? { ...item, status: 'DELETED' } : item))
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
      if (selectedId === preceptor.userId) {
        setSelectedId(null);
        setSelectedDetail(null);
      }
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

  const handleSaveDetail = async () => {
    if (!selectedId) return;
    setActionLoadingId(selectedId);
    try {
      await adminService.updateAdminPreceptor(selectedId, {
        displayName: editForm.displayName,
        specialty: editForm.specialty,
        location: editForm.location,
        verificationStatus: editForm.verificationStatus,
      });
      await Promise.all([loadPreceptors(), loadDetail(selectedId)]);
      setToast({ type: 'success', message: 'Preceptor detail updated.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Unable to update preceptor.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleAddNote = async () => {
    if (!selectedId || !noteForm.note.trim()) return;
    setActionLoadingId(selectedId);
    try {
      await adminService.addPreceptorVerificationNote(selectedId, noteForm.note.trim(), noteForm.noteType);
      setNoteForm({ note: '', noteType: 'GENERAL' });
      await loadDetail(selectedId);
      setToast({ type: 'success', message: 'Verification note added.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Unable to add note.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const totalCount = preceptors.length;
  const deletedCount = preceptors.filter((preceptor) => preceptor.status === 'DELETED').length;
  const verifiedCount = preceptors.filter((preceptor) => preceptor.isVerified).length;
  const selectedLoading = selectedId != null && actionLoadingId === selectedId;

  const renderedHistory = useMemo(() => history.slice(0, 6), [history]);

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Preceptor Management</p>
          <h1 className="text-3xl font-bold text-slate-900">Preceptor Oversight</h1>
          <p className="text-sm text-slate-500">Manage verification, search, profile updates, history, and license review.</p>
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
            <p className="text-xs uppercase tracking-[0.4em] text-emerald-200">Verified</p>
            <p className="text-4xl font-bold">{verifiedCount}</p>
          </div>
        </section>

        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <h2 className="text-lg font-semibold text-slate-900">Preceptor roster</h2>
            <div className="flex flex-col gap-3 md:flex-row">
              <div className="flex items-center rounded-full border border-slate-200 bg-slate-50 p-1 text-xs font-semibold">
                {(['all', 'approved', 'rejected'] as PreceptorTab[]).map((tab) => (
                  <button
                    key={tab}
                    type="button"
                    onClick={() => setActiveTab(tab)}
                    className={`rounded-full px-4 py-2 uppercase tracking-[0.2em] ${activeTab === tab ? 'bg-blue-600 text-white' : 'text-slate-600'}`}
                  >
                    {tab}
                  </button>
                ))}
              </div>
              <div className="flex gap-2">
                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search preceptors"
                  className="rounded-full border border-slate-200 px-4 py-2 text-sm"
                />
                <button
                  type="button"
                  onClick={searchPreceptors}
                  className="rounded-full border border-blue-200 px-4 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-50"
                >
                  Search
                </button>
                <button
                  type="button"
                  onClick={loadPreceptors}
                  className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                >
                  Refresh
                </button>
              </div>
            </div>
          </div>

          {isLoading ? (
            <p className="mt-4 text-sm text-slate-500">Loading preceptors...</p>
          ) : error ? (
            <p className="mt-4 text-sm text-rose-600">{error}</p>
          ) : (
            <div className="mt-4 grid gap-6 lg:grid-cols-[1.2fr,1fr]">
              <div className="space-y-4">
                {preceptors.length === 0 && (
                  <p className="py-6 text-center text-sm text-slate-500">No preceptors found.</p>
                )}
                {preceptors.map((preceptor) => {
                  const isDeleted = preceptor.status === 'DELETED';
                  const loading = actionLoadingId === preceptor.userId;
                  const isSelected = selectedId === preceptor.userId;
                  return (
                    <div
                      key={preceptor.userId}
                      className={`rounded-2xl border p-4 ${isSelected ? 'border-blue-300 bg-blue-50/40' : 'border-slate-100 bg-slate-50/80'}`}
                    >
                      <div className="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                        <div>
                          <button
                            type="button"
                            onClick={() => loadDetail(preceptor.userId)}
                            className="text-left text-base font-semibold text-slate-900 hover:text-blue-700"
                          >
                            {preceptor.displayName}
                          </button>
                          <p className="text-xs text-slate-500">{preceptor.email || 'No email provided'}</p>
                          <p className="mt-1 text-xs text-slate-400">{preceptor.specialty || 'No specialty'} · {preceptor.location || 'No location'}</p>
                        </div>
                        <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.3em]">
                          <span className={`rounded-full px-2 py-1 ${isDeleted ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-600'}`}>
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
                        <button
                          type="button"
                          onClick={() => handleLicenseAction(preceptor.userId, 'view')}
                          disabled={loading}
                          className="rounded-full border border-blue-200 px-3 py-1 text-xs font-semibold text-blue-700 hover:bg-blue-50 disabled:opacity-60"
                        >
                          Review License
                        </button>
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

              <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-slate-900">Detail panel</h3>
                  {selectedDetail ? (
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => handleLicenseAction(selectedDetail.userId ?? selectedId ?? '', 'view')}
                        disabled={selectedLoading}
                        className="rounded-full border border-slate-200 px-3 py-1 text-xs font-semibold text-slate-700"
                      >
                        View License
                      </button>
                      <button
                        type="button"
                        onClick={() => handleLicenseAction(selectedDetail.userId ?? selectedId ?? '', 'download')}
                        disabled={selectedLoading}
                        className="rounded-full border border-blue-200 px-3 py-1 text-xs font-semibold text-blue-700"
                      >
                        Download
                      </button>
                    </div>
                  ) : null}
                </div>

                {detailLoading ? (
                  <p className="mt-4 text-sm text-slate-500">Loading details...</p>
                ) : detailError ? (
                  <p className="mt-4 text-sm text-rose-600">{detailError}</p>
                ) : !selectedDetail ? (
                  <p className="mt-4 text-sm text-slate-500">Select a preceptor to inspect live admin detail, history, billing, and analytics.</p>
                ) : (
                  <div className="mt-4 space-y-5 text-sm text-slate-600">
                    <div className="grid gap-3 md:grid-cols-2">
                      <label className="space-y-1">
                        <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Name</span>
                        <input value={editForm.displayName} onChange={(e) => setEditForm((prev) => ({ ...prev, displayName: e.target.value }))} className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm" />
                      </label>
                      <label className="space-y-1">
                        <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Specialty</span>
                        <input value={editForm.specialty} onChange={(e) => setEditForm((prev) => ({ ...prev, specialty: e.target.value }))} className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm" />
                      </label>
                      <label className="space-y-1">
                        <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Location</span>
                        <input value={editForm.location} onChange={(e) => setEditForm((prev) => ({ ...prev, location: e.target.value }))} className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm" />
                      </label>
                      <label className="space-y-1">
                        <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Verification</span>
                        <input value={editForm.verificationStatus} onChange={(e) => setEditForm((prev) => ({ ...prev, verificationStatus: e.target.value }))} className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm" />
                      </label>
                    </div>

                    <div className="flex justify-end">
                      <button
                        type="button"
                        onClick={handleSaveDetail}
                        disabled={selectedLoading}
                        className="rounded-full bg-blue-600 px-4 py-2 text-xs font-semibold uppercase tracking-[0.3em] text-white disabled:opacity-60"
                      >
                        {selectedLoading ? 'Saving...' : 'Save Detail'}
                      </button>
                    </div>

                    <div className="rounded-xl bg-slate-50 p-4">
                      <h4 className="text-sm font-bold text-slate-900">Verification Notes</h4>
                      <div className="mt-3 flex flex-col gap-3">
                        <select
                          value={noteForm.noteType}
                          onChange={(e) => setNoteForm((prev) => ({ ...prev, noteType: e.target.value }))}
                          className="rounded-lg border border-slate-200 px-3 py-2 text-sm"
                        >
                          <option value="GENERAL">General</option>
                          <option value="APPROVAL">Approval</option>
                          <option value="REJECTION">Rejection</option>
                        </select>
                        <textarea
                          value={noteForm.note}
                          onChange={(e) => setNoteForm((prev) => ({ ...prev, note: e.target.value }))}
                          className="min-h-24 rounded-lg border border-slate-200 px-3 py-2 text-sm"
                          placeholder="Add verification note"
                        />
                        <button
                          type="button"
                          onClick={handleAddNote}
                          disabled={selectedLoading || !noteForm.note.trim()}
                          className="self-end rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold uppercase tracking-[0.3em] text-slate-700 disabled:opacity-60"
                        >
                          Add Note
                        </button>
                      </div>
                    </div>

                    <div className="grid gap-4 lg:grid-cols-2">
                      <div className="rounded-xl border border-slate-200 p-4">
                        <h4 className="text-sm font-bold text-slate-900">Verification History</h4>
                        <div className="mt-3 space-y-2">
                          {renderedHistory.length === 0 ? (
                            <p className="text-xs text-slate-500">No verification history found.</p>
                          ) : (
                            renderedHistory.map((item, index) => (
                              <div key={index} className="rounded-lg bg-slate-50 px-3 py-2 text-xs text-slate-600">
                                <pre className="whitespace-pre-wrap font-sans">{JSON.stringify(item, null, 2)}</pre>
                              </div>
                            ))
                          )}
                        </div>
                      </div>

                      <div className="space-y-4">
                        <div className="rounded-xl border border-slate-200 p-4">
                          <h4 className="text-sm font-bold text-slate-900">Billing</h4>
                          <pre className="mt-3 whitespace-pre-wrap text-xs text-slate-600">{JSON.stringify(billing, null, 2)}</pre>
                        </div>
                        <div className="rounded-xl border border-slate-200 p-4">
                          <h4 className="text-sm font-bold text-slate-900">Analytics</h4>
                          <pre className="mt-3 whitespace-pre-wrap text-xs text-slate-600">{JSON.stringify(analytics, null, 2)}</pre>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </section>
      </div>
    </AdminLayout>
  );
};

export default PreceptorManagement;
