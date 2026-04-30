import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import {
  roleService,
  type RoleCreatePayload,
  type RoleDetail,
  type RoleSummary,
  type RoleUpdatePayload,
} from '../../services/role';

const RoleManagement: React.FC = () => {
  const [roles, setRoles] = useState<RoleSummary[]>([]);
  const [selectedRole, setSelectedRole] = useState<RoleDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [roleForm, setRoleForm] = useState({ roleName: '', description: '' });
  const [isEditing, setIsEditing] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  useEffect(() => {
    loadRoles();
  }, []);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const loadRoles = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const fetchedRoles = await roleService.getAllRoles();
      setRoles(fetchedRoles);
    } catch (err: any) {
      setError(err?.message || 'Failed to fetch roles.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRoleClick = async (role: RoleSummary) => {
    setDetailLoading(true);
    try {
      const detail = await roleService.getRoleDetail(role.roleId);
      setSelectedRole(detail);
      setRoleForm({
        roleName: detail.roleName ?? '',
        description: detail.description ?? '',
      });
      setIsEditing(true);
    } catch (err: any) {
      setError(err?.message || 'Unable to load role detail.');
    } finally {
      setDetailLoading(false);
    }
  };

  const resetForm = () => {
    setRoleForm({ roleName: '', description: '' });
    setIsEditing(false);
    setSelectedRole(null);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!roleForm.roleName.trim()) {
      setToast({ type: 'error', message: 'Role name is required.' });
      return;
    }

    try {
      setIsSubmitting(true);
      if (isEditing && selectedRole) {
        const payload: RoleUpdatePayload = {
          roleName: roleForm.roleName.trim(),
          description: roleForm.description.trim(),
        };
        const updated = await roleService.updateRole(selectedRole.roleId, payload);
        setToast({ type: 'success', message: 'Role updated successfully.' });
        await loadRoles();
        setSelectedRole(updated);
      } else {
        const payload: RoleCreatePayload = {
          roleName: roleForm.roleName.trim(),
          description: roleForm.description.trim(),
        };
        const created = await roleService.createRole(payload);
        setToast({ type: 'success', message: 'Role created successfully.' });
        await loadRoles();
        setSelectedRole(created);
        setIsEditing(true);
      }
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Unable to save role.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedRole) return;
    if (!window.confirm(`Delete role "${selectedRole.roleName}"?`)) return;

    try {
      setIsSubmitting(true);
      await roleService.deleteRole(selectedRole.roleId);
      setToast({ type: 'success', message: 'Role deleted successfully.' });
      resetForm();
      await loadRoles();
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Unable to delete role.' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Role Module</p>
          <h1 className="text-3xl font-bold text-slate-900">Role Definitions</h1>
          <p className="text-sm text-slate-500">Review available roles and inspect detailed metadata.</p>
        </header>

        {toast ? (
          <div className={`rounded-lg px-4 py-3 text-sm font-medium ${toast.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'}`}>
            {toast.message}
          </div>
        ) : null}

        <section className="grid gap-6 lg:grid-cols-[2fr,1fr]">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Roles</h2>
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={resetForm}
                  className="text-xs font-semibold text-slate-500 hover:text-slate-700"
                >
                  New Role
                </button>
                <button
                  type="button"
                  onClick={loadRoles}
                  className="text-xs font-semibold text-blue-600 hover:text-blue-500"
                >
                  Refresh
                </button>
              </div>
            </div>
            {isLoading ? (
              <p className="mt-4 text-sm text-slate-500">Loading roles...</p>
            ) : error ? (
              <p className="mt-4 text-sm text-rose-600">{error}</p>
            ) : (
              <div className="mt-4 space-y-3 text-sm text-slate-600">
                {roles.length === 0 && (
                  <p className="py-6 text-center text-sm text-slate-500">No roles available.</p>
                )}
                {roles.map((role) => (
                  <button
                    key={role.roleId}
                    type="button"
                    onClick={() => handleRoleClick(role)}
                    className="w-full rounded-2xl border border-slate-100 bg-slate-50/80 px-4 py-3 text-left text-sm font-semibold text-slate-900 transition hover:border-slate-300"
                  >
                    <div className="flex items-center justify-between gap-4">
                      <span>{role.roleName}</span>
                      <span className="text-xs text-slate-400">ID: {role.roleId}</span>
                    </div>
                    <p className="mt-1 text-xs font-normal text-slate-500">{role.description || 'No description'}</p>
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-slate-900">{isEditing ? 'Edit role' : 'Create role'}</h2>
            <form onSubmit={handleSubmit} className="mt-4 space-y-4">
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Role Name</label>
                <input
                  value={roleForm.roleName}
                  onChange={(event) => setRoleForm((prev) => ({ ...prev, roleName: event.target.value }))}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                  placeholder="e.g. ROLE_MODERATOR"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Description</label>
                <textarea
                  value={roleForm.description}
                  onChange={(event) => setRoleForm((prev) => ({ ...prev, description: event.target.value }))}
                  className="min-h-24 w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                  placeholder="Role summary"
                />
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="rounded-full bg-blue-600 px-4 py-2 text-xs font-semibold uppercase tracking-[0.3em] text-white hover:bg-blue-700 disabled:opacity-60"
                >
                  {isSubmitting ? 'Saving...' : isEditing ? 'Update Role' : 'Create Role'}
                </button>
                {isEditing ? (
                  <button
                    type="button"
                    onClick={handleDelete}
                    disabled={isSubmitting}
                    className="rounded-full border border-rose-200 px-4 py-2 text-xs font-semibold uppercase tracking-[0.3em] text-rose-600 hover:bg-rose-50 disabled:opacity-60"
                  >
                    Delete Role
                  </button>
                ) : null}
              </div>
            </form>
            {detailLoading ? (
              <p className="mt-4 text-sm text-slate-500">Loading detail...</p>
            ) : selectedRole ? (
              <div className="mt-4 space-y-3 text-sm text-slate-600">
                <p>
                  <span className="font-semibold">Name:</span> {selectedRole.roleName}
                </p>
                <p>
                  <span className="font-semibold">ID:</span> {selectedRole.roleId}
                </p>
                <p className="text-sm text-slate-500">{selectedRole.description}</p>
                {selectedRole.permissions && selectedRole.permissions.length > 0 && (
                  <div>
                    <h3 className="text-xs uppercase tracking-[0.3em] text-slate-400">Permissions</h3>
                    <ul className="mt-2 space-y-1 text-xs text-slate-500">
                      {selectedRole.permissions.map((perm) => (
                        <li key={perm}>{perm}</li>
                      ))}
                    </ul>
                  </div>
                )}
                {selectedRole.createdAt && (
                  <p className="text-[11px] text-slate-400">
                    Created: {new Date(selectedRole.createdAt).toLocaleString()}
                  </p>
                )}
                {selectedRole.updatedAt && (
                  <p className="text-[11px] text-slate-400">
                    Updated: {new Date(selectedRole.updatedAt).toLocaleString()}
                  </p>
                )}
              </div>
            ) : (
              <p className="mt-4 text-sm text-slate-500">Select a role to view metadata.</p>
            )}
          </div>
        </section>
      </div>
    </AdminLayout>
  );
};

export default RoleManagement;
