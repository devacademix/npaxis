import React from 'react';
import LayoutWrapper from './LayoutWrapper';

interface PreceptorLayoutProps {
  children: React.ReactNode;
  pageTitle?: string;
}

const navItems = [
  { to: '/preceptor/dashboard', label: 'Dashboard', icon: 'dashboard' },
  { to: '/preceptor/profile', label: 'Profile', icon: 'person' },
  { to: '/preceptor/license-verification', label: 'License', icon: 'workspace_premium' },
  { to: '/preceptor/inquiries', label: 'Inquiries', icon: 'mark_email_unread' },
  { to: '/preceptor/subscription', label: 'Subscription', icon: 'workspace_premium' },
  { to: '/preceptor/billing', label: 'Billing', icon: 'payments' },
];

const PreceptorLayout: React.FC<PreceptorLayoutProps> = ({ children, pageTitle = 'Preceptor Dashboard' }) => {
  return (
    <LayoutWrapper
      navItems={navItems}
      brandTitle="NPaxis"
      brandSubtitle="Preceptor Portal"
      pageTitle={pageTitle}
    >
      {children}
    </LayoutWrapper>
  );
};

export default PreceptorLayout;
