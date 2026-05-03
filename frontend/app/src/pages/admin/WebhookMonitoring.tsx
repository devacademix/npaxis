import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import {
  adminService,
  type WebhookEventDetail,
  type WebhookEventHistoryItem,
  type WebhookMetrics,
} from '../../services/admin';

const PAGE_SIZE = 10;

const formatDateTime = (value?: string) => {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'N/A';
  return date.toLocaleString('en-US', {
    month: 'short',
    day: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const prettyJson = (value?: string) => {
  if (!value) return 'No payload available.';
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
};

const getStatusTone = (status: string) => {
  const normalized = status.toUpperCase();
  if (normalized === 'SUCCEEDED') return 'bg-emerald-100 text-emerald-700';
  if (normalized === 'FAILED') return 'bg-rose-100 text-rose-700';
  if (normalized === 'RETRYING') return 'bg-amber-100 text-amber-700';
  return 'bg-slate-100 text-slate-700';
};

const WebhookMonitoring: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';

  const [metrics, setMetrics] = useState<WebhookMetrics | null>(null);
  const [history, setHistory] = useState<WebhookEventHistoryItem[]>([]);
  const [historyPage, setHistoryPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [eventTypeFilter, setEventTypeFilter] = useState('');
  const [isMetricsLoading, setIsMetricsLoading] = useState(true);
  const [isHistoryLoading, setIsHistoryLoading] = useState(true);
  const [metricsError, setMetricsError] = useState<string | null>(null);
  const [historyError, setHistoryError] = useState<string | null>(null);
  const [activeDetail, setActiveDetail] = useState<WebhookEventDetail | null>(null);
  const [activeSummary, setActiveSummary] = useState<WebhookEventHistoryItem | null>(null);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [retryingEventId, setRetryingEventId] = useState<string | null>(null);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const loadMetrics = useCallback(async () => {
    try {
      setIsMetricsLoading(true);
      setMetricsError(null);
      const response = await adminService.getWebhookMetrics();
      setMetrics(response);
    } catch (err: any) {
      setMetricsError(err?.message || 'Failed to load webhook metrics.');
    } finally {
      setIsMetricsLoading(false);
    }
  }, []);

  const loadHistory = useCallback(async (page: number) => {
    try {
      setIsHistoryLoading(true);
      setHistoryError(null);
      const response = await adminService.getWebhookHistory({ page: page - 1, size: PAGE_SIZE });
      setHistory(response.items);
      setTotalPages(Math.max(1, response.totalPages || 1));
      setTotalElements(response.totalElements);
    } catch (err: any) {
      setHistoryError(err?.message || 'Failed to load webhook history.');
    } finally {
      setIsHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!isAdmin) return;
    loadMetrics();
  }, [isAdmin, loadMetrics]);

  useEffect(() => {
    if (!isAdmin) return;
    loadHistory(historyPage);
  }, [historyPage, isAdmin, loadHistory]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const filteredHistory = useMemo(() => {
    const eventTypeKeyword = eventTypeFilter.trim().toLowerCase();
    return history.filter((item) => {
      const matchesStatus = statusFilter === 'ALL' || item.status.toUpperCase() === statusFilter;
      const matchesType =
        !eventTypeKeyword || item.eventType.toLowerCase().includes(eventTypeKeyword);
      return matchesStatus && matchesType;
    });
  }, [eventTypeFilter, history, statusFilter]);

  const handleOpenDetail = async (item: WebhookEventHistoryItem) => {
    setActiveSummary(item);
    setActiveDetail(null);
    setDetailError(null);
    setIsDetailLoading(true);

    try {
      const response = await adminService.getWebhookEventDetail(item.eventId);
      setActiveDetail(response);
    } catch (err: any) {
      setDetailError(err?.message || 'Failed to load webhook event detail.');
    } finally {
      setIsDetailLoading(false);
    }
  };

  const handleRetry = async (eventId: string) => {
    try {
      setRetryingEventId(eventId);
      await adminService.retryWebhookEvent(eventId);
      setToast({ type: 'success', message: 'Webhook retry initiated successfully.' });
      await Promise.all([loadMetrics(), loadHistory(historyPage)]);
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Failed to retry webhook event.' });
    } finally {
      setRetryingEventId(null);
    }
  };

  if (!isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AdminLayout>
      <div className="mb-8 flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight text-on-surface font-headline">Webhook Monitoring</h1>
          <p className="mt-2 max-w-2xl text-slate-500">
            Monitor delivery health, inspect event payloads, and retry failed webhook deliveries from one place.
          </p>
        </div>
        <button
          type="button"
          onClick={() => {
            loadMetrics();
            loadHistory(historyPage);
          }}
          className="rounded-full bg-blue-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-700"
        >
          Refresh
        </button>
      </div>

      {metricsError ? (
        <div className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{metricsError}</div>
      ) : null}

      <div className="mb-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {[
          {
            label: 'Success Rate',
            value: isMetricsLoading ? '...' : `${Number(metrics?.successRate ?? 0).toFixed(1)}%`,
            helper: 'Successful webhook deliveries',
          },
          {
            label: 'Succeeded Events',
            value: isMetricsLoading ? '...' : String(metrics?.successfulCount ?? 0),
            helper: 'Delivered without retry',
          },
          {
            label: 'Failed Events',
            value: isMetricsLoading ? '...' : String(metrics?.failedCount ?? 0),
            helper: 'Need investigation',
          },
          {
            label: 'Retrying Events',
            value: isMetricsLoading ? '...' : String(metrics?.failedRetryingCount ?? 0),
            helper: isMetricsLoading
              ? '...'
              : `Avg retries ${Number(metrics?.averageRetryCount ?? 0).toFixed(1)}`,
          },
        ].map((card) => (
          <div key={card.label} className="rounded-3xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <p className="text-xs font-bold uppercase tracking-[0.24em] text-slate-500">{card.label}</p>
            <p className="mt-4 text-4xl font-extrabold tracking-tight text-slate-900">{card.value}</p>
            <p className="mt-2 text-sm text-slate-500">{card.helper}</p>
          </div>
        ))}
      </div>

      <div className="mb-6 grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
        <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-end">
            <div className="flex-1">
              <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Event Type Filter</label>
              <input
                type="text"
                value={eventTypeFilter}
                onChange={(event) => setEventTypeFilter(event.target.value)}
                placeholder="Filter by event type"
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              />
            </div>
            <div className="w-full md:w-56">
              <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Status</label>
              <select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value)}
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              >
                <option value="ALL">All Statuses</option>
                <option value="SUCCEEDED">Succeeded</option>
                <option value="FAILED">Failed</option>
                <option value="RETRYING">Retrying</option>
              </select>
            </div>
          </div>

          {historyError ? (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{historyError}</div>
          ) : (
            <div className="overflow-hidden rounded-2xl border border-slate-100">
              <div className="overflow-x-auto">
                <table className="w-full min-w-[820px] text-left">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500">Event ID</th>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500">Event Type</th>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500">Status</th>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500">Processed</th>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500">Retries</th>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500">Error</th>
                      <th className="px-5 py-3 text-[11px] font-bold uppercase tracking-wider text-slate-500 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white">
                    {isHistoryLoading ? (
                      Array.from({ length: 6 }, (_, index) => (
                        <tr key={index}>
                          <td colSpan={7} className="px-5 py-4">
                            <div className="h-8 animate-pulse rounded-lg bg-slate-100" />
                          </td>
                        </tr>
                      ))
                    ) : filteredHistory.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="px-5 py-10 text-center text-sm text-slate-500">
                          No webhook events match the current filters.
                        </td>
                      </tr>
                    ) : (
                      filteredHistory.map((item) => (
                        <tr key={item.eventId} className="hover:bg-slate-50">
                          <td className="px-5 py-4 text-sm font-semibold text-slate-900">{item.eventId}</td>
                          <td className="px-5 py-4 text-sm text-slate-700">{item.eventType}</td>
                          <td className="px-5 py-4">
                            <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${getStatusTone(item.status)}`}>
                              {item.status}
                            </span>
                          </td>
                          <td className="px-5 py-4 text-sm text-slate-600">{formatDateTime(item.processedAt)}</td>
                          <td className="px-5 py-4 text-sm text-slate-600">{item.retryCount}</td>
                          <td className="max-w-[220px] truncate px-5 py-4 text-sm text-slate-500">{item.errorMessage || 'None'}</td>
                          <td className="px-5 py-4">
                            <div className="flex items-center justify-end gap-2">
                              <button
                                type="button"
                                onClick={() => handleOpenDetail(item)}
                                className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                              >
                                View
                              </button>
                              <button
                                type="button"
                                disabled={retryingEventId === item.eventId || item.status.toUpperCase() !== 'FAILED'}
                                onClick={() => handleRetry(item.eventId)}
                                className="rounded-lg bg-amber-500 px-3 py-1.5 text-xs font-semibold text-white hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-50"
                              >
                                {retryingEventId === item.eventId ? 'Retrying...' : 'Retry Failed'}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
              Page {historyPage} of {totalPages} | {totalElements} total events
            </p>
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setHistoryPage((prev) => Math.max(1, prev - 1))}
                disabled={historyPage === 1}
                className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Previous
              </button>
              <button
                type="button"
                onClick={() => setHistoryPage((prev) => Math.min(totalPages, prev + 1))}
                disabled={historyPage === totalPages}
                className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Next
              </button>
            </div>
          </div>
        </div>

        <div className="rounded-3xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-bold text-slate-900">Operational Snapshot</h2>
          <div className="mt-5 space-y-4 text-sm">
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Most Common Event</p>
              <p className="mt-1 font-semibold text-slate-900">{metrics?.mostCommonEventType || 'N/A'}</p>
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Oldest Pending / Retrying Event</p>
              <p className="mt-1 font-semibold text-slate-900">{formatDateTime(metrics?.oldestPendingEventDate)}</p>
            </div>
            <div>
              <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Report Generated</p>
              <p className="mt-1 font-semibold text-slate-900">{formatDateTime(metrics?.reportGeneratedAt)}</p>
            </div>
          </div>
        </div>
      </div>

      {activeSummary ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
          <div className="w-full max-w-3xl rounded-3xl bg-white p-6 shadow-xl">
            <div className="mb-5 flex items-start justify-between gap-4">
              <div>
                <h3 className="text-2xl font-bold text-slate-900">Webhook Event Detail</h3>
                <p className="mt-1 text-sm text-slate-500">{activeSummary.eventId}</p>
              </div>
              <button
                type="button"
                onClick={() => {
                  setActiveSummary(null);
                  setActiveDetail(null);
                  setDetailError(null);
                }}
                className="rounded-full border border-slate-200 p-2 text-slate-500 hover:text-slate-700"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            {isDetailLoading ? (
              <div className="flex items-center gap-3 rounded-xl bg-slate-50 px-4 py-4 text-sm font-medium text-slate-600">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
                Loading event detail...
              </div>
            ) : detailError ? (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{detailError}</div>
            ) : (
              <div className="space-y-5">
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Event Type</p>
                    <p className="mt-1 text-sm font-semibold text-slate-900">{activeDetail?.eventType || activeSummary.eventType}</p>
                  </div>
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Status</p>
                    <p className="mt-1">
                      <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-bold ${getStatusTone(activeDetail?.status || activeSummary.status)}`}>
                        {activeDetail?.status || activeSummary.status}
                      </span>
                    </p>
                  </div>
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Event Date</p>
                    <p className="mt-1 text-sm font-semibold text-slate-900">{formatDateTime(activeDetail?.eventDate || activeSummary.processedAt)}</p>
                  </div>
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Last Updated</p>
                    <p className="mt-1 text-sm font-semibold text-slate-900">{formatDateTime(activeDetail?.lastUpdated)}</p>
                  </div>
                </div>

                <div className="rounded-2xl border border-slate-200">
                  <div className="border-b border-slate-200 px-4 py-3">
                    <p className="text-xs font-bold uppercase tracking-wider text-slate-500">Event Payload</p>
                  </div>
                  <pre className="max-h-[360px] overflow-auto bg-slate-950 px-4 py-4 text-xs text-slate-100">
                    {prettyJson(activeDetail?.eventPayload)}
                  </pre>
                </div>
              </div>
            )}
          </div>
        </div>
      ) : null}

      {toast ? (
        <div
          className={`fixed bottom-6 right-6 z-50 rounded-xl px-4 py-3 text-sm font-semibold shadow-lg ${
            toast.type === 'success'
              ? 'border border-emerald-200 bg-emerald-50 text-emerald-800'
              : 'border border-red-200 bg-red-50 text-red-800'
          }`}
        >
          {toast.message}
        </div>
      ) : null}
    </AdminLayout>
  );
};

export default WebhookMonitoring;
