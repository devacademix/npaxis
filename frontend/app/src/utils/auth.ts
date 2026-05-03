export type AppRole = 'ADMIN' | 'PRECEPTOR' | 'STUDENT';

export const getStoredToken = (): string => localStorage.getItem('accessToken') || '';

export const normalizeRole = (role: string | null | undefined): AppRole | null => {
  if (!role) return null;
  const normalized = role.toUpperCase().replace(/^ROLE_/, '');
  if (normalized === 'ADMIN') return 'ADMIN';
  if (normalized === 'PRECEPTOR') return 'PRECEPTOR';
  if (normalized === 'STUDENT') return 'STUDENT';
  return null;
};

export const getStoredRole = (): AppRole | null => normalizeRole(localStorage.getItem('role'));

export const getDefaultPathForRole = (role: AppRole | null): string => {
  if (role === 'ADMIN') return '/admin/dashboard';
  if (role === 'PRECEPTOR') return '/preceptor/dashboard';
  if (role === 'STUDENT') return '/student/dashboard';
  return '/login';
};

export const isAuthorizedForRole = (allowedRoles: AppRole[], role: AppRole | null): boolean => {
  if (!role) return false;
  return allowedRoles.includes(role);
};
