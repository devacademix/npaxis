import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import adminService, { type AdminPreceptorDetail, type VerificationHistoryItem } from '../../services/admin';

interface ManagementPreceptor {
  userId: number;
  displayName: string;
  email: string;
  specialty?: string;
  location?: string;
  verificationStatus?: string;
  isPremium?: boolean;
}

const PreceptorManagement: React.FC = () => {
  const [preceptors, setPreceptors] = useState<ManagementPreceptor[]>([]);
  const [selected, setSelected] = useState<AdminPreceptorDetail | null>(null);
  const [history, setHistory] = useState<VerificationHistoryItem[]>([]);
  const [filters, setFilters] = useState({ specialty: '', location: '', verificationStatus: '' });
  const [note, setNote] = useState('');
  const [rejectReason, setRejectReason] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const loadPreceptors = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await adminService.getAdminPreceptors({
        specialty: filters.specialty || undefined,
        location: filters.location || undefined,
        verificationStatus: filters.verificationStatus || undefined,
      });
      setPreceptors(response);
    } catch (err: any) {
      setError(err?.message || 'Unable to load preceptors.');
    } finally {
      setIsLoading(false);
    }
  };

  const loadDetail = async (userId: number) => {
    try {
      setDetailLoading(true);
      const [detail, verificationHistory] = await Promise.all([
        adminService.getAdminPreceptorDetail(userId),
        adminService.getPreceptorVerificationHistory(userId).catch(() => []),
      ]);
      setSelected(detail);
      setHistory(verificationHistory);
    } catch (err: any) {
      setError(err?.message || 'Unable to load preceptor detail.');
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    loadPreceptors();
  }, []);

  const handleAddNote = async () => {
    if (!selected || !note.trim()) return;
    try {
      await adminService.addPreceptorVerificationNote(selected.userId, note.trim());
      setSuccess('Verification note added.');
      setNote('');
      await loadDetail(selected.userId);
    } catch (err: any) {
      setError(err?.message || 'Failed to add note.');
    }
  };

  const handleReject = async () => {
    if (!selected || !rejectReason.trim()) return;
    try {
      await adminService.rejectPreceptorWithReason(selected.userId, rejectReason.trim());
      setSuccess('Preceptor rejected successfully.');
      setRejectReason('');
      await loadPreceptors();
      await loadDetail(selected.userId);
    } catch (err: any) {
      setError(err?.message || 'Failed to reject preceptor.');
    }
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Preceptor Management</p>
          <h1 className="text-3xl font-bold text-slate-900">Admin Preceptor Operations</h1>
          <p className="text-sm text-slate-500">Use the documented administration endpoints for detail review, billing insight, verification history, and audit notes.</p>
        </header>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}

        <section className="grid gap-4 md:grid-cols-4">
          <input value={filters.specialty} onChange={(e) => setFilters((prev) => ({ ...prev, specialty: e.target.value }))} placeholder="Filter by specialty" className="rounded-xl border border-slate-200 px-4 py-3 text-sm" />
          <input value={filters.location} onChange={(e) => setFilters((prev) => ({ ...prev, location: e.target.value }))} placeholder="Filter by location" className="rounded-xl border border-slate-200 px-4 py-3 text-sm" />
          <select value={filters.verificationStatus} onChange={(e) => setFilters((prev) => ({ ...prev, verificationStatus: e.target.value }))} className="rounded-xl border border-slate-200 px-4 py-3 text-sm">
            <option value="">All statuses</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
          <button type="button" onClick={loadPreceptors} className="rounded-full bg-blue-700 px-5 py-3 text-sm font-bold text-white hover:bg-blue-800">Apply Filters</button>
        </section>

        <section className="grid gap-6 lg:grid-cols-[1.2fr,1fr]">
          <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Preceptor Directory</h2>
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">{preceptors.length} result(s)</span>
            </div>
            {isLoading ? (
              <div className="mt-4 space-y-3">
                {Array.from({ length: 4 }, (_, index) => <div key={index} className="h-16 animate-pulse rounded-xl bg-slate-200/70" />)}
              </div>
            ) : (
              <div className="mt-4 space-y-3">
                {preceptors.map((preceptor) => (
                  <button
                    key={preceptor.userId}
                    type="button"
                    onClick={() => loadDetail(preceptor.userId)}
                    className={`w-full rounded-2xl border px-4 py-4 text-left transition ${selected?.userId === preceptor.userId ? 'border-blue-300 bg-blue-50' : 'border-slate-100 bg-slate-50/80 hover:border-slate-300'}`}
                  >
                    <div className="flex items-center justify-between gap-4">
                      <div>
                        <p className="text-base font-semibold text-slate-900">{preceptor.displayName}</p>
                        <p className="text-xs text-slate-500">{preceptor.email}</p>
                        <p className="mt-1 text-xs text-slate-500">{preceptor.specialty || 'Specialty unavailable'} • {preceptor.location || 'Location unavailable'}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-xs font-bold uppercase tracking-wider text-slate-500">{preceptor.verificationStatus || 'UNKNOWN'}</p>
                        <p className="mt-1 text-xs text-slate-400">{preceptor.isPremium ? 'Premium' : 'Standard'}</p>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <h2 className="text-lg font-semibold text-slate-900">Preceptor Detail</h2>
            {detailLoading ? (
              <div className="mt-4 h-52 animate-pulse rounded-xl bg-slate-200/70" />
            ) : selected ? (
              <div className="mt-4 space-y-4">
                <div className="space-y-1">
                  <p className="text-xl font-bold text-slate-900">{selected.displayName}</p>
                  <p className="text-sm text-slate-500">{selected.email}</p>
                  <p className="text-sm text-slate-500">{selected.specialty || 'Specialty unavailable'} • {selected.location || 'Location unavailable'}</p>
                </div>

                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div className="rounded-xl bg-slate-50 p-3"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Verification</p><p className="mt-1 font-semibold text-slate-800">{selected.verificationStatus || 'N/A'}</p></div>
                  <div className="rounded-xl bg-slate-50 p-3"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Premium</p><p className="mt-1 font-semibold text-slate-800">{selected.isPremium ? 'Yes' : 'No'}</p></div>
                </div>

                <div className="flex flex-wrap gap-2">
                  <a href={adminService.getAdminLicenseReviewUrl(selected.userId)} target="_blank" rel="noreferrer" className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50">Review License</a>
                  <a href={adminService.getAdminLicenseDownloadUrl(selected.userId)} target="_blank" rel="noreferrer" className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50">Download License</a>
                </div>

                <div>
                  <h3 className="text-sm font-bold text-slate-900">Add Verification Note</h3>
                  <textarea value={note} onChange={(e) => setNote(e.target.value)} rows={3} className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Add review note..." />
                  <button type="button" onClick={handleAddNote} className="mt-2 rounded-full bg-blue-700 px-4 py-2 text-xs font-bold text-white hover:bg-blue-800">Save Note</button>
                </div>

                <div>
                  <h3 className="text-sm font-bold text-slate-900">Reject Preceptor</h3>
                  <textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} rows={3} className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Provide rejection reason..." />
                  <button type="button" onClick={handleReject} className="mt-2 rounded-full border border-red-200 px-4 py-2 text-xs font-bold text-red-600 hover:bg-red-50">Reject with Reason</button>
                </div>

                <div>
                  <h3 className="text-sm font-bold text-slate-900">Verification History</h3>
                  <div className="mt-2 space-y-2">
                    {history.length === 0 ? (
                      <p className="text-sm text-slate-500">No verification history found.</p>
                    ) : (
                      history.map((item) => (
                        <div key={item.auditId} className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                          <p className="font-semibold text-slate-800">{item.previousStatus || 'N/A'} → {item.newStatus || 'N/A'}</p>
                          <p>{item.reviewNote || 'No note provided'}</p>
                          <p className="mt-1 text-xs text-slate-400">{item.changeTimestamp ? new Date(item.changeTimestamp).toLocaleString() : 'N/A'}</p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            ) : (
              <p className="mt-4 text-sm text-slate-500">Select a preceptor to inspect detail, notes, and verification history.</p>
            )}
          </div>
        </section>
      </div>
    </AdminLayout>
  );
};

export default PreceptorManagement;
