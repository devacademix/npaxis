import React from 'react';
import { Navigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';

interface PlaceholderProps {
  title: string;
  description: string;
}

const Placeholder: React.FC<PlaceholderProps> = ({ title, description }) => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  return (
    <StudentLayout pageTitle={title}>
      <div className="mx-auto max-w-4xl rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-20 text-center shadow-sm">
        <h2 className="text-2xl font-black tracking-tight text-slate-900">{title}</h2>
        <p className="mt-2 text-sm text-slate-600">{description}</p>
      </div>
    </StudentLayout>
  );
};

export default Placeholder;
