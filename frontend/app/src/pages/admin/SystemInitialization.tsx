import React, { useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { authService } from '../../services/auth';

const SystemInitialization: React.FC = () => {
  const [isInitializing, setIsInitializing] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const handleInit = async () => {
    setStatus(null);
    setIsInitializing(true);
    try {
      await authService.initializeSystem();
      setStatus({ type: 'success', message: 'System initialized with default roles and admin account.' });
    } catch (err: any) {
      setStatus({ type: 'error', message: err?.message || 'Initialization failed or already initialized.' });
    } finally {
      setIsInitializing(false);
    }
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">System Utility</p>
          <h1 className="text-3xl font-bold text-slate-900">System Initialization</h1>
          <p className="text-sm text-slate-500">
            Initialize roles and setup the default administration account (can be rerun on staging only).
          </p>
        </header>

        {status && (
          <div
            className={`rounded-lg px-4 py-3 text-sm font-medium ${
              status.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'
            }`}
          >
            {status.message}
          </div>
        )}

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <p className="text-sm text-slate-600">
            Click the button below to seed roles and create the default admin account defined by the backend.
            This is typically needed once during deployment.
          </p>
          <button
            onClick={handleInit}
            disabled={isInitializing}
            className="mt-6 flex items-center justify-center rounded-full bg-blue-600 px-6 py-3 text-sm font-semibold uppercase tracking-[0.4em] text-white transition hover:bg-blue-700 disabled:opacity-60"
          >
            {isInitializing ? 'Initializing...' : 'Initialize System'}
          </button>
        </div>
      </div>
    </AdminLayout>
  );
};

export default SystemInitialization;
