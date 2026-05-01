import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { roleService, type RoleDetail, type RoleSummary } from '../../services/role';

const RoleManagement: React.FC = () => {
  const [roles, setRoles] = useState<RoleSummary[]>([]);
  const [selectedRole, setSelectedRole] = useState<RoleDetail | null>(null);
  const [draft, setDraft] = useState({ roleName: '', description: '' });
  const [isLoading, setIsLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

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

  useEffect(() => {
    loadRoles();
  }, []);

  const handleRoleClick = async (role: RoleSummary) => {
    setDetailLoading(true);
    try {
      const detail = await roleService.getRoleDetail(role.roleId);
      setSelectedRole(detail);
      setDraft({
        roleName: detail.roleName,
        description: detail.description || '',
      });
    } catch (err: any) {
      setError(err?.message || 'Unable to load role detail.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      setIsSaving(true);
      setError(null);
      await roleService.createRole({
        roleName: draft.roleName.trim(),
        description: draft.description.trim(),
      });
      setSuccess('Role created successfully.');
      setDraft({ roleName: '', description: '' });
      await loadRoles();
    } catch (err: any) {
      setError(err?.message || 'Failed to create role.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleUpdate = async () => {
    if (!selectedRole) return;
    try {
      setIsSaving(true);
      setError(null);
      const updated = await roleService.updateRole(selectedRole.roleId, {
        description: draft.description.trim(),
      });
      setSelectedRole({ ...selectedRole, ...updated, description: draft.description.trim() });
      setSuccess('Role updated successfully.');
      await loadRoles();
    } catch (err: any) {
      setError(err?.message || 'Failed to update role.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!selectedRole) return;
    if (!window.confirm(`Delete role ${selectedRole.roleName}?`)) return;
    try {
      setIsSaving(true);
      setError(null);
      await roleService.deleteRole(selectedRole.roleId);
      setSelectedRole(null);
      setDraft({ roleName: '', description: '' });
      setSuccess('Role deleted successfully.');
      await loadRoles();
    } catch (err: any) {
      setError(err?.message || 'Failed to delete role.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Role Module</p>
          <h1 className="text-3xl font-bold text-slate-900">Role Definitions</h1>
          <p className="text-sm text-slate-500">Create, update, and retire application roles.</p>
        </header>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}

        <section className="grid gap-6 lg:grid-cols-[2fr,1fr]">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Roles</h2>
              <button type="button" onClick={loadRoles} className="text-xs font-semibold text-blue-600 hover:text-blue-500">
                Refresh
              </button>
            </div>
            {isLoading ? (
              <p className="mt-4 text-sm text-slate-500">Loading roles...</p>
            ) : (
              <div className="mt-4 space-y-3 text-sm text-slate-600">
                {roles.length === 0 && <p className="py-6 text-center text-sm text-slate-500">No roles available.</p>}
                {roles.map((role) => (
                  <button
                    key={role.roleId}
                    type="button"
                    onClick={() => handleRoleClick(role)}
                    className={`w-full rounded-2xl border px-4 py-3 text-left text-sm font-semibold transition ${selectedRole?.roleId === role.roleId ? 'border-blue-300 bg-blue-50' : 'border-slate-100 bg-slate-50/80 hover:border-slate-300'}`}
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
            <h2 className="text-lg font-semibold text-slate-900">{selectedRole ? 'Edit Role' : 'Create Role'}</h2>
            {detailLoading ? (
              <p className="mt-4 text-sm text-slate-500">Loading detail...</p>
            ) : (
              <div className="mt-4 space-y-4 text-sm text-slate-600">
                <label className="block">
                  <span className="mb-1 block text-xs font-semibold uppercase tracking-wider text-slate-500">Role Name</span>
                  <input
                    value={draft.roleName}
                    disabled={Boolean(selectedRole)}
                    onChange={(event) => setDraft((prev) => ({ ...prev, roleName: event.target.value }))}
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  />
                </label>
                <label className="block">
                  <span className="mb-1 block text-xs font-semibold uppercase tracking-wider text-slate-500">Description</span>
                  <textarea
                    rows={5}
                    value={draft.description}
                    onChange={(event) => setDraft((prev) => ({ ...prev, description: event.target.value }))}
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                  />
                </label>

                <div className="flex flex-wrap gap-2">
                  {!selectedRole ? (
                    <button type="button" onClick={handleCreate} disabled={isSaving || !draft.roleName.trim()} className="rounded-full bg-blue-700 px-4 py-2 text-xs font-bold uppercase tracking-wider text-white disabled:opacity-60">
                      {isSaving ? 'Saving...' : 'Create Role'}
                    </button>
                  ) : (
                    <>
                      <button type="button" onClick={handleUpdate} disabled={isSaving} className="rounded-full bg-blue-700 px-4 py-2 text-xs font-bold uppercase tracking-wider text-white disabled:opacity-60">
                        {isSaving ? 'Saving...' : 'Update Role'}
                      </button>
                      <button type="button" onClick={handleDelete} disabled={isSaving} className="rounded-full border border-red-200 px-4 py-2 text-xs font-bold uppercase tracking-wider text-red-600 disabled:opacity-60">
                        Delete Role
                      </button>
                    </>
                  )}
                </div>
              </div>
            )}
          </div>
        </section>
      </div>
    </AdminLayout>
  );
};

export default RoleManagement;
