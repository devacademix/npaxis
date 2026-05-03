import React from 'react';

interface DashboardCardProps {
  label: string;
  value: string;
  icon: string;
  helper: string;
}

const DashboardCard: React.FC<DashboardCardProps> = ({ label, value, icon, helper }) => {
  return (
    <article className="group rounded-3xl border border-slate-200 bg-white p-5 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-lg">
      <div className="flex items-start justify-between gap-3">
        <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-100 text-slate-700 transition-colors group-hover:bg-blue-50 group-hover:text-blue-700">
          <span className="material-symbols-outlined">{icon}</span>
        </div>
        <p className="text-[11px] font-bold uppercase tracking-[0.24em] text-slate-500">{label}</p>
      </div>
      <p className="mt-5 text-4xl font-black tracking-tight text-slate-900">{value}</p>
      <p className="mt-2 text-sm text-slate-500">{helper}</p>
    </article>
  );
};

export default DashboardCard;
