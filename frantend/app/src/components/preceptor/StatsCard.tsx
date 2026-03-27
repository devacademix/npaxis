import React from 'react';

interface StatsCardProps {
  title: string;
  value: string;
  subtitle: string;
  icon: string;
  badge?: {
    text: string;
    tone: 'success' | 'neutral';
  };
}

const badgeStyles: Record<NonNullable<StatsCardProps['badge']>['tone'], string> = {
  success: 'bg-emerald-100 text-emerald-700',
  neutral: 'bg-slate-100 text-slate-700',
};

const StatsCard: React.FC<StatsCardProps> = ({ title, value, subtitle, icon, badge }) => {
  return (
    <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200 transition-all hover:-translate-y-0.5 hover:shadow-md">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-700">
          <span className="material-symbols-outlined">{icon}</span>
        </div>
        {badge ? (
          <span className={`rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${badgeStyles[badge.tone]}`}>
            {badge.text}
          </span>
        ) : null}
      </div>

      <p className="text-[11px] font-bold uppercase tracking-wider text-slate-500">{title}</p>
      <p className="mt-1 text-3xl font-black tracking-tight text-slate-900">{value}</p>
      <p className="mt-2 text-sm text-slate-500">{subtitle}</p>
    </article>
  );
};

export default StatsCard;
