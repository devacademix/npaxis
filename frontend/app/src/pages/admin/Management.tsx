import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { adminService, type AdminUser } from '../../services/admin';

type PendingPreceptorPanelItem = {
  id: number | string;
  name: string;
  email: string;
  status: string;
  dateSubmitted: string;
};

const AdminManagement: React.FC = () => {
  const [admins, setAdmins] = useState<AdminUser[]>([]);
  const [pendingPreceptors, setPendingPreceptors] = useState<PendingPreceptorPanelItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  useEffect(() => {
    loadManagers();
  }, []);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const loadManagers = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const [adminRoster, pendingList] = await Promise.all([
        adminService.getAdminRoster(),
        adminService.getPendingPreceptors(),
      ]);

      setAdmins(Array.isArray(adminRoster) ? adminRoster : []);
      setPendingPreceptors(
        (Array.isArray(pendingList?.items) ? pendingList.items : []).map((preceptor) => ({
          id: preceptor.id,
          name: preceptor.name,
          email: preceptor.email,
          status: preceptor.status,
          dateSubmitted: preceptor.dateSubmitted,
        })),
      );
    } catch (err: any) {
      setErrorMessage(err?.message || 'Unable to load admin data.');
    } finally {
      setIsLoading(false);
    }
  };

  const determineEnabledState = (admin: AdminUser) => {
    const normalized = (admin as any).isEnabled ?? (admin as any).enabled ?? true;
    return Boolean(normalized);
  };

  const handleToggle = async (admin: AdminUser) => {
    const currentUserId = Number(localStorage.getItem('userId'));
    if (Number(admin.userId) === currentUserId) {
      setToast({ type: 'error', message: 'You cannot disable your own admin account.' });
      return;
    }

    const nextEnabled = !determineEnabledState(admin);
    setActionLoadingId(Number(admin.userId));
    try {
      await adminService.toggleAdminAccount(admin.userId, nextEnabled);
      setAdmins((prev) =>
        prev.map((item) =>
          item.userId === admin.userId
            ? {
                ...item,
                isEnabled: nextEnabled,
                enabled: nextEnabled,
              }
            : item,
        ),
      );
      setToast({
        type: 'success',
        message: `Admin ${nextEnabled ? 'enabled' : 'disabled'} successfully.`,
      });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Failed to update admin status.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const adminCount = admins.length;
  const disabledCount = admins.filter((admin) => !determineEnabledState(admin)).length;

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header className="flex flex-col gap-2">
          <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">Administration</p>
          <div>
            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Admin Management</h1>
            <p className="text-sm text-slate-500">View administrators, toggle access, and monitor verification queue</p>
          </div>
        </header>

        {toast && (
          <div
            className={`rounded-lg px-4 py-3 text-sm font-medium ${toast.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'}`}
          >
            {toast.message}
          </div>
        )}

        <section className="grid gap-5 md:grid-cols-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Admins</p>
            <p className="text-4xl font-bold">{adminCount}</p>
            <p className="text-xs text-slate-400">Active administrators</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-slate-50/60 p-4 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Disabled</p>
            <p className="text-4xl font-bold text-rose-500">{disabledCount}</p>
            <p className="text-xs text-slate-400">Accounts awaiting re-activation</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-gradient-to-r from-[#0c4a6e] to-[#2563eb] p-4 shadow-md text-white">
            <p className="text-xs uppercase tracking-[0.4em] text-blue-200">Pending Approvals</p>
            <p className="text-4xl font-bold">{pendingPreceptors.length}</p>
            <p className="text-xs text-blue-100">Preceptors awaiting review</p>
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-2">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Admin roster</h2>
              <span className="text-xs text-slate-400">Auto-synced</span>
            </div>
            {isLoading ? (
              <p className="mt-4 text-sm text-slate-500">Loading administrators...</p>
            ) : errorMessage ? (
              <p className="mt-4 text-sm text-rose-600">{errorMessage}</p>
            ) : (
              <div className="mt-6 space-y-2 text-sm text-slate-600">
                <div className="grid grid-cols-[2fr_2fr_1fr_1fr_1fr] gap-2 border-b border-slate-100 pb-2 text-xs uppercase tracking-[0.3em] text-slate-400">
                  <span>Name</span>
                  <span>Email</span>
                  <span>Role</span>
                  <span>Status</span>
                  <span>Action</span>
                </div>
                {admins.length === 0 && (
                  <p className="py-6 text-center text-sm text-slate-500">No admin accounts found.</p>
                )}
                {admins.map((admin) => {
                  const enabled = determineEnabledState(admin);
                  return (
                    <div
                      key={admin.userId}
                      className="grid grid-cols-[2fr_2fr_1fr_1fr_1fr] items-center gap-2 rounded-xl border border-slate-100 bg-surface-container-low px-3 py-2 text-sm text-slate-700"
                    >
                      <span className="text-base font-semibold text-slate-900">{admin.displayName}</span>
                      <span>{admin.email}</span>
                      <span>
                        {(() => {
                          const roleValue = admin.role as any;
                          return typeof roleValue === 'string'
                            ? roleValue
                            : roleValue?.roleName || roleValue?.roleDescription || 'Admin';
                        })()}
                      </span>
                      <span className={`font-semibold ${enabled ? 'text-emerald-600' : 'text-rose-500'}`}>
                        {enabled ? 'Enabled' : 'Disabled'}
                      </span>
                      <button
                        type="button"
                        onClick={() => handleToggle(admin)}
                        disabled={actionLoadingId === Number(admin.userId)}
                        className={`rounded-full px-3 py-1 text-xs font-semibold transition ${
                          enabled
                            ? 'border border-rose-500 text-rose-600 hover:bg-rose-50'
                            : 'border border-emerald-500 text-emerald-600 hover:bg-emerald-50'
                        } ${actionLoadingId === Number(admin.userId) ? 'opacity-60 cursor-wait' : ''}`}
                      >
                        {actionLoadingId === Number(admin.userId)
                          ? 'Processing...'
                          : enabled
                          ? 'Disable'
                          : 'Enable'}
                      </button>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Pending approvals</h2>
              <button
                type="button"
                onClick={loadManagers}
                className="text-xs font-semibold text-blue-600 transition hover:text-blue-500"
              >
                Refresh
              </button>
            </div>
            {pendingPreceptors.length === 0 ? (
              <p className="mt-4 text-sm text-slate-500">No pending preceptor applications at the moment.</p>
            ) : (
              <ul className="mt-4 space-y-4">
                {pendingPreceptors.slice(0, 3).map((preceptor) => (
                  <li key={preceptor.id} className="rounded-xl border border-slate-100 bg-slate-50/80 p-4">
                    <p className="text-sm font-semibold text-slate-900">{preceptor.name}</p>
                    <p className="text-xs text-slate-500">{preceptor.email}</p>
                    <div className="mt-2 flex flex-wrap items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.3em]">
                      <span className="rounded-full bg-slate-200 px-2 py-1 text-slate-600">{preceptor.status}</span>
                      <span className="rounded-full bg-slate-200 px-2 py-1 text-slate-600">
                        {preceptor.dateSubmitted}
                      </span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      </div>
    </AdminLayout>
  );
};

export default AdminManagement;
