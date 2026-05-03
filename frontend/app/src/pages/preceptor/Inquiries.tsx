import React from 'react';
import { Navigate } from 'react-router-dom';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import SkeletonBlock from '../../components/ui/SkeletonBlock';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import { useSession } from '../../context/SessionContext';
import inquiryService, { type InquiryStatusFilter } from '../../services/inquiry';
import { usePreceptorInquiries } from '../../hooks/usePreceptorInquiries';
import { maskName } from '../../utils/maskName';

const statusTabs = [
  { label: 'All', value: 'ALL' },
  { label: 'New', value: 'NEW' },
  { label: 'Read', value: 'READ' },
  { label: 'Replied', value: 'REPLIED' },
];

const Inquiries: React.FC = () => {
  const { currentUser, role, isLoading: isSessionLoading } = useSession();
  const { inquiries, loading, refreshing, error, status, diagnostics, setStatus, refetch } = usePreceptorInquiries();
  const [actionError, setActionError] = React.useState<string | null>(null);

  if (!isSessionLoading && role !== 'PRECEPTOR') {
    return <Navigate to="/login" replace />;
  }

  const formatDateTime = (value?: string) => {
    if (!value) return 'N/A';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return 'N/A';
    return date.toLocaleString('en-US', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const markAsRead = async (inquiryId: number) => {
    try {
      setActionError(null);
      await inquiryService.markAsRead(inquiryId);
      await refetch(status, true);
    } catch (err: any) {
      setActionError(err?.message || 'Unable to mark inquiry as read.');
    }
  };

  return (
    <PreceptorLayout pageTitle="Inquiries">
      <div className="mx-auto max-w-6xl space-y-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <h2 className="text-3xl font-black tracking-tight text-slate-900">Student Inquiries</h2>
            <p className="mt-1 text-sm text-slate-500">Review messages sent to your profile and keep the inbox organized.</p>
          </div>

          <div className="flex flex-wrap gap-2">
            {statusTabs.map((tab) => (
              <button
                key={tab.value}
                type="button"
                aria-label={`Show ${tab.label.toLowerCase()} inquiries`}
                onClick={() => setStatus(tab.value as InquiryStatusFilter)}
                className={`rounded-full px-4 py-2 text-sm font-semibold transition ${
                  status === tab.value
                    ? 'bg-slate-900 text-white'
                    : 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{error}</div>
        ) : null}
        {actionError ? (
          <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-700">{actionError}</div>
        ) : null}

        {refreshing && inquiries.length > 0 ? (
          <div className="rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-medium text-blue-700">
            Refreshing inquiries...
          </div>
        ) : null}

        <details className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700">
          <summary className="cursor-pointer font-semibold text-slate-900">Inquiry Diagnostics</summary>
          <div className="mt-3 space-y-2">
            <p><span className="font-semibold">Auth role:</span> {String(diagnostics.authRole ?? 'null')}</p>
            <p><span className="font-semibold">Session role:</span> {String(role ?? 'null')}</p>
            <p><span className="font-semibold">Current user:</span> {currentUser?.displayName || 'N/A'} ({currentUser?.userId ?? 'N/A'})</p>
            <p><span className="font-semibold">API URL:</span> {diagnostics.apiUrl}</p>
            <p><span className="font-semibold">Token present:</span> {String(diagnostics.tokenPresent)}</p>
            <p><span className="font-semibold">Count:</span> {diagnostics.itemCount}</p>
            <p><span className="font-semibold">Query status:</span> {diagnostics.queryStatus}</p>
            <pre className="overflow-auto rounded-xl bg-slate-900 p-4 text-xs text-slate-100">
              {JSON.stringify(diagnostics.rawResponse, null, 2)}
            </pre>
            <pre className="overflow-auto rounded-xl bg-slate-900 p-4 text-xs text-slate-100">
              {JSON.stringify(inquiries, null, 2)}
            </pre>
          </div>
        </details>

        <div className="overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
          {loading || isSessionLoading ? (
            <div className="space-y-3 p-6">
              {Array.from({ length: 4 }, (_, index) => (
                <SkeletonBlock key={index} className="h-16" />
              ))}
            </div>
          ) : error && inquiries.length === 0 ? (
            <div className="p-6">
              <ErrorState message="Unable to load inquiries with the current session." onRetry={() => void refetch(status, true)} />
            </div>
          ) : !error && inquiries.length === 0 ? (
            <div className="p-6">
              <EmptyState text={`No ${status === 'ALL' ? '' : status.toLowerCase()} inquiries received yet.`.replace('  ', ' ')} />
            </div>
          ) : (
            <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Student</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Subject</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Message</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Received</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {inquiries.map((item) => (
                  <tr key={item.inquiryId} className="align-top hover:bg-slate-50">
                    <td className="px-4 py-3 text-sm font-semibold text-slate-900">
                      {item.studentName ? maskName(item.studentName) : 'Student'}
                    </td>
                    <td className="px-4 py-3 text-sm font-semibold text-slate-900">{item.subject}</td>
                    <td className="px-4 py-3 text-sm text-slate-600">{item.message}</td>
                    <td className="px-4 py-3 text-sm text-slate-600">{formatDateTime(item.createdAt)}</td>
                    <td className="px-4 py-3 text-sm font-semibold text-slate-700">{item.status}</td>
                    <td className="px-4 py-3">
                      <button
                        type="button"
                        disabled={item.status === 'READ'}
                        onClick={() => markAsRead(item.inquiryId)}
                        className="rounded-full border border-slate-200 px-3 py-1 text-xs font-semibold text-slate-700 hover:bg-slate-100 disabled:opacity-50"
                      >
                        Mark as read
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          )}
        </div>
      </div>
    </PreceptorLayout>
  );
};

export default Inquiries;
