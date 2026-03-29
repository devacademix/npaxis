import React from 'react';
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
  { to: '/preceptor/subscription', label: 'Subscription', icon: 'workspace_premium' },
  { to: '/preceptor/billing', label: 'Billing', icon: 'payments' },
];

const PreceptorLayout: React.FC<PreceptorLayoutProps> = ({ children, pageTitle = 'Preceptor Dashboard' }) => {
  const navigate = useNavigate();
  const displayName = localStorage.getItem('displayName') || 'Preceptor';
  const role = localStorage.getItem('role') || 'PRECEPTOR';

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <aside className="fixed left-0 top-0 z-40 flex h-screen w-64 flex-col border-r border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-100 px-6 py-5">
          <h2 className="text-2xl font-black tracking-tight text-blue-800">NPaxis</h2>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Preceptor Portal</p>
        </div>

        <nav className="flex-1 space-y-1 px-3 py-4">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${isActive ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                }`
              }
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

      <header className="sticky top-0 z-30 ml-64 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6 shadow-sm">
        <h1 className="text-lg font-black tracking-tight text-slate-900">{pageTitle}</h1>
        <div className="flex items-center gap-3">
          <div className="rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">{displayName}</div>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-full bg-slate-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-slate-800"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="ml-64 p-6 md:p-8">{children}</main>
    </div>
  );
};

export default PreceptorLayout;
