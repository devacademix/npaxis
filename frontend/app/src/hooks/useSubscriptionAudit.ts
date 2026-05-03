import { useCallback, useEffect, useState } from 'react';
import paymentService, { type SubscriptionStatus } from '../services/payment';
import type { SubscriptionStatusCheck } from './useSubscriptionStatus';

const STORAGE_KEY = 'npaxis:subscription-audit';

export type AuditState = {
  plansLoaded: boolean;
  checkoutCalled: boolean;
  redirectedToStripe: boolean;
  returnedFromStripe: boolean;
  statusChecks: Array<{ at: number; accessEnabled?: boolean; raw?: any }>;
  finalStatus?: { accessEnabled: boolean; status: string; planName?: string };
  error?: string;
};

const emptyAuditState: AuditState = {
  plansLoaded: false,
  checkoutCalled: false,
  redirectedToStripe: false,
  returnedFromStripe: false,
  statusChecks: [],
};

const readAuditState = (): AuditState => {
  if (typeof window === 'undefined') return emptyAuditState;

  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return emptyAuditState;
    return {
      ...emptyAuditState,
      ...JSON.parse(raw),
      statusChecks: Array.isArray(JSON.parse(raw)?.statusChecks) ? JSON.parse(raw).statusChecks : [],
    };
  } catch {
    return emptyAuditState;
  }
};

const normalizeFinalStatus = (status: SubscriptionStatus | null | undefined) => ({
  accessEnabled: status?.accessEnabled === true,
  status: String(status?.status ?? ''),
  ...(status?.planName ? { planName: status.planName } : {}),
});

export const useSubscriptionAudit = () => {
  const [audit, setAudit] = useState<AuditState>(readAuditState);

  useEffect(() => {
    window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(audit));
  }, [audit]);

  const patchAudit = useCallback((updater: (current: AuditState) => AuditState) => {
    setAudit((current) => updater(current));
  }, []);

  const runAudit = useCallback(async () => {
    patchAudit((current) => ({
      ...current,
      error: undefined,
    }));

    try {
      const plans = await paymentService.getSubscriptionPlans();
      patchAudit((current) => ({
        ...current,
        plansLoaded: Array.isArray(plans) && plans.length > 0,
      }));
      return plans;
    } catch (err: any) {
      patchAudit((current) => ({
        ...current,
        plansLoaded: false,
        error: err?.message || 'Failed to load subscription plans.',
      }));
      throw err;
    }
  }, [patchAudit]);

  const markCheckoutCalled = useCallback(() => {
    patchAudit((current) => ({
      ...current,
      checkoutCalled: true,
      error: undefined,
    }));
  }, [patchAudit]);

  const markRedirectedToStripe = useCallback(() => {
    patchAudit((current) => ({
      ...current,
      redirectedToStripe: true,
    }));
  }, [patchAudit]);

  const markReturnedFromStripe = useCallback(() => {
    patchAudit((current) => ({
      ...current,
      returnedFromStripe: true,
    }));
  }, [patchAudit]);

  const recordStatusCheck = useCallback((check: SubscriptionStatusCheck) => {
    patchAudit((current) => ({
      ...current,
      statusChecks: [...current.statusChecks, check],
    }));
  }, [patchAudit]);

  const setFinalStatus = useCallback((status: SubscriptionStatus | null | undefined) => {
    patchAudit((current) => ({
      ...current,
      finalStatus: normalizeFinalStatus(status),
      error: undefined,
    }));
  }, [patchAudit]);

  const setAuditError = useCallback((message: string) => {
    patchAudit((current) => ({
      ...current,
      error: message,
    }));
  }, [patchAudit]);

  const resetAudit = useCallback(() => {
    setAudit(emptyAuditState);
    window.sessionStorage.removeItem(STORAGE_KEY);
  }, []);

  return {
    audit,
    runAudit,
    markCheckoutCalled,
    markRedirectedToStripe,
    markReturnedFromStripe,
    recordStatusCheck,
    setFinalStatus,
    setAuditError,
    resetAudit,
  };
};

export default useSubscriptionAudit;
