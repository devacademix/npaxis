import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { roleService, type RoleDetail, type RoleSummary } from '../../services/role';

const RoleManagement: React.FC = () => {
  const [roles, setRoles] = useState<RoleSummary[]>([]);
  const [selectedRole, setSelectedRole] = useState<RoleDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  useEffect(() => {
    loadRoles();
  }, []);

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
    } catch (err: any) {
      setError(err?.message || 'Unable to load role detail.');
    } finally {
      setDetailLoading(false);
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

        <section className="grid gap-6 lg:grid-cols-[2fr,1fr]">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Roles</h2>
              <button
                type="button"
                onClick={loadRoles}
                className="text-xs font-semibold text-blue-600 hover:text-blue-500"
              >
                Refresh
              </button>
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
            <h2 className="text-lg font-semibold text-slate-900">Role details</h2>
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
