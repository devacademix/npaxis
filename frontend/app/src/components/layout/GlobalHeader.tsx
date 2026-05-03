import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useSession } from '../../context/SessionContext';

interface GlobalHeaderProps {
  pageTitle?: string;
  onMenuClick?: () => void;
  headerLeading?: React.ReactNode;
  topSearch?: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
  };
  rightContent?: React.ReactNode;
}

const GlobalHeader: React.FC<GlobalHeaderProps> = ({
  pageTitle = 'Workspace',
  onMenuClick,
  headerLeading,
  topSearch,
  rightContent,
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { dashboardPath } = useSession();

  const handleBack = () => {
    if (window.history.length > 1) {
      navigate(-1);
      return;
    }
    navigate(dashboardPath || '/login');
  };

  const handleDashboard = () => {
    navigate(dashboardPath || '/login');
  };

  const isOnDashboard = location.pathname === dashboardPath;

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex w-full max-w-[1280px] flex-col gap-3 px-4 py-3 sm:px-6 lg:px-8">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex min-w-0 flex-1 items-center gap-3">
            {onMenuClick ? (
              <button
                type="button"
                onClick={onMenuClick}
                className="inline-flex items-center justify-center rounded-full border border-slate-200 p-2 text-slate-600 hover:border-slate-300 lg:hidden"
              >
                <span className="material-symbols-outlined">menu</span>
              </button>
            ) : null}
            {headerLeading ? <div className="shrink-0">{headerLeading}</div> : null}
            <h1 className="min-w-0 truncate text-base font-black tracking-tight text-slate-900 sm:text-lg lg:text-xl">
              {pageTitle}
            </h1>
          </div>

          <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row sm:items-center">
            <button
              type="button"
              onClick={handleBack}
              className="inline-flex w-full items-center justify-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 sm:w-auto"
            >
              <span className="material-symbols-outlined text-base">arrow_back</span>
              Back
            </button>
            {!isOnDashboard ? (
              <button
                type="button"
                onClick={handleDashboard}
                className="inline-flex w-full items-center justify-center gap-2 rounded-full bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 sm:w-auto"
              >
                <span className="material-symbols-outlined text-base">dashboard</span>
                Dashboard
              </button>
            ) : null}
            {rightContent}
          </div>
        </div>

        {topSearch ? (
          <div className="w-full">
            <div className="relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
                search
              </span>
              <input
                type="text"
                value={topSearch.value}
                onChange={(event) => topSearch.onChange(event.target.value)}
                placeholder={topSearch.placeholder || 'Search...'}
                className="w-full rounded-full border border-slate-200 bg-slate-50 py-2.5 pl-10 pr-4 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
              />
            </div>
          </div>
        ) : null}
      </div>
    </header>
  );
};

export default GlobalHeader;
