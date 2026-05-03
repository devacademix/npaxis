import React, { useState, useEffect } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { adminService } from '../../services/admin';

interface Preceptor {
  id: number | string;
  name: string;
  email: string;
  credentials?: string;
  licenseNumber: string;
  licenseFileUrl?: string;
  dateSubmitted: string;
  status: string;
  avatarUrl?: string;
}

const PendingPreceptors: React.FC = () => {
  const [preceptors, setPreceptors] = useState<Preceptor[]>([]);
  const [totalPending, setTotalPending] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Modal & Notification State
  const [modalState, setModalState] = useState<{ isOpen: boolean; type: 'approve' | 'reject' | null; preceptor: Preceptor | null }>({
    isOpen: false,
    type: null,
    preceptor: null
  });
  const [actionLoading, setActionLoading] = useState(false);
  const [bulkLoading, setBulkLoading] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  useEffect(() => {
    fetchPreceptors();
  }, [page]);

  // Display Toast for 3s
  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const fetchPreceptors = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await adminService.getPendingPreceptors({ page, size: 10 });
      setPreceptors(Array.isArray(response.items) ? response.items : []);
      setTotalPending(Number(response.totalElements ?? 0));
      setTotalPages(Math.max(1, Number(response.totalPages ?? 1)));
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch pending preceptors.');
      setPreceptors([]);
      setTotalPending(0);
      setTotalPages(1);
    } finally {
      setIsLoading(false);
    }
  };

  const handleActionClick = (type: 'approve' | 'reject', preceptor: Preceptor) => {
    setModalState({ isOpen: true, type, preceptor });
  };

  const confirmAction = async () => {
    if (!modalState.preceptor || !modalState.type) return;
    const { id } = modalState.preceptor;
    const actionType = modalState.type;

    try {
      setActionLoading(true);
      if (actionType === 'approve') {
        await adminService.approvePreceptor(id);
      } else {
        await adminService.rejectPreceptor(id);
      }
      
      // Update UI state locally
      setPreceptors(prev => prev.filter(p => p.id !== id));
      setToast({ message: `Preceptor successfully ${actionType}d.`, type: 'success' });
      setModalState({ isOpen: false, type: null, preceptor: null });
      
    } catch (err: any) {
      setToast({ message: `Failed to ${actionType} preceptor: ${err.message}`, type: 'error' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleExportAuditLog = () => {
    if (preceptors.length === 0) {
      setToast({ message: 'No records available to export.', type: 'error' });
      return;
    }

    const header = ['ID', 'Name', 'Email', 'Credentials', 'LicenseNumber', 'Status', 'SubmittedAt'];
    const rows = preceptors.map((preceptor) => [
      String(preceptor.id),
      `"${preceptor.name.replaceAll('"', '""')}"`,
      preceptor.email,
      `"${(preceptor.credentials || '').replaceAll('"', '""')}"`,
      preceptor.licenseNumber,
      preceptor.status,
      preceptor.dateSubmitted,
    ]);
    const csv = [header.join(','), ...rows.map((row) => row.join(','))].join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const exportUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = exportUrl;
    link.setAttribute('download', `pending-preceptors-${new Date().toISOString().slice(0, 10)}.csv`);
    link.click();
    URL.revokeObjectURL(exportUrl);
    setToast({ message: 'Audit log exported.', type: 'success' });
  };

  const handleBulkApprove = async () => {
    if (preceptors.length === 0) {
      setToast({ message: 'No pending preceptors to approve.', type: 'error' });
      return;
    }

    try {
      setBulkLoading(true);
      const currentItems = [...preceptors];
      const results = await Promise.allSettled(
        currentItems.map((preceptor) => adminService.approvePreceptor(preceptor.id))
      );
      const approvedIds = currentItems
        .filter((_, index) => results[index].status === 'fulfilled')
        .map((item) => item.id);
      const failedCount = results.length - approvedIds.length;

      setPreceptors((previous) => previous.filter((preceptor) => !approvedIds.includes(preceptor.id)));
      if (failedCount > 0) {
        setToast({
          message: `${approvedIds.length} approved, ${failedCount} failed. Please retry remaining records.`,
          type: 'error',
        });
      } else {
        setToast({ message: 'All pending preceptors approved successfully.', type: 'success' });
      }
    } catch (err: any) {
      setToast({ message: err?.message || 'Bulk approval failed.', type: 'error' });
    } finally {
      setBulkLoading(false);
    }
  };

  const handleViewDocument = (preceptor: Preceptor) => {
    if (!preceptor.licenseFileUrl) {
      setToast({ message: 'License document URL is not available.', type: 'error' });
      return;
    }
    window.open(preceptor.licenseFileUrl, '_blank', 'noopener,noreferrer');
  };

  const renderSkeletonTable = () => (
    <div className="animate-pulse space-y-4">
      {[...Array(3)].map((_, i) => (
        <div key={i} className="flex items-center space-x-4 px-6 py-5 border-b border-surface-container-low">
          <div className="w-10 h-10 rounded-lg bg-slate-200"></div>
          <div className="flex-1 space-y-2 py-1">
            <div className="h-4 bg-slate-200 rounded w-1/4"></div>
            <div className="h-3 bg-slate-200 rounded w-1/6"></div>
          </div>
          <div className="h-8 bg-slate-200 rounded w-32"></div>
        </div>
      ))}
    </div>
  );

  return (
    <AdminLayout>
      {/* Header Section */}
      <div className="mb-10 flex items-end justify-between">
        <div>
          <nav className="flex items-center gap-2 text-xs font-medium text-slate-400 uppercase tracking-[0.05em] mb-2 font-body">
            <span>Admin</span>
            <span className="material-symbols-outlined text-xs">chevron_right</span>
            <span className="text-blue-700">Verification</span>
          </nav>
          <h2 className="text-4xl font-extrabold text-on-surface tracking-tight font-headline">Preceptor Verification</h2>
          <p className="text-slate-500 mt-2 flex items-center gap-2">
            <span className="inline-block w-2 h-2 rounded-full bg-amber-600"></span>
            <span className="font-medium text-amber-700">{totalPending} Pending Applications</span> requiring review
          </p>
        </div>
        <div className="flex gap-3">
          <button
            type="button"
            onClick={handleExportAuditLog}
            className="px-5 py-2.5 rounded-full bg-surface-container-high text-slate-700 text-sm font-semibold hover:bg-slate-200 transition-colors"
          >
              Export Audit Log
          </button>
          <button
            type="button"
            onClick={handleBulkApprove}
            disabled={bulkLoading || preceptors.length === 0}
            className="px-5 py-2.5 rounded-full bg-gradient-to-br from-[#003d9b] to-[#0052cc] text-white text-sm font-semibold shadow-md hover:opacity-90 transition-all disabled:opacity-60 disabled:cursor-not-allowed"
          >
              {bulkLoading ? 'Processing...' : 'Bulk Approve'}
          </button>
        </div>
      </div>

      {error && preceptors.length === 0 && (
         <div className="mb-6 bg-error-container/50 border border-error-container p-4 rounded-lg text-on-error-container text-sm">
             {error}
         </div>
      )}

      {/* Verification Table */}
      <div className="bg-surface-container-lowest rounded-xl overflow-hidden ring-1 ring-slate-200 shadow-sm font-body">
        {isLoading ? renderSkeletonTable() : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50">
                  <th className="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-wider text-slate-500">Preceptor Name</th>
                  <th className="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-wider text-slate-500">Credentials</th>
                  <th className="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-wider text-slate-500">License Number</th>
                  <th className="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-wider text-slate-500">Date Submitted</th>
                  <th className="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-6 py-4 text-[0.6875rem] font-bold uppercase tracking-wider text-slate-500 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {preceptors.length === 0 ? (
                  <tr><td colSpan={6} className="px-6 py-8 text-center text-slate-500">No pending preceptors found.</td></tr>
                ) : (
                  preceptors.map((preceptor) => (
                    <tr key={preceptor.id} className="hover:bg-slate-50/50 transition-colors">
                      <td className="px-6 py-5">
                        <div className="flex items-center gap-3">
                          {preceptor.avatarUrl ? (
                            <img src={preceptor.avatarUrl} alt={preceptor.name} className="w-10 h-10 rounded-lg object-cover bg-slate-100" />
                          ) : (
                            <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center text-blue-700 font-bold">{preceptor.name.charAt(0)}</div>
                          )}
                          <div>
                            <p className="font-semibold text-on-surface">{preceptor.name}</p>
                            <p className="text-xs text-slate-500">{preceptor.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-5">
                        <span className="text-sm font-medium text-slate-600">{preceptor.credentials || 'N/A'}</span>
                      </td>
                      <td className="px-6 py-5">
                        <div className="flex items-center gap-2 group">
                          <span className="text-sm font-mono text-slate-700 bg-slate-100 px-2 py-1 rounded">{preceptor.licenseNumber}</span>
                          <button
                            type="button"
                            onClick={() => handleViewDocument(preceptor)}
                            className="text-blue-600 hover:bg-blue-50 p-1 rounded transition-colors"
                            title="View Document"
                          >
                            <span className="material-symbols-outlined text-lg">description</span>
                          </button>
                        </div>
                      </td>
                      <td className="px-6 py-5">
                        <span className="text-sm text-slate-500">{preceptor.dateSubmitted}</span>
                      </td>
                      <td className="px-6 py-5">
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">
                           {preceptor.status}
                        </span>
                      </td>
                      <td className="px-6 py-5 text-right">
                        <div className="flex justify-end gap-2">
                          <button 
                            onClick={() => handleActionClick('reject', preceptor)}
                            className="h-9 px-4 rounded-lg border border-red-200 text-red-600 text-xs font-bold hover:bg-red-50 hover:border-red-300 transition-colors">
                              Reject
                          </button>
                          <button 
                            onClick={() => handleActionClick('approve', preceptor)}
                            className="h-9 px-4 rounded-lg bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 transition-colors shadow-sm">
                              Approve
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
        {!isLoading && preceptors.length > 0 && (
          <div className="px-6 py-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
            <p className="text-xs text-slate-500 font-medium">
              Showing {preceptors.length} pending applications on page {page + 1} of {totalPages}
            </p>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setPage((current) => Math.max(current - 1, 0))}
                disabled={page <= 0}
                className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-slate-100"
              >
                Previous
              </button>
              <button
                type="button"
                onClick={() => setPage((current) => current + 1)}
                disabled={page + 1 >= totalPages}
                className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-slate-100"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Confirmation Modal */}
      {modalState.isOpen && modalState.preceptor && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
            <div className={`h-2 w-full ${modalState.type === 'approve' ? 'bg-emerald-500' : 'bg-red-500'}`}></div>
            <div className="p-6">
              <div className="flex items-center gap-4 mb-6">
                <div className={`w-12 h-12 rounded-full flex items-center justify-center ${modalState.type === 'approve' ? 'bg-emerald-100 text-emerald-600' : 'bg-red-100 text-red-600'}`}>
                  <span className="material-symbols-outlined text-2xl">
                    {modalState.type === 'approve' ? 'verified' : 'cancel'}
                  </span>
                </div>
                <div>
                  <h3 className="text-xl font-bold text-slate-900">Confirm {modalState.type === 'approve' ? 'Approval' : 'Rejection'}</h3>
                  <p className="text-sm text-slate-500">You are about to {modalState.type} this application.</p>
                </div>
              </div>

              <div className="bg-slate-50 p-4 rounded-xl mb-6 flex items-center gap-3 border border-slate-100">
                 {modalState.preceptor.avatarUrl ? (
                    <img src={modalState.preceptor.avatarUrl} alt="" className="w-10 h-10 rounded-lg object-cover" />
                  ) : null}
                 <div>
                   <p className="font-bold text-sm">{modalState.preceptor.name}</p>
                   <p className="text-xs text-slate-500">License: {modalState.preceptor.licenseNumber}</p>
                 </div>
              </div>

              <p className="text-sm text-slate-600 mb-6">
                {modalState.type === 'approve' 
                  ? "Are you sure you want to approve this preceptor? They will be granted full access to the platform immediately."
                  : "Are you sure you want to reject this preceptor? This action will notify the applicant."}
              </p>

              <div className="flex justify-end gap-3 font-body">
                <button 
                  onClick={() => setModalState({ isOpen: false, type: null, preceptor: null })}
                  disabled={actionLoading}
                  className="px-4 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                  Cancel
                </button>
                <button 
                  onClick={confirmAction}
                  disabled={actionLoading}
                  className={`px-5 py-2 text-sm font-bold text-white rounded-lg transition-all flex items-center gap-2 ${
                    modalState.type === 'approve' ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-red-600 hover:bg-red-700'
                  } ${actionLoading ? 'opacity-70 cursor-not-allowed' : ''}`}>
                  {actionLoading ? (
                     <span className="material-symbols-outlined animate-spin text-sm">progress_activity</span>
                  ) : null}
                  Confirm {modalState.type === 'approve' ? 'Approval' : 'Rejection'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Toast Notification */}
      {toast && (
        <div className={`fixed bottom-6 right-6 p-4 rounded-xl shadow-lg flex items-center gap-3 z-50 animate-in slide-in-from-bottom-5 font-body border 
          ${toast.type === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-red-50 border-red-200 text-red-800'}`}>
          <span className="material-symbols-outlined">
            {toast.type === 'success' ? 'check_circle' : 'error'}
          </span>
          <p className="text-sm font-semibold pr-4">{toast.message}</p>
          <button onClick={() => setToast(null)} className="opacity-70 hover:opacity-100">
            <span className="material-symbols-outlined text-sm">close</span>
          </button>
        </div>
      )}
    </AdminLayout>
  );
};

export default PendingPreceptors;
