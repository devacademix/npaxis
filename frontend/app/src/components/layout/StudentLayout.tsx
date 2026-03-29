import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { authService } from '../../services/auth';

interface StudentLayoutProps {
  children: React.ReactNode;
  pageTitle?: string;
  headerLeading?: React.ReactNode;
  topSearch?: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
  };
}

const navItems = [
  { label: 'Dashboard', icon: 'dashboard', to: '/student' },
  { label: 'Browse', icon: 'travel_explore', to: '/student/browse' },
  { label: 'Saved', icon: 'favorite', to: '/student/saved' },
  { label: 'Profile', icon: 'person', to: '/student/profile' },
];

const StudentLayout: React.FC<StudentLayoutProps> = ({ children, pageTitle = 'Student Dashboard', headerLeading, topSearch }) => {
  const navigate = useNavigate();
  const displayName = localStorage.getItem('displayName') || 'Student';

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <aside className="fixed left-0 top-0 z-40 hidden h-screen w-64 flex-col border-r border-slate-200 bg-white shadow-sm lg:flex">
        <div className="border-b border-slate-100 px-6 py-5">
          <h2 className="text-2xl font-black tracking-tight text-blue-800">NPaxis</h2>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Student Portal</p>
        </div>

        <nav className="flex-1 space-y-1 px-3 py-4">
          {navItems.map((item) => (
            <NavLink
              key={item.label}
              to={item.to}
              end={item.to === '/student'}
              className={({ isActive }) =>
                `flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${
                  isActive
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
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
            <p className="text-xs font-bold uppercase tracking-wider text-slate-500">ROLE_STUDENT</p>
            <p className="mt-1 truncate text-sm font-semibold text-slate-800">{displayName}</p>
          </div>
        </div>
      </aside>

      <header className="sticky top-0 z-30 flex h-16 items-center justify-between gap-4 border-b border-slate-200 bg-white px-4 shadow-sm lg:ml-64 lg:px-6">
        <div className="flex min-w-0 flex-1 items-center gap-4">
          {headerLeading ? <div className="shrink-0">{headerLeading}</div> : null}
          <h1 className="shrink-0 text-lg font-black tracking-tight text-slate-900">{pageTitle}</h1>
          {topSearch ? (
            <div className="hidden max-w-xl flex-1 items-center md:flex">
              <div className="relative w-full">
                <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
                <input
                  type="text"
                  value={topSearch.value}
                  onChange={(event) => topSearch.onChange(event.target.value)}
                  placeholder={topSearch.placeholder || 'Search...'}
                  className="w-full rounded-full border border-slate-200 bg-slate-50 py-2 pl-10 pr-4 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
                />
              </div>
            </div>
          ) : null}
        </div>
        <div className="flex shrink-0 items-center gap-3">
          <div className="hidden rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 sm:block">{displayName}</div>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-full bg-slate-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-slate-800"
          >
            Logout
          </button>
        </div>
      </header>

      <main className="p-4 sm:p-6 lg:ml-64 lg:p-8">{children}</main>
    </div>
  );
};

export default StudentLayout;
