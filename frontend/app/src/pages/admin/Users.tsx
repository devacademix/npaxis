import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import UserFilters from '../../components/admin/UserFilters';
import UserTable, { type UserTableRow } from '../../components/admin/UserTable';
import { authService } from '../../services/auth';
import { userService, type UserRecord } from '../../services/user';
import { adminService } from '../../services/admin';

const PAGE_SIZE = 8;
const normalizeRoleValue = (roleValue: string) => roleValue.toUpperCase().replace('ROLE_', '');

const Users: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';
  const navigate = useNavigate();

  const [users, setUsers] = useState<UserTableRow[]>([]);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const [viewUser, setViewUser] = useState<UserTableRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<UserTableRow | null>(null);
  const [isActionLoading, setIsActionLoading] = useState(false);
  const [showAddAdminModal, setShowAddAdminModal] = useState(false);
  const [addAdminForm, setAddAdminForm] = useState({ email: '', displayName: '', password: '' });

  const showToast = (type: 'success' | 'error', message: string) => {
    setToast({ type, message });
  };

  useEffect(() => {
    if (!isAdmin) return;

    const loadUsers = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await userService.getAllUsers();
        const normalized = (Array.isArray(response) ? response : []).map((user: UserRecord) => ({
          userId: user.userId,
          displayName: user.displayName,
          email: user.email,
          role: user.role,
          isEnabled: Boolean(user.isEnabled ?? user.enabled ?? user.accountEnabled ?? true),
          isDeleted: Boolean(user.isDeleted ?? user.deleted ?? false),
        }));
        setUsers(normalized);
      } catch (err: any) {
        setError(err?.message || 'Failed to load users.');
      } finally {
        setIsLoading(false);
      }
    };

    loadUsers();
  }, [isAdmin]);

  useEffect(() => {
    setCurrentPage(1);
  }, [search, roleFilter, statusFilter]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const filteredUsers = useMemo(() => {
    const keyword = search.trim().toLowerCase();

    return users.filter((user) => {
      const matchesSearch =
        !keyword ||
        user.displayName.toLowerCase().includes(keyword) ||
        user.email.toLowerCase().includes(keyword);

      const matchesRole =
        roleFilter === 'ALL' || normalizeRoleValue(user.role) === normalizeRoleValue(roleFilter);

      const derivedStatus = user.isDeleted || !user.isEnabled ? 'DISABLED' : 'ACTIVE';
      const matchesStatus = statusFilter === 'ALL' || derivedStatus === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, search, roleFilter, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filteredUsers.length / PAGE_SIZE));
  const paginatedUsers = useMemo(() => {
    const startIndex = (currentPage - 1) * PAGE_SIZE;
    return filteredUsers.slice(startIndex, startIndex + PAGE_SIZE);
  }, [filteredUsers, currentPage]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  const updateUserState = (userId: number, patch: Partial<UserTableRow>) => {
    setUsers((prev) => prev.map((item) => (item.userId === userId ? { ...item, ...patch } : item)));
  };

  const removeUserFromState = (userId: number) => {
    setUsers((prev) => prev.filter((item) => item.userId !== userId));
  };

  const handleToggleStatus = async (user: UserTableRow) => {
    const currentUserId = Number(localStorage.getItem('userId'));
    if (currentUserId === user.userId) {
      showToast('error', 'You cannot disable your own account.');
      return;
    }

    if (user.isDeleted) return;

    const nextEnabled = !user.isEnabled;
    try {
      setIsActionLoading(true);
      await userService.toggleAccountStatus(user.userId, nextEnabled);
      updateUserState(user.userId, { isEnabled: nextEnabled });
      showToast('success', `User ${nextEnabled ? 'enabled' : 'disabled'} successfully.`);
    } catch (err: any) {
      showToast('error', err?.message || 'Failed to update user status.');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleRestore = async (user: UserTableRow) => {
    try {
      setIsActionLoading(true);
      await userService.restoreUser(user.userId);
      updateUserState(user.userId, { isDeleted: false, isEnabled: true });
      showToast('success', 'User restored successfully.');
    } catch (err: any) {
      showToast('error', err?.message || 'Failed to restore user.');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleSoftDelete = async () => {
    if (!deleteTarget) return;
    const currentUserId = Number(localStorage.getItem('userId'));
    if (currentUserId === deleteTarget.userId) {
      showToast('error', 'You cannot delete your own account.');
      return;
    }

    try {
      setIsActionLoading(true);
      await userService.softDeleteUser(deleteTarget.userId);
      updateUserState(deleteTarget.userId, { isDeleted: true, isEnabled: false });
      setDeleteTarget(null);
      showToast('success', 'User soft deleted successfully.');
    } catch (err: any) {
      showToast('error', err?.message || 'Failed to soft delete user.');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleHardDelete = async () => {
    if (!deleteTarget) return;
    const currentUserId = Number(localStorage.getItem('userId'));
    if (currentUserId === deleteTarget.userId) {
      showToast('error', 'You cannot delete your own account.');
      return;
    }

    try {
      setIsActionLoading(true);
      await userService.hardDeleteUser(deleteTarget.userId);
      removeUserFromState(deleteTarget.userId);
      setDeleteTarget(null);
      showToast('success', 'User permanently deleted.');
    } catch (err: any) {
      showToast('error', err?.message || 'Failed to hard delete user.');
    } finally {
      setIsActionLoading(false);
    }
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
      showToast('success', 'Admin created successfully. Please refresh manually if needed.');
      setShowAddAdminModal(false);
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
      <div className="mb-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight text-on-surface font-headline">User Management</h1>
          <p className="mt-2 text-slate-500">Manage students, preceptors, and admins across the platform.</p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={() => setShowAddAdminModal(true)}
            className="w-fit rounded-full bg-blue-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-700"
          >
            + Add Admin
          </button>
          <button
            type="button"
            onClick={handleLogout}
            className="w-fit rounded-full bg-slate-900 px-5 py-2.5 text-sm font-bold text-white hover:bg-slate-800"
          >
            Logout
          </button>
        </div>
      </div>

      <div className="mb-5">
        <UserFilters
          search={search}
          onSearchChange={setSearch}
          roleFilter={roleFilter}
          onRoleFilterChange={setRoleFilter}
          statusFilter={statusFilter}
          onStatusFilterChange={setStatusFilter}
        />
      </div>

      {error ? (
        <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      ) : null}

      <UserTable
        users={paginatedUsers}
        isLoading={isLoading}
        onViewUser={setViewUser}
        onToggleStatus={handleToggleStatus}
        onOpenDeleteModal={setDeleteTarget}
        onRestoreUser={handleRestore}
      />

      {!isLoading && filteredUsers.length > 0 ? (
        <div className="mt-5 flex flex-col items-center justify-between gap-3 rounded-xl bg-white px-4 py-3 shadow-sm ring-1 ring-slate-200 sm:flex-row">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
            Page {currentPage} of {totalPages}
          </p>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
              disabled={currentPage === 1}
              className="rounded-md border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Previous
            </button>

            {Array.from({ length: totalPages }, (_, index) => index + 1).map((page) => (
              <button
                key={page}
                type="button"
                onClick={() => setCurrentPage(page)}
                className={`rounded-md px-3 py-1.5 text-xs font-bold ${
                  currentPage === page
                    ? 'bg-blue-600 text-white'
                    : 'border border-slate-200 text-slate-700 hover:bg-slate-100'
                }`}
              >
                {page}
              </button>
            ))}

            <button
              type="button"
              onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
              disabled={currentPage === totalPages}
              className="rounded-md border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      ) : null}

      {viewUser ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
            <h3 className="mb-5 text-xl font-bold text-slate-900">User Details</h3>
            <div className="space-y-3 text-sm">
              <p><span className="font-semibold text-slate-600">User ID:</span> {viewUser.userId}</p>
              <p><span className="font-semibold text-slate-600">Name:</span> {viewUser.displayName}</p>
              <p><span className="font-semibold text-slate-600">Email:</span> {viewUser.email}</p>
              <p><span className="font-semibold text-slate-600">Role:</span> {viewUser.role}</p>
              <p>
                <span className="font-semibold text-slate-600">Status:</span>{' '}
                {viewUser.isDeleted || !viewUser.isEnabled ? 'Disabled' : 'Active'}
              </p>
            </div>
            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setViewUser(null)}
                className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {deleteTarget ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
            <h3 className="mb-3 text-xl font-bold text-slate-900">Delete User</h3>
            <p className="text-sm text-slate-600">
              Choose how you want to delete <span className="font-semibold">{deleteTarget.displayName}</span>.
            </p>
            <div className="mt-6 flex flex-wrap justify-end gap-2">
              <button
                type="button"
                disabled={isActionLoading}
                onClick={() => setDeleteTarget(null)}
                className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700"
              >
                Cancel
              </button>
              <button
                type="button"
                disabled={isActionLoading}
                onClick={handleSoftDelete}
                className="rounded-lg bg-amber-500 px-4 py-2 text-sm font-semibold text-white hover:bg-amber-600"
              >
                Soft Delete
              </button>
              <button
                type="button"
                disabled={isActionLoading}
                onClick={handleHardDelete}
                className="rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700"
              >
                Hard Delete
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {toast ? (
        <div
          className={`fixed bottom-6 right-6 z-50 rounded-xl px-4 py-3 text-sm font-semibold shadow-lg ${
            toast.type === 'success'
              ? 'border border-emerald-200 bg-emerald-50 text-emerald-800'
              : 'border border-red-200 bg-red-50 text-red-800'
          }`}
        >
          {toast.message}
        </div>
      ) : null}

      {isActionLoading ? (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-slate-900/20">
          <div className="flex items-center gap-3 rounded-full bg-white px-4 py-2 text-sm font-semibold text-slate-700 shadow-md">
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
            Processing...
          </div>
        </div>
      ) : null}

      {showAddAdminModal ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
            <h3 className="mb-3 text-xl font-bold text-slate-900">Add New Admin</h3>
            <p className="mb-5 text-sm text-slate-600">Register a new administrator to the platform.</p>
            <form onSubmit={handleAddAdminSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-slate-700">Display Name</label>
                <input
                  type="text"
                  required
                  value={addAdminForm.displayName}
                  onChange={(e) => setAddAdminForm({ ...addAdminForm, displayName: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-slate-200 px-4 py-2 text-sm outline-none transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-700">Email Address</label>
                <input
                  type="email"
                  required
                  value={addAdminForm.email}
                  onChange={(e) => setAddAdminForm({ ...addAdminForm, email: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-slate-200 px-4 py-2 text-sm outline-none transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-700">Account Password</label>
                <input
                  type="password"
                  required
                  value={addAdminForm.password}
                  onChange={(e) => setAddAdminForm({ ...addAdminForm, password: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-slate-200 px-4 py-2 text-sm outline-none transition-all placeholder:text-slate-400 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
                />
              </div>
              <div className="mt-6 flex flex-wrap justify-end gap-2 pt-2">
                <button
                  type="button"
                  disabled={isActionLoading}
                  onClick={() => setShowAddAdminModal(false)}
                  className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isActionLoading}
                  className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
                >
                  Create Admin
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </AdminLayout>
  );
};

export default Users;
