import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getDefaultPathForRole, getStoredRole, getStoredToken } from '../../utils/auth';

const StripeReturn: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const token = getStoredToken();
    const role = getStoredRole();
    const isCancel = location.pathname.endsWith('/cancel');

    if (!token) {
      navigate('/login', { replace: true, state: { from: location } });
      return;
    }

    if (role === 'PRECEPTOR') {
      navigate(`/preceptor/subscription${isCancel ? '?checkout=canceled' : '?checkout=success'}`, { replace: true });
      return;
    }

    navigate(getDefaultPathForRole(role), { replace: true });
  }, [location, navigate]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-6 text-center">
      <div>
        <h1 className="text-2xl font-black text-slate-900">Returning to your account</h1>
        <p className="mt-2 text-sm text-slate-500">Please wait while we finish your checkout flow.</p>
      </div>
    </div>
  );
};

export default StripeReturn;
