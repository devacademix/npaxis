import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import paymentService, { type SubscriptionStatus } from '../services/payment';

const STATUS_CACHE_TTL_MS = 8000;
const STATUS_POLL_DELAYS_MS = [2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000];

let cachedStatus: SubscriptionStatus | null | undefined;
let cachedAt = 0;
let inFlightRequest: Promise<SubscriptionStatus | null> | null = null;

const hasFreshCache = () =>
  cachedStatus !== undefined && Date.now() - cachedAt < STATUS_CACHE_TTL_MS;

const fetchSubscriptionStatus = async (force = false): Promise<SubscriptionStatus | null> => {
  if (!force && hasFreshCache()) {
    return cachedStatus ?? null;
  }

  if (!force && inFlightRequest) {
    return inFlightRequest;
  }

  inFlightRequest = paymentService
    .getSubscriptionStatus()
    .then((result) => {
      cachedStatus = result ?? null;
      cachedAt = Date.now();
      return cachedStatus;
    })
    .finally(() => {
      inFlightRequest = null;
    });

  return inFlightRequest;
};

const wait = (delayMs: number) =>
  new Promise<void>((resolve) => {
    window.setTimeout(resolve, delayMs);
  });

export interface SubscriptionStatusCheck {
  at: number;
  accessEnabled?: boolean;
  raw?: SubscriptionStatus | null;
}

interface PollOptions {
  onCheck?: (check: SubscriptionStatusCheck) => void;
  onError?: (error: string) => void;
}

export interface PollResult {
  finalStatus: SubscriptionStatus | null;
  activated: boolean;
}

export const useSubscriptionStatus = () => {
  const [subscription, setSubscription] = useState<SubscriptionStatus | null | undefined>(() =>
    hasFreshCache() ? cachedStatus ?? null : undefined
  );
  const [isLoading, setIsLoading] = useState(() => !hasFreshCache());
  const [error, setError] = useState<string | null>(null);
  const activePollToken = useRef(0);

  const refreshStatus = useCallback(async (force = false) => {
    try {
      setError(null);
      setIsLoading(true);
      const nextStatus = await fetchSubscriptionStatus(force);
      setSubscription(nextStatus);
      return nextStatus;
    } catch (err: any) {
      const message = err?.message || 'Failed to load subscription status.';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const cancelPolling = useCallback(() => {
    activePollToken.current += 1;
  }, []);

  const pollUntilActive = useCallback(
    async (options: PollOptions = {}): Promise<PollResult> => {
      const token = activePollToken.current + 1;
      activePollToken.current = token;

      let latestStatus: SubscriptionStatus | null = null;

      for (const delayMs of STATUS_POLL_DELAYS_MS) {
        if (activePollToken.current !== token) {
          return { finalStatus: latestStatus, activated: false };
        }

        try {
          const nextStatus = await fetchSubscriptionStatus(true);
          latestStatus = nextStatus ?? null;
          setSubscription(nextStatus ?? null);
          setError(null);
          options.onCheck?.({
            at: Date.now(),
            accessEnabled: nextStatus?.accessEnabled,
            raw: nextStatus ?? null,
          });

          if (nextStatus?.accessEnabled === true) {
            return {
              finalStatus: nextStatus,
              activated: true,
            };
          }
        } catch (err: any) {
          const message = err?.message || 'Failed to refresh subscription status.';
          setError(message);
          options.onError?.(message);
        }

        await wait(delayMs);
      }

      return {
        finalStatus: latestStatus,
        activated: latestStatus?.accessEnabled === true,
      };
    },
    []
  );

  useEffect(() => {
    if (hasFreshCache()) {
      setSubscription(cachedStatus ?? null);
      setIsLoading(false);
      return;
    }

    refreshStatus().catch(() => undefined);
  }, [refreshStatus]);

  useEffect(() => () => cancelPolling(), [cancelPolling]);

  const isActive = useMemo(() => subscription?.accessEnabled === true, [subscription]);

  return {
    subscription,
    isActive,
    isLoading,
    error,
    refreshStatus,
    pollUntilActive,
    cancelPolling,
    clearStatusError: () => setError(null),
  };
};

export default useSubscriptionStatus;
