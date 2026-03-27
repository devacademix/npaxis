import React from 'react';
import type { VerificationStatus } from '../../services/preceptor';

interface StatusBadgeProps {
  status?: VerificationStatus | null;
}

const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  const normalized = String(status || 'PENDING').toUpperCase();

  const className =
    normalized === 'APPROVED'
      ? 'bg-emerald-100 text-emerald-700 border border-emerald-200'
      : normalized === 'REJECTED'
      ? 'bg-red-100 text-red-700 border border-red-200'
      : 'bg-amber-100 text-amber-700 border border-amber-200';

  return (
    <span className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${className}`}>
      {normalized}
    </span>
  );
};

export default StatusBadge;
