import { useCallback, useMemo, useRef } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import paymentService, { type SubscriptionStatus } from '../services/payment';

const STATUS_POLL_DELAYS_MS = [2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000];
const subscriptionStatusQueryKey = ['subscription', 'status'] as const;

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
  const queryClient = useQueryClient();
  const activePollToken = useRef(0);

  const query = useQuery<SubscriptionStatus | null, Error>({
    queryKey: subscriptionStatusQueryKey,
    queryFn: () => paymentService.getSubscriptionStatus(),
    staleTime: 8_000,
    refetchInterval: (queryState) => {
      const data = queryState.state.data;
      return data?.accessEnabled === true ? false : 3_000;
    },
    placeholderData: (previousData) => previousData,
  });

  const refreshStatus = useCallback(
    async (force = false) => {
      const nextStatus = await query.refetch();
      if (force) {
        await queryClient.invalidateQueries({ queryKey: subscriptionStatusQueryKey });
      }
      return nextStatus.data ?? null;
    },
    [query, queryClient]
  );

  const cancelPolling = useCallback(() => {
    activePollToken.current += 1;
  }, []);

  const pollUntilActive = useCallback(
    async (options: PollOptions = {}): Promise<PollResult> => {
      const token = activePollToken.current + 1;
      activePollToken.current = token;

      let latestStatus = query.data ?? null;

      for (const delayMs of STATUS_POLL_DELAYS_MS) {
        if (activePollToken.current !== token) {
          return { finalStatus: latestStatus, activated: false };
        }

        try {
          const nextStatus = await queryClient.fetchQuery({
            queryKey: subscriptionStatusQueryKey,
            queryFn: () => paymentService.getSubscriptionStatus(),
            staleTime: 0,
          });

          latestStatus = nextStatus ?? null;
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
          options.onError?.(message);
        }

        await wait(delayMs);
      }

      return {
        finalStatus: latestStatus,
        activated: latestStatus?.accessEnabled === true,
      };
    },
    [query.data, queryClient]
  );

  const clearStatusError = useCallback(() => {
    queryClient.resetQueries({ queryKey: subscriptionStatusQueryKey, exact: true });
  }, [queryClient]);

  const isActive = useMemo(() => query.data?.accessEnabled === true, [query.data]);

  return {
    subscription: query.data,
    isActive,
    isLoading: query.isLoading,
    error: query.error?.message || null,
    refreshStatus,
    pollUntilActive,
    cancelPolling,
    clearStatusError,
  };
};

export default useSubscriptionStatus;
