import React from 'react';

export interface UserTableRow {
  userId: number;
  displayName: string;
  email: string;
  role: string;
  isEnabled: boolean;
  isDeleted: boolean;
}

interface UserTableProps {
  users: UserTableRow[];
  isLoading: boolean;
  onViewUser: (user: UserTableRow) => void;
  onToggleStatus: (user: UserTableRow) => void;
  onOpenDeleteModal: (user: UserTableRow) => void;
  onRestoreUser: (user: UserTableRow) => void;
}

const UserTable: React.FC<UserTableProps> = ({
  users,
  isLoading,
  onViewUser,
  onToggleStatus,
  onOpenDeleteModal,
  onRestoreUser,
}) => {
  if (isLoading) {
    return (
      <div className="overflow-hidden rounded-xl bg-white shadow-sm ring-1 ring-slate-200">
        <div className="animate-pulse space-y-4 p-6">
          {[...Array(8)].map((_, index) => (
            <div key={index} className="h-10 rounded-md bg-slate-200/70" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl bg-white shadow-sm ring-1 ring-slate-200">
      <div className="overflow-x-auto">
        <table className="w-full min-w-[980px] text-left">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-slate-500">User ID</th>
              <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-slate-500">Name</th>
              <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-slate-500">Email</th>
              <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-slate-500">Role</th>
              <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-slate-500">Status</th>
              <th className="px-6 py-4 text-[11px] font-bold uppercase tracking-wider text-slate-500 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-6 py-10 text-center text-sm text-slate-500">
                  No users found
                </td>
              </tr>
            ) : (
              users.map((user) => {
                const normalizedRole = user.role.replace('ROLE_', '');
                const statusText = user.isDeleted || !user.isEnabled ? 'Disabled' : 'Active';
                const statusClass =
                  statusText === 'Active'
                    ? 'bg-emerald-100 text-emerald-700'
                    : 'bg-amber-100 text-amber-700';

                return (
                  <tr key={user.userId} className="transition-colors hover:bg-slate-50">
                    <td className="px-6 py-4 text-sm font-semibold text-slate-700">{user.userId}</td>
                    <td className="px-6 py-4 text-sm font-semibold text-slate-900">{user.displayName}</td>
                    <td className="px-6 py-4 text-sm text-slate-600">{user.email}</td>
                    <td className="px-6 py-4">
                      <span className="inline-flex rounded-full bg-blue-50 px-2.5 py-1 text-xs font-bold text-blue-700">
                        {normalizedRole}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${statusClass}`}>
                        {statusText}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => onViewUser(user)}
                          className="rounded-md border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                        >
                          View
                        </button>

                        {user.isDeleted ? (
                          <button
                            type="button"
                            onClick={() => onRestoreUser(user)}
                            className="rounded-md bg-emerald-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-700"
                          >
                            Restore
                          </button>
                        ) : (
                          <>
                            <label className="inline-flex cursor-pointer items-center">
                              <input
                                type="checkbox"
                                checked={user.isEnabled}
                                onChange={() => onToggleStatus(user)}
                                className="peer sr-only"
                              />
                              <span className="peer relative h-6 w-11 rounded-full bg-slate-200 transition-colors peer-checked:bg-blue-600 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-blue-500/20">
                                <span className="absolute left-0.5 top-0.5 h-5 w-5 rounded-full bg-white transition-transform peer-checked:translate-x-5" />
                              </span>
                            </label>
                            <button
                              type="button"
                              onClick={() => onOpenDeleteModal(user)}
                              className="rounded-md bg-red-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-700"
                            >
                              Delete
                            </button>
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default UserTable;
