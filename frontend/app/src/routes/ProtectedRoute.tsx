import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import {
  getDefaultPathForRole,
  getStoredRole,
  getStoredToken,
  isAuthorizedForRole,
  type AppRole,
} from '../utils/auth';

interface ProtectedRouteProps {
  allowedRoles: AppRole[];
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles, children }) => {
  const location = useLocation();
  const token = getStoredToken();
  const role = getStoredRole();

  if (!token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!isAuthorizedForRole(allowedRoles, role)) {
    return <Navigate to={getDefaultPathForRole(role)} replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
