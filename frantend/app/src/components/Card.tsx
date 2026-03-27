import React from 'react';

interface CardProps {
  title: string;
  value: string | number;
  subtitle: string;
  trendText: string;
  icon: string;
  colorClass: string;
}

const Card: React.FC<CardProps> = ({ title, value, subtitle, trendText, icon, colorClass }) => {
  return (
    <div className="bg-surface-container-lowest p-6 rounded-xl border border-transparent hover:border-blue-100 transition-all">
      <div className="flex justify-between items-start mb-4">
        <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${colorClass}`}>
          <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>
            {icon}
          </span>
        </div>
        <span className="text-emerald-600 bg-emerald-50 px-2 py-1 rounded-full text-[10px] font-bold">
          {trendText}
        </span>
      </div>
      <p className="text-slate-500 text-xs font-bold uppercase tracking-wider mb-1">{title}</p>
      <h3 className="text-3xl font-extrabold text-on-surface">{value}</h3>
      <p className="text-[10px] text-slate-400 mt-2 italic">{subtitle}</p>
    </div>
  );
};

export default Card;
