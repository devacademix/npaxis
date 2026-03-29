import React from 'react';
import PreceptorLayout from '../../components/layout/PreceptorLayout';

interface PlaceholderPageProps {
  title: string;
  description: string;
}

const PlaceholderPage: React.FC<PlaceholderPageProps> = ({ title, description }) => {
  return (
    <PreceptorLayout pageTitle={title}>
      <div className="rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <h2 className="text-2xl font-bold text-slate-900">{title}</h2>
        <p className="mt-2 text-slate-500">{description}</p>
      </div>
    </PreceptorLayout>
  );
};

export default PlaceholderPage;
