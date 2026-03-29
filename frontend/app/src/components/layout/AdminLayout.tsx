import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';

const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();

  return (
    <div className="bg-surface text-on-surface min-h-screen">
      <aside className="fixed left-0 top-0 z-40 flex flex-col h-screen w-64 border-r-0 bg-slate-50 font-headline antialiased tracking-tight shadow-sm">
        <div className="p-6">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-[#003d9b] to-[#0052cc] flex items-center justify-center text-white">
              <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>medical_services</span>
            </div>
            <div>
              <h2 className="text-2xl font-bold tracking-tighter text-blue-800">NPaxis</h2>
              <p className="text-[10px] uppercase tracking-widest text-slate-500 font-semibold">Clinical Admin</p>
            </div>
          </div>
        </div>
        
        <nav className="flex-1 mt-4 px-2 space-y-1">
          <NavLink to="/admin" end className={({isActive}) => `flex items-center gap-3 px-4 py-3 font-semibold transition-all ${isActive ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-80' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'}`}>
            <span className="material-symbols-outlined">dashboard</span>
            <span>Overview</span>
          </NavLink>
          <NavLink to="/admin/users" className={({isActive}) => `flex items-center gap-3 px-4 py-3 font-semibold transition-all ${isActive ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-80' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'}`}>
            <span className="material-symbols-outlined">group</span>
            <span>Users</span>
          </NavLink>
          <NavLink to="/admin/preceptors/pending" className={({isActive}) => `flex items-center gap-3 px-4 py-3 font-semibold transition-all ${isActive ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-80' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'}`}>
            <span className="material-symbols-outlined">medical_services</span>
            <span>Preceptors</span>
          </NavLink>
          <NavLink to="/admin/revenue" className={({isActive}) => `flex items-center gap-3 px-4 py-3 font-semibold transition-all ${isActive ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-80' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'}`}>
            <span className="material-symbols-outlined">payments</span>
            <span>Revenue</span>
          </NavLink>
          <NavLink to="/admin/add-admin" className={({isActive}) => `flex items-center gap-3 px-4 py-3 font-semibold transition-all ${isActive ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-80' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'}`}>
            <span className="material-symbols-outlined">person_add</span>
            <span>Add Admin</span>
          </NavLink>
          <NavLink to="/admin/settings" className={({isActive}) => `flex items-center gap-3 px-4 py-3 font-semibold transition-all ${isActive ? 'text-blue-700 border-r-4 border-blue-700 bg-blue-50/50 opacity-80' : 'text-slate-600 hover:text-blue-600 hover:bg-slate-200/50'}`}>
            <span className="material-symbols-outlined">settings</span>
            <span>Settings</span>
          </NavLink>
        </nav>
        
        <div className="p-4 mt-auto">
          <div className="bg-slate-100 rounded-xl p-4">
            <p className="text-xs font-semibold text-slate-500 mb-2">SYSTEM STATUS</p>
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
              <span className="text-sm font-medium">All systems operational</span>
            </div>
          </div>
        </div>
      </aside>

      <header className="sticky top-0 z-30 flex items-center justify-between px-6 ml-64 w-[calc(100%-16rem)] h-16 bg-white/85 backdrop-blur-md border-b border-slate-100 shadow-sm font-body text-sm font-medium">
        <div className="flex items-center gap-4 flex-1">
          <div className="relative w-full max-w-md group">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
            <input className="w-full pl-10 pr-4 py-2 bg-surface-container-low border-none rounded-full focus:ring-2 focus:ring-blue-500/20 focus:bg-surface-container-lowest transition-all" placeholder="Search records, users, or transactions..." type="text"/>
          </div>
        </div>
        
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-4 text-slate-500">
            <button
              type="button"
              onClick={() => navigate('/admin/settings')}
              className="hover:text-blue-600 transition-all relative"
              title="View admin notifications"
            >
              <span className="material-symbols-outlined">notifications</span>
              <span className="absolute top-0 right-0 w-2 h-2 bg-error rounded-full border-2 border-white"></span>
            </button>
            <button
              type="button"
              onClick={() => navigate('/support')}
              className="hover:text-blue-600 transition-all"
              title="Open support"
            >
              <span className="material-symbols-outlined">help_outline</span>
            </button>
          </div>
          <div className="h-8 w-[1px] bg-slate-100 mx-2"></div>
          <div className="flex items-center gap-3 cursor-pointer group">
            <div className="text-right">
              <p className="text-xs font-bold text-on-surface">System Admin</p>
              <p className="text-[10px] text-slate-500 font-medium">Administrator</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-slate-200 border-2 border-slate-100 group-hover:border-blue-200 transition-all flex items-center justify-center text-slate-500">
              <span className="material-symbols-outlined text-lg">admin_panel_settings</span>
            </div>
          </div>
        </div>
      </header>
      
      <main className="ml-64 p-8 min-h-[calc(100vh-4rem)]">
        {children}
      </main>
    </div>
  );
};

export default AdminLayout;
