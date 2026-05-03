import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/auth';
import type { UserResponse } from '../types';
import { getDefaultPathForRole, getStoredRole, getStoredToken, normalizeRole, type AppRole } from '../utils/auth';

interface SessionContextValue {
  currentUser: UserResponse | null;
  role: AppRole | null;
  dashboardPath: string;
  isLoading: boolean;
}

const SessionContext = createContext<SessionContextValue>({
  currentUser: null,
  role: null,
  dashboardPath: '/login',
  isLoading: true,
});

export const SessionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null);
  const [role, setRole] = useState<AppRole | null>(getStoredRole());
  const [isLoading, setIsLoading] = useState(Boolean(getStoredToken()));

  useEffect(() => {
    let isCancelled = false;

    const syncSession = async () => {
      const token = getStoredToken();
      if (!token) {
        setCurrentUser(null);
        setRole(null);
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        const user = await authService.getCurrentUserCached();
        if (isCancelled) return;
        setCurrentUser(user);
        setRole(normalizeRole(user.role) ?? getStoredRole());
      } catch {
        if (isCancelled) return;
        setCurrentUser(null);
        setRole(getStoredRole());
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    const handleSessionChanged = () => {
      syncSession();
    };

    syncSession();
    window.addEventListener('storage', handleSessionChanged);
    window.addEventListener('npaxis:session-changed', handleSessionChanged);

    return () => {
      isCancelled = true;
      window.removeEventListener('storage', handleSessionChanged);
      window.removeEventListener('npaxis:session-changed', handleSessionChanged);
    };
  }, []);

  const value = useMemo<SessionContextValue>(() => {
    return {
      currentUser,
      role,
      dashboardPath: getDefaultPathForRole(role),
      isLoading,
    };
  }, [currentUser, isLoading, role]);

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
};

export const useSession = () => useContext(SessionContext);
