import React from 'react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';

export interface RevenueChartPoint {
  label: string;
  value: number;
}

interface RevenueChartProps {
  title: string;
  type: 'line' | 'bar';
  data: RevenueChartPoint[];
  valuePrefix?: string;
}

const RevenueChart: React.FC<RevenueChartProps> = ({ title, type, data, valuePrefix = '' }) => {
  return (
    <div className="rounded-xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
      <h3 className="mb-4 text-lg font-bold text-slate-900">{title}</h3>
      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          {type === 'line' ? (
            <LineChart data={data}>
              <CartesianGrid stroke="#e5e7eb" strokeDasharray="4 4" />
              <XAxis dataKey="label" tick={{ fill: '#64748b', fontSize: 12 }} />
              <YAxis tick={{ fill: '#64748b', fontSize: 12 }} />
              <Tooltip
                formatter={(value) => `${valuePrefix}${Number(value ?? 0).toLocaleString()}`}
                contentStyle={{ borderRadius: 12, borderColor: '#e2e8f0' }}
              />
              <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={3} dot={{ r: 3 }} />
            </LineChart>
          ) : (
            <BarChart data={data}>
              <CartesianGrid stroke="#e5e7eb" strokeDasharray="4 4" />
              <XAxis dataKey="label" tick={{ fill: '#64748b', fontSize: 12 }} />
              <YAxis tick={{ fill: '#64748b', fontSize: 12 }} />
              <Tooltip
                formatter={(value) => `${valuePrefix}${Number(value ?? 0).toLocaleString()}`}
                contentStyle={{ borderRadius: 12, borderColor: '#e2e8f0' }}
              />
              <Bar dataKey="value" fill="#4f46e5" radius={[8, 8, 0, 0]} />
            </BarChart>
          )}
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default RevenueChart;
