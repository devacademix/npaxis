import React from 'react';

interface RevenueCardProps {
  title: string;
  value: string;
  subtitle?: string;
  icon?: string;
  accentClass?: string;
}

const RevenueCard: React.FC<RevenueCardProps> = ({
  title,
  value,
  subtitle,
  icon = 'payments',
  accentClass = 'bg-blue-50 text-blue-700',
}) => {
  return (
    <div className="rounded-xl bg-white p-5 shadow-sm ring-1 ring-slate-200 transition-all hover:-translate-y-0.5 hover:shadow-md">
      <div className="mb-4 flex items-center justify-between">
        <span className={`inline-flex h-10 w-10 items-center justify-center rounded-lg ${accentClass}`}>
          <span className="material-symbols-outlined text-lg">{icon}</span>
        </span>
      </div>
      <p className="text-[11px] font-bold uppercase tracking-wider text-slate-500">{title}</p>
      <p className="mt-2 text-3xl font-black tracking-tight text-slate-900">{value}</p>
      {subtitle ? <p className="mt-2 text-xs text-slate-500">{subtitle}</p> : null}
    </div>
  );
};

export default RevenueCard;
