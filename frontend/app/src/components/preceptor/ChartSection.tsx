import React from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

export interface ViewsDataPoint {
  label: string;
  views: number;
}

export interface InteractionsDataPoint {
  label: string;
  contactReveals: number;
  inquiries: number;
}

interface ChartSectionProps {
  viewsData: ViewsDataPoint[];
  interactionsData: InteractionsDataPoint[];
}

const emptyCard = (title: string, description: string) => (
  <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
    <h3 className="text-xl font-bold text-slate-900">{title}</h3>
    <p className="mt-1 text-sm text-slate-500">{description}</p>
    <div className="mt-6 flex h-[280px] items-center justify-center rounded-xl border border-dashed border-slate-300 bg-slate-50 text-sm font-semibold text-slate-500">
      No analytics data available
    </div>
  </div>
);

const ChartSection: React.FC<ChartSectionProps> = ({ viewsData, interactionsData }) => {
  return (
    <section className="grid grid-cols-1 gap-6 xl:grid-cols-2">
      {viewsData.length === 0 ? (
        emptyCard('Profile Views Over Time', 'Track how often students discover your profile.')
      ) : (
        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h3 className="text-xl font-bold text-slate-900">Profile Views Over Time</h3>
          <p className="mt-1 text-sm text-slate-500">Trend of profile visibility by month.</p>
          <div className="mt-5 h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={viewsData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="label" stroke="#64748b" tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" tickLine={false} axisLine={false} allowDecimals={false} />
                <Tooltip />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="views"
                  stroke="#2563eb"
                  strokeWidth={3}
                  dot={{ r: 3 }}
                  activeDot={{ r: 5 }}
                  name="Profile Views"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {interactionsData.length === 0 ? (
        emptyCard('Interactions Per Month', 'Compare contact reveals and inquiries over time.')
      ) : (
        <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h3 className="text-xl font-bold text-slate-900">Interactions Per Month</h3>
          <p className="mt-1 text-sm text-slate-500">Contact reveals and inquiry engagement.</p>
          <div className="mt-5 h-[280px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={interactionsData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                <XAxis dataKey="label" stroke="#64748b" tickLine={false} axisLine={false} />
                <YAxis stroke="#64748b" tickLine={false} axisLine={false} allowDecimals={false} />
                <Tooltip />
                <Legend />
                <Bar dataKey="contactReveals" fill="#0ea5e9" radius={[6, 6, 0, 0]} name="Contact Reveals" />
                <Bar dataKey="inquiries" fill="#7c3aed" radius={[6, 6, 0, 0]} name="Inquiries" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </section>
  );
};

export default ChartSection;
