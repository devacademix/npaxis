import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import inquiryService, { type InquiryRecord } from '../../services/inquiry';

const Inquiries: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');

  const [items, setItems] = useState<InquiryRecord[]>([]);
  const [status, setStatus] = useState('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isStudent) return;

    const loadInquiries = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await inquiryService.getMyInquiries(status);
        setItems(response);
      } catch (err: any) {
        setError(err?.message || 'Failed to load inquiries.');
      } finally {
        setIsLoading(false);
      }
    };

    loadInquiries();
  }, [isStudent, status]);

  if (!isStudent) {
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
            onChange={(event) => setStatus(event.target.value)}
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

        <div className="overflow-hidden rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
          {isLoading ? (
            <div className="space-y-3 p-6">
              {Array.from({ length: 4 }, (_, index) => (
                <div key={index} className="h-16 animate-pulse rounded-xl bg-slate-200/70" />
              ))}
            </div>
          ) : items.length === 0 ? (
            <div className="px-6 py-14 text-center text-sm font-medium text-slate-500">No inquiries found.</div>
          ) : (
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
          )}
        </div>
      </div>
    </StudentLayout>
  );
};

export default Inquiries;
