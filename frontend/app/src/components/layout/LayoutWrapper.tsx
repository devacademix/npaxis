import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { authService } from '../../services/auth';
import { useSession } from '../../context/SessionContext';
import GlobalHeader from './GlobalHeader';

interface NavItem {
  to: string;
  label: string;
  icon: string;
}

interface LayoutWrapperProps {
  children: React.ReactNode;
  navItems: NavItem[];
  brandTitle: string;
  brandSubtitle: string;
  pageTitle?: string;
  headerLeading?: React.ReactNode;
  topSearch?: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
  };
  sidebarFooter?: React.ReactNode;
  rightHeaderContent?: React.ReactNode;
}

const LayoutWrapper: React.FC<LayoutWrapperProps> = ({
  children,
  navItems,
  brandTitle,
  brandSubtitle,
  pageTitle,
  headerLeading,
  topSearch,
  sidebarFooter,
  rightHeaderContent,
}) => {
  const navigate = useNavigate();
  const { currentUser, role } = useSession();
  const [isDrawerOpen, setDrawerOpen] = useState(false);

  const displayName = currentUser?.displayName || localStorage.getItem('displayName') || 'User';
  const displayRole = role || 'USER';

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
        mobile ? 'h-full w-72 max-w-[85vw]' : 'fixed inset-y-0 left-0 w-64'
      }`}
    >
      <div className="border-b border-slate-100 px-5 py-5">
        <h2 className="text-2xl font-black tracking-tight text-blue-800">{brandTitle}</h2>
        <p className="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-500">{brandSubtitle}</p>
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to.endsWith('/dashboard')}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${
                isActive
                  ? 'bg-blue-50 text-blue-700 shadow-sm'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`
            }
            onClick={() => mobile && setDrawerOpen(false)}
          >
            <span className="material-symbols-outlined text-base">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-slate-100 p-4">
        {sidebarFooter ? (
          sidebarFooter
        ) : (
          <div className="rounded-xl bg-slate-50 p-3">
            <p className="text-xs font-bold uppercase tracking-wider text-slate-500">{displayRole}</p>
            <p className="mt-1 truncate text-sm font-semibold text-slate-800">{displayName}</p>
          </div>
        )}
      </div>
    </aside>
  );

  return (
    <div className="min-h-screen overflow-x-hidden bg-slate-100 text-slate-900">
      <div className="hidden lg:flex">
        <Sidebar />
      </div>

      {isDrawerOpen ? (
        <div className="fixed inset-0 z-50 flex lg:hidden">
          <div className="fixed inset-0 bg-slate-900/40" onClick={() => setDrawerOpen(false)} />
          <div className="relative z-10">
            <Sidebar mobile />
          </div>
        </div>
      ) : null}

      <div className="min-h-screen lg:pl-64">
        <GlobalHeader
          pageTitle={pageTitle}
          onMenuClick={() => setDrawerOpen(true)}
          headerLeading={headerLeading}
          topSearch={topSearch}
          rightContent={
            <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row sm:items-center">
              {rightHeaderContent}
              <div className="hidden max-w-[220px] truncate rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 md:block">
                {displayName}
              </div>
              <button
                type="button"
                onClick={handleLogout}
                className="w-full rounded-full bg-slate-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-slate-800 sm:w-auto"
              >
                Logout
              </button>
            </div>
          }
        />

        <main className="mx-auto w-full max-w-[1280px] px-4 py-4 sm:px-6 sm:py-6 lg:px-8 lg:py-8">
          {children}
        </main>
      </div>
    </div>
  );
};

export default LayoutWrapper;
