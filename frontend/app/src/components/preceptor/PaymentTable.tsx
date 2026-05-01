import React from 'react';
import type { PaymentHistoryItem } from '../../services/payment';

interface PaymentTableProps {
  payments: PaymentHistoryItem[];
  isLoading: boolean;
  onViewInvoice: (invoiceUrl?: string) => void;
}

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(amount || 0);

const formatDate = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'N/A';
  return date.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
};

const statusBadgeClass = (status: string) => {
  const normalized = status.toUpperCase();
  if (normalized === 'PAID') return 'bg-emerald-100 text-emerald-700';
  if (normalized === 'FAILED') return 'bg-red-100 text-red-700';
  return 'bg-amber-100 text-amber-700';
};

const PaymentTable: React.FC<PaymentTableProps> = ({ payments, isLoading, onViewInvoice }) => {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 4 }, (_, index) => (
          <div key={index} className="h-14 animate-pulse rounded-xl bg-slate-200/70" />
        ))}
      </div>
    );
  }

  if (payments.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-12 text-center text-sm font-medium text-slate-500">
        No transactions found
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Transaction ID</th>
              <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Amount</th>
              <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
              <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Date</th>
              <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 bg-white">
            {payments.map((payment) => (
              <tr key={payment.transactionId} className="transition-colors hover:bg-slate-50">
                <td className="px-4 py-3 text-sm font-semibold text-slate-700">{payment.transactionId}</td>
                <td className="px-4 py-3 text-sm font-semibold text-slate-800">{formatCurrency(payment.amount)}</td>
                <td className="px-4 py-3 text-sm">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-bold uppercase ${statusBadgeClass(payment.status)}`}>
                    {payment.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-sm text-slate-600">{formatDate(payment.date)}</td>
                <td className="px-4 py-3">
                  <button
                    type="button"
                    onClick={() => onViewInvoice(payment.invoiceUrl)}
                    className="rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                  >
                    {payment.invoiceUrl ? 'Download Invoice' : 'View'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PaymentTable;
