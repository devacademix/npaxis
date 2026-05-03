import React, { useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useSession } from '../../context/SessionContext';
import StudentLayout from '../../components/layout/StudentLayout';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import SkeletonBlock from '../../components/ui/SkeletonBlock';
import { type InquiryStatusFilter } from '../../services/inquiry';
import { useInquiries } from '../../hooks/useInquiries';

const Inquiries: React.FC = () => {
  const { role, isLoading: isSessionLoading } = useSession();
  const isStudent = role === 'STUDENT';
  const [status, setStatus] = useState<InquiryStatusFilter>('NEW');
  const query = useInquiries(status, {
    scope: 'student',
    enabled: !isSessionLoading && isStudent,
  });
  const items = query.data ?? [];
  const isLoading = isSessionLoading || (query.isLoading && items.length === 0);
  const isRefreshing = query.isFetching && !isLoading;
  const error = query.error?.message || null;
  const emptyLabel = useMemo(
    () => `No ${status === 'ALL' ? '' : status.toLowerCase()} inquiries found.`.replace('  ', ' '),
    [status]
  );

  if (!isSessionLoading && !isStudent) {
    return <Navigate to="/login" replace />;
  }

  return (
    <StudentLayout pageTitle="My Inquiries">
      <div className="mx-auto max-w-6xl space-y-6">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <h2 className="text-3xl font-black tracking-tight text-slate-900">My Inquiries</h2>
            <p className="mt-1 text-sm text-slate-500">Track messages sent to preceptors and monitor their status.</p>
          </div>
          <select
            value={status}
            onChange={(event) => setStatus(event.target.value as InquiryStatusFilter)}
            aria-label="Filter inquiries by status"
            className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-700"
          >
            <option value="ALL">All Statuses</option>
            <option value="NEW">New</option>
            <option value="READ">Read</option>
            <option value="REPLIED">Replied</option>
          </select>
        </div>

        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{error}</div>
        ) : null}

        {isRefreshing && items.length > 0 ? (
          <div className="rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-medium text-blue-700">
            Refreshing inquiries...
          </div>
        ) : null}

        <div className="overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
          {isLoading ? (
            <div className="space-y-3 p-6">
              {Array.from({ length: 4 }, (_, index) => (
                <SkeletonBlock key={index} className="h-16" />
              ))}
            </div>
          ) : error && items.length === 0 ? (
            <div className="p-6">
              <ErrorState message={error} onRetry={() => void query.refetch()} />
            </div>
          ) : items.length === 0 ? (
            <div className="p-6">
              <EmptyState text={emptyLabel} />
            </div>
          ) : (
            <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Subject</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Message</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Created</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {items.map((item) => (
                  <tr key={item.inquiryId} className="align-top hover:bg-slate-50">
                    <td className="px-4 py-3 text-sm font-semibold text-slate-900">{item.subject}</td>
                    <td className="px-4 py-3 text-sm text-slate-600">{item.message}</td>
                    <td className="px-4 py-3 text-sm font-semibold text-slate-700">{item.status}</td>
                    <td className="px-4 py-3 text-sm text-slate-500">
                      {item.createdAt ? new Date(item.createdAt).toLocaleString() : 'N/A'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          )}
        </div>
      </div>
    </StudentLayout>
  );
};

export default Inquiries;
