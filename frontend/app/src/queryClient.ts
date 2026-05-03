import { MutationCache, QueryCache, QueryClient } from '@tanstack/react-query';

const logQueryError = (context: string, error: unknown) => {
  console.error(`${context}:`, error);
};

export const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error, query) => {
      logQueryError(`Query Error [${query.queryHash}]`, error);
    },
  }),
  mutationCache: new MutationCache({
    onError: (error, _variables, _context, mutation) => {
      logQueryError(`Mutation Error [${mutation.options.mutationKey?.join(':') || 'unknown'}]`, error);
    },
  }),
  defaultOptions: {
    queries: {
      retry: 2,
      staleTime: 10_000,
      refetchOnWindowFocus: false,
    },
  },
});

export default queryClient;
