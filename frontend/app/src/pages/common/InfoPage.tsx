import React from 'react';
import { Link } from 'react-router-dom';

interface InfoPageProps {
  title: string;
  description: string;
}

const InfoPage: React.FC<InfoPageProps> = ({ title, description }) => {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <section className="w-full max-w-2xl rounded-2xl bg-white p-8 text-center shadow-sm ring-1 ring-slate-200">
        <h1 className="text-3xl font-black tracking-tight text-slate-900">{title}</h1>
        <p className="mt-2 text-sm text-slate-600">{description}</p>
        <div className="mt-6 flex justify-center gap-2">
          <Link
            to="/login"
            className="inline-flex items-center gap-1 rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            <span className="material-symbols-outlined text-base">arrow_back</span>
            Back to Login
          </Link>
        </div>
      </section>
    </main>
  );
};

export default InfoPage;
