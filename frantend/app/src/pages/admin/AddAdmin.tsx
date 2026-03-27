import React, { useState } from 'react';
import { Navigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import { adminService } from '../../services/admin';

const AddAdmin: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';

  const [addAdminForm, setAddAdminForm] = useState({ email: '', displayName: '', password: '' });
  const [isActionLoading, setIsActionLoading] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const showToast = (type: 'success' | 'error', message: string) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 3000);
  };

  const handleAddAdminSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!addAdminForm.email || !addAdminForm.displayName || !addAdminForm.password) {
      showToast('error', 'All fields are required.');
      return;
    }
    try {
      setIsActionLoading(true);
      await adminService.createAdmin(addAdminForm);
      showToast('success', 'New Administrator created successfully.');
      setAddAdminForm({ email: '', displayName: '', password: '' });
    } catch (err: any) {
      showToast('error', err?.message || 'Failed to create admin.');
    } finally {
      setIsActionLoading(false);
    }
  };

  if (!isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AdminLayout>
      <div className="mb-8">
        <h1 className="text-4xl font-extrabold tracking-tight text-on-surface font-headline">Add Admin</h1>
        <p className="mt-2 text-slate-500">Register a new administrator to manage the NPaxis platform.</p>
      </div>

      <div className="max-w-2xl rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <form onSubmit={handleAddAdminSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-semibold text-slate-700">Display Name</label>
            <input
              type="text"
              required
              value={addAdminForm.displayName}
              onChange={(e) => setAddAdminForm({ ...addAdminForm, displayName: e.target.value })}
              className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
              placeholder="e.g. John Doe"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-slate-700">Email Address</label>
            <input
              type="email"
              required
              value={addAdminForm.email}
              onChange={(e) => setAddAdminForm({ ...addAdminForm, email: e.target.value })}
              className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
              placeholder="admin@npaxis.com"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold text-slate-700">Password</label>
            <input
              type="password"
              required
              value={addAdminForm.password}
              onChange={(e) => setAddAdminForm({ ...addAdminForm, password: e.target.value })}
              className="mt-2 w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
              placeholder="Enter a secure password"
            />
          </div>
          
          <div className="pt-4">
            <button
              type="submit"
              disabled={isActionLoading}
              className="w-full rounded-xl bg-blue-600 px-4 py-3 text-sm font-bold text-white hover:bg-blue-700 transition-colors disabled:opacity-70 flex justify-center items-center gap-2"
            >
              {isActionLoading && <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white"></span>}
              Create Administrator
            </button>
          </div>
        </form>
      </div>

      {toast && (
        <div
          className={`fixed bottom-6 right-6 z-50 rounded-xl px-4 py-3 text-sm font-semibold shadow-lg ${
            toast.type === 'success'
              ? 'border border-emerald-200 bg-emerald-50 text-emerald-800'
              : 'border border-red-200 bg-red-50 text-red-800'
          }`}
        >
          {toast.message}
        </div>
      )}
    </AdminLayout>
  );
};

export default AddAdmin;
