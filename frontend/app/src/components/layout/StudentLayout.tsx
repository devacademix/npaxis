import React from 'react';
import LayoutWrapper from './LayoutWrapper';

interface StudentLayoutProps {
  children: React.ReactNode;
  pageTitle?: string;
  headerLeading?: React.ReactNode;
  topSearch?: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
  };
}

const navItems = [
  { label: 'Dashboard', icon: 'dashboard', to: '/student/dashboard' },
  { label: 'Browse', icon: 'travel_explore', to: '/student/browse' },
  { label: 'Saved', icon: 'favorite', to: '/student/saved' },
  { label: 'Inquiries', icon: 'forum', to: '/student/inquiries' },
  { label: 'Profile', icon: 'person', to: '/student/profile' },
];

const StudentLayout: React.FC<StudentLayoutProps> = ({
  children,
  pageTitle = 'Student Dashboard',
  headerLeading,
  topSearch,
}) => {
  return (
    <LayoutWrapper
      navItems={navItems}
      brandTitle="NPaxis"
      brandSubtitle="Student Portal"
      pageTitle={pageTitle}
      headerLeading={headerLeading}
      topSearch={topSearch}
    >
      {children}
    </LayoutWrapper>
  );
};

export default StudentLayout;
