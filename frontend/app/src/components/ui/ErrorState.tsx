import React from 'react';

interface ErrorStateProps {
  title?: string;
  message: string;
  retryLabel?: string;
  onRetry?: () => void;
}

const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'Something went wrong',
  message,
  retryLabel = 'Retry',
  onRetry,
}) => {
  return (
    <div className="rounded-3xl border border-red-200 bg-red-50 px-5 py-6">
      <p className="text-base font-bold text-red-800">{title}</p>
      <p className="mt-2 text-sm font-medium text-red-700">{message}</p>
      {onRetry ? (
        <button
          type="button"
          onClick={onRetry}
          className="mt-4 inline-flex items-center justify-center rounded-full border border-red-200 bg-white px-5 py-2.5 text-sm font-semibold text-red-700 hover:bg-red-100"
        >
          {retryLabel}
        </button>
      ) : null}
    </div>
  );
};

export default ErrorState;
