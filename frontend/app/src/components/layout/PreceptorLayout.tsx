import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { authService } from '../../services/auth';

interface PreceptorLayoutProps {
  children: React.ReactNode;
  pageTitle?: string;
}

const navItems = [
  { to: '/preceptor/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { to: '/preceptor/profile', label: 'Profile', icon: 'person' },
  { to: '/preceptor/license-verification', label: 'License', icon: 'workspace_premium' },
  { to: '/preceptor/inquiries', label: 'Inquiries', icon: 'mark_email_unread' },
  { to: '/preceptor/subscription', label: 'Subscription', icon: 'workspace_premium' },
  { to: '/preceptor/billing', label: 'Billing', icon: 'payments' },
];

const PreceptorLayout: React.FC<PreceptorLayoutProps> = ({ children, pageTitle = 'Preceptor Dashboard' }) => {
  const navigate = useNavigate();
  const [isDrawerOpen, setDrawerOpen] = useState(false);
  const displayName = localStorage.getItem('displayName') || 'Preceptor';
  const role = localStorage.getItem('role') || 'PRECEPTOR';

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  const Sidebar = ({ mobile }: { mobile?: boolean }) => (
    <aside
      className={`flex flex-col border-r border-slate-200 bg-white shadow-sm ${
        mobile ? 'h-full w-64' : 'fixed inset-y-0 left-0 w-64'
      }`}
    >
      <div className="border-b border-slate-100 px-6 py-5">
        <h2 className="text-2xl font-black tracking-tight text-blue-800">NPaxis</h2>
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Preceptor Portal</p>
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${
                isActive ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`
            }
            onClick={() => mobile && setDrawerOpen(false)}
          >
            <span className="material-symbols-outlined text-base">{item.icon}</span>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-slate-100 p-4">
        <div className="rounded-xl bg-slate-50 p-3">
          <p className="text-xs font-bold uppercase tracking-wider text-slate-500">{role}</p>
          <p className="mt-1 truncate text-sm font-semibold text-slate-800">{displayName}</p>
        </div>
      </div>
    </aside>
  );

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <div className="hidden md:flex">
        <Sidebar />
      </div>

      {isDrawerOpen && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <div className="fixed inset-0 bg-slate-900/40" onClick={() => setDrawerOpen(false)} />
          <div className="relative z-10 flex">
            <Sidebar mobile />
          </div>
        </div>
      )}

      <div className="flex min-h-screen flex-col md:pl-64">
        <header className="sticky top-0 z-30 flex min-h-16 items-center justify-between gap-3 border-b border-slate-200 bg-white px-4 py-3 shadow-sm md:px-6">
          <div className="flex min-w-0 flex-1 items-center gap-3">
            <button
              type="button"
              onClick={() => setDrawerOpen(true)}
              className="inline-flex items-center justify-center rounded-full border border-slate-200 p-2 text-slate-600 hover:border-slate-300 md:hidden"
            >
              <span className="material-symbols-outlined">menu</span>
            </button>
            <h1 className="truncate text-base font-black tracking-tight text-slate-900 sm:text-lg">{pageTitle}</h1>
          </div>

          <div className="flex shrink-0 items-center gap-2 sm:gap-3">
            <div className="hidden max-w-[180px] truncate rounded-full bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-700 sm:block">
              {displayName}
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-full bg-slate-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-slate-800"
            >
              Logout
            </button>
          </div>
        </header>

        <main className="p-4 sm:p-6 md:p-8">{children}</main>
      </div>
    </div>
  );
};

export default PreceptorLayout;
