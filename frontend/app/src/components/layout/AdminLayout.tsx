import React from 'react';
import { useNavigate } from 'react-router-dom';
import LayoutWrapper from './LayoutWrapper';

const navItems = [
  { to: '/admin/dashboard', label: 'Overview', icon: 'dashboard' },
  { to: '/admin/users', label: 'Users', icon: 'group' },
  { to: '/admin/preceptors/pending', label: 'Preceptors', icon: 'medical_services' },
  { to: '/admin/preceptors', label: 'Preceptor Ops', icon: 'support_agent' },
  { to: '/admin/management', label: 'Admin Management', icon: 'manage_accounts' },
  { to: '/admin/revenue', label: 'Revenue', icon: 'payments' },
  { to: '/admin/webhooks', label: 'Webhooks', icon: 'hub' },
  { to: '/admin/add-admin', label: 'Add Admin', icon: 'person_add' },
  { to: '/admin/students', label: 'Students', icon: 'school' },
  { to: '/admin/roles', label: 'Roles', icon: 'badge' },
  { to: '/admin/system', label: 'System', icon: 'bolt' },
  { to: '/admin/settings', label: 'Settings', icon: 'settings' },
];

const AdminLayout: React.FC<{ children: React.ReactNode; pageTitle?: string }> = ({
  children,
  pageTitle = 'Admin Workspace',
}) => {
  const navigate = useNavigate();

  return (
    <LayoutWrapper
      navItems={navItems}
      brandTitle="NPaxis"
      brandSubtitle="Clinical Admin"
      pageTitle={pageTitle}
      sidebarFooter={
        <div className="rounded-xl bg-slate-100 p-4">
          <p className="mb-2 text-xs font-semibold text-slate-500">SYSTEM STATUS</p>
          <div className="flex items-center gap-2">
            <span className="h-2 w-2 rounded-full bg-emerald-500" />
            <span className="text-sm font-medium text-slate-700">All systems operational</span>
          </div>
        </div>
      }
      rightHeaderContent={
        <div className="flex w-full items-center justify-end gap-2 sm:w-auto sm:gap-3">
          <button
            type="button"
            onClick={() => navigate('/admin/settings')}
            className="inline-flex items-center justify-center rounded-full border border-slate-200 p-2 text-slate-600 hover:border-slate-300 hover:text-blue-600"
            title="View admin notifications"
          >
            <span className="material-symbols-outlined">notifications</span>
          </button>
          <button
            type="button"
            onClick={() => navigate('/support')}
            className="inline-flex items-center justify-center rounded-full border border-slate-200 p-2 text-slate-600 hover:border-slate-300 hover:text-blue-600"
            title="Open support"
          >
            <span className="material-symbols-outlined">help_outline</span>
          </button>
        </div>
      }
    >
      {children}
    </LayoutWrapper>
  );
};

export default AdminLayout;
