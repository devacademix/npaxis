import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';

const navItems = [
  { to: '/admin', label: 'Overview', icon: 'dashboard' },
  { to: '/admin/users', label: 'Users', icon: 'group' },
  { to: '/admin/preceptors/pending', label: 'Preceptors', icon: 'medical_services' },
  { to: '/admin/preceptors', label: 'Preceptor Ops', icon: 'support_agent' },
  { to: '/admin/management', label: 'Admin Management', icon: 'manage_accounts' },
  { to: '/admin/revenue', label: 'Revenue', icon: 'payments' },
  { to: '/admin/add-admin', label: 'Add Admin', icon: 'person_add' },
  { to: '/admin/students', label: 'Students', icon: 'school' },
  { to: '/admin/roles', label: 'Roles', icon: 'badge' },
  { to: '/admin/system', label: 'System', icon: 'bolt' },
  { to: '/admin/settings', label: 'Settings', icon: 'settings' },
];

const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const [isDrawerOpen, setDrawerOpen] = useState(false);
  const handleProfileClick = () => navigate('/admin/settings');

  const Sidebar = ({ mobile }: { mobile?: boolean }) => (
    <aside
      className={`flex flex-col bg-slate-50 text-slate-700 border border-slate-100 shadow-sm ${
        mobile ? 'h-full w-64' : 'fixed inset-y-0 left-0 w-64'
      }`}
    >
      <div className="p-6 border-b border-slate-100">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#003d9b] to-[#0052cc] flex items-center justify-center text-white">
            <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>
              medical_services
            </span>
          </div>
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-blue-800">NPaxis</h2>
            <p className="text-[10px] uppercase tracking-widest text-slate-500 font-semibold">Clinical Admin</p>
          </div>
        </div>
      </div>
      <nav className="flex-1 overflow-y-auto px-2 py-4 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/admin'}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-4 py-3 font-semibold transition-all ${
                isActive
                  ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-90 shadow-inner'
                  : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'
              }`
            }
            onClick={() => mobile && setDrawerOpen(false)}
          >
            <span className="material-symbols-outlined">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t border-slate-100">
        <div className="bg-slate-100 rounded-xl p-4">
          <p className="text-xs font-semibold text-slate-500 mb-2">SYSTEM STATUS</p>
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
            <span className="text-sm font-medium text-slate-700">All systems operational</span>
          </div>
        </div>
      </div>
    </aside>
  );

  return (
    <div className="min-h-screen bg-surface text-on-surface">
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

      <div className="flex flex-col min-h-screen md:pl-64">
        <header className="sticky top-0 z-30 flex items-center justify-between gap-4 bg-white/90 px-4 py-3 backdrop-blur border-b border-slate-100 shadow-sm md:px-6">
          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={() => setDrawerOpen(true)}
              className="inline-flex items-center justify-center rounded-full border border-slate-200 p-2 text-slate-600 hover:border-slate-300 md:hidden"
            >
              <span className="material-symbols-outlined">menu</span>
            </button>
            <div className="relative hidden w-full max-w-md lg:flex">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
                search
              </span>
              <input
                className="w-full pl-10 pr-4 py-2 rounded-full border border-transparent bg-surface-container-low focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition"
                placeholder="Search records, users, or transactions..."
                type="text"
              />
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex items-center gap-4 text-slate-500">
              <button
                type="button"
                onClick={() => navigate('/admin/settings')}
                className="relative hover:text-blue-600 transition"
                title="View admin notifications"
              >
                <span className="material-symbols-outlined">notifications</span>
                <span className="absolute top-0 right-0 w-2 h-2 bg-error rounded-full border-2 border-white"></span>
              </button>
              <button
                type="button"
                onClick={() => navigate('/support')}
                className="hover:text-blue-600 transition"
                title="Open support"
              >
                <span className="material-symbols-outlined">help_outline</span>
              </button>
            </div>
            <div className="h-8 w-px bg-slate-100" />
            <button
              type="button"
              onClick={handleProfileClick}
              className="flex items-center gap-3 group focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
            >
              <div className="text-right">
                <p className="text-xs font-bold text-on-surface">System Admin</p>
                <p className="text-[10px] text-slate-500 font-medium">Administrator</p>
              </div>
              <div className="w-10 h-10 rounded-full border-2 border-slate-100 bg-slate-200 flex items-center justify-center text-slate-500 transition group-hover:border-blue-200">
                <span className="material-symbols-outlined text-lg">admin_panel_settings</span>
              </div>
            </button>
          </div>
        </header>

        <main className="flex-1 w-full max-w-screen-xl px-4 py-6 sm:px-6 md:px-8 lg:px-10 mx-auto">{children}</main>
      </div>
    </div>
  );
};

export default AdminLayout;
