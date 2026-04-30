import React, { useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { authService } from '../../services/auth';
import { adminService } from '../../services/admin';

const SystemInitialization: React.FC = () => {
  const [isInitializing, setIsInitializing] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [health, setHealth] = useState<any | null>(null);
  const [webhookHistory, setWebhookHistory] = useState<any[]>([]);
  const [selectedWebhook, setSelectedWebhook] = useState<any | null>(null);
  const [isLoadingDiagnostics, setIsLoadingDiagnostics] = useState(false);

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

  const loadDiagnostics = async () => {
    setIsLoadingDiagnostics(true);
    setStatus(null);
    try {
      const [healthResponse, historyResponse] = await Promise.all([
        adminService.getHealth(),
        adminService.getWebhookHistory({ page: 0, size: 20 }).catch(() => []),
      ]);
      setHealth(healthResponse);
      setWebhookHistory(Array.isArray(historyResponse) ? historyResponse : []);
    } catch (err: any) {
      setStatus({ type: 'error', message: err?.message || 'Unable to load system diagnostics.' });
    } finally {
      setIsLoadingDiagnostics(false);
    }
  };

  const handleWebhookDetail = async (eventId: string | number) => {
    try {
      const detail = await adminService.getWebhookEvent(eventId);
      setSelectedWebhook(detail);
    } catch (err: any) {
      setStatus({ type: 'error', message: err?.message || 'Unable to load webhook event detail.' });
    }
  };

  const handleWebhookRetry = async (eventId: string | number) => {
    try {
      await adminService.retryWebhookEvent(eventId);
      setStatus({ type: 'success', message: 'Webhook retry request submitted.' });
      await loadDiagnostics();
    } catch (err: any) {
      setStatus({ type: 'error', message: err?.message || 'Unable to retry webhook event.' });
    }
  };

  React.useEffect(() => {
    void loadDiagnostics();
  }, []);

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

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-slate-900">System Health</h2>
              <p className="text-sm text-slate-500">Live backend health response and webhook diagnostics.</p>
            </div>
            <button
              type="button"
              onClick={loadDiagnostics}
              disabled={isLoadingDiagnostics}
              className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold uppercase tracking-[0.3em] text-slate-700"
            >
              {isLoadingDiagnostics ? 'Refreshing...' : 'Refresh'}
            </button>
          </div>

          <div className="mt-4 rounded-xl bg-slate-50 p-4">
            <pre className="whitespace-pre-wrap text-xs text-slate-700">{JSON.stringify(health, null, 2)}</pre>
          </div>

          <div className="mt-6 grid gap-6 lg:grid-cols-[1.2fr,1fr]">
            <div>
              <h3 className="text-sm font-bold uppercase tracking-[0.3em] text-slate-500">Webhook History</h3>
              <div className="mt-3 space-y-3">
                {webhookHistory.length === 0 ? (
                  <p className="text-sm text-slate-500">No webhook history available.</p>
                ) : (
                  webhookHistory.map((event, index) => {
                    const eventId = event?.eventId ?? event?.id ?? index;
                    return (
                      <div key={eventId} className="rounded-xl border border-slate-100 bg-slate-50/80 p-4">
                        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                          <div>
                            <p className="text-sm font-semibold text-slate-900">{event?.eventType ?? 'Webhook Event'}</p>
                            <p className="text-xs text-slate-500">Event ID: {eventId}</p>
                          </div>
                          <div className="flex gap-2">
                            <button
                              type="button"
                              onClick={() => handleWebhookDetail(eventId)}
                              className="rounded-full border border-slate-200 px-3 py-1 text-xs font-semibold text-slate-700"
                            >
                              View
                            </button>
                            <button
                              type="button"
                              onClick={() => handleWebhookRetry(eventId)}
                              className="rounded-full border border-blue-200 px-3 py-1 text-xs font-semibold text-blue-700"
                            >
                              Retry
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div>
              <h3 className="text-sm font-bold uppercase tracking-[0.3em] text-slate-500">Webhook Detail</h3>
              <div className="mt-3 rounded-xl bg-slate-50 p-4">
                <pre className="whitespace-pre-wrap text-xs text-slate-700">{JSON.stringify(selectedWebhook, null, 2)}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </AdminLayout>
  );
};

export default SystemInitialization;
