import React from 'react';

interface StatsCardProps {
  title: string;
  value: string | number;
  subtitle: string;
  icon: string;
  tone?: 'blue' | 'emerald' | 'amber';
}

const toneStyles: Record<NonNullable<StatsCardProps['tone']>, string> = {
  blue: 'bg-blue-50 text-blue-700',
  emerald: 'bg-emerald-50 text-emerald-700',
  amber: 'bg-amber-50 text-amber-700',
};

const StatsCard: React.FC<StatsCardProps> = ({ title, value, subtitle, icon, tone = 'blue' }) => {
  return (
    <article className="group rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200 transition-transform duration-200 hover:-translate-y-1 hover:shadow-md">
      <div className="mb-4 flex items-center justify-between">
        <div className={`inline-flex h-11 w-11 items-center justify-center rounded-xl ${toneStyles[tone]}`}>
          <span className="material-symbols-outlined text-xl">{icon}</span>
        </div>
      </div>
      <p className="text-xs font-bold uppercase tracking-wider text-slate-500">{title}</p>
      <p className="mt-1 text-3xl font-black tracking-tight text-slate-900">{value}</p>
      <p className="mt-2 text-sm text-slate-500">{subtitle}</p>
    </article>
  );
};

export default StatsCard;
