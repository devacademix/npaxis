import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import inquiryService, { type InquiryRecord } from '../../services/inquiry';

const Inquiries: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');

  const [items, setItems] = useState<InquiryRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadInquiries = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await inquiryService.getMyInquiries();
      setItems(response);
    } catch (err: any) {
      setError(err?.message || 'Failed to load inquiries.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (!isPreceptor) return;
    loadInquiries();
  }, [isPreceptor]);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const markAsRead = async (inquiryId: number) => {
    try {
      setActionError(null);
      await inquiryService.markAsRead(inquiryId);
      setItems((prev) => prev.map((item) => (item.inquiryId === inquiryId ? { ...item, status: 'READ' } : item)));
    } catch (err: any) {
      setActionError(err?.message || 'Unable to mark inquiry as read.');
    }
  };

  return (
    <PreceptorLayout pageTitle="Inquiries">
      <div className="mx-auto max-w-6xl space-y-6">
        <div>
          <h2 className="text-3xl font-black tracking-tight text-slate-900">Student Inquiries</h2>
          <p className="mt-1 text-sm text-slate-500">Review messages sent to your profile and keep the inbox organized.</p>
        </div>

        {error ? (
          <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{error}</div>
        ) : null}
        {actionError ? (
          <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-700">{actionError}</div>
        ) : null}

        <div className="overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
          {isLoading ? (
            <div className="space-y-3 p-6">
              {Array.from({ length: 4 }, (_, index) => (
                <div key={index} className="h-16 animate-pulse rounded-xl bg-slate-200/70" />
              ))}
            </div>
          ) : items.length === 0 ? (
            <div className="px-6 py-14 text-center text-sm font-medium text-slate-500">No inquiries have been received yet.</div>
          ) : (
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Student</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Subject</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Message</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 bg-white">
                {items.map((item) => (
                  <tr key={item.inquiryId} className="align-top hover:bg-slate-50">
                    <td className="px-4 py-3 text-sm font-semibold text-slate-900">{item.studentName || 'Student'}</td>
                    <td className="px-4 py-3 text-sm font-semibold text-slate-900">{item.subject}</td>
                    <td className="px-4 py-3 text-sm text-slate-600">{item.message}</td>
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
          )}
        </div>
      </div>
    </PreceptorLayout>
  );
};

export default Inquiries;
