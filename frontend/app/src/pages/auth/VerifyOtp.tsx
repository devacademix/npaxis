import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../../services/auth';
import type { AuthResponse } from '../../types';

const RESEND_COOLDOWN_SECONDS = 30;

const VerifyOtp: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialEmail = searchParams.get('email') || '';

  const [email, setEmail] = useState(initialEmail);
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (resendCountdown <= 0) return;

    const timerId = window.setTimeout(() => {
      setResendCountdown((previous) => Math.max(previous - 1, 0));
    }, 1000);

    return () => {
      window.clearTimeout(timerId);
    };
  }, [resendCountdown]);

  const handleOtpChange = (value: string, index: number) => {
    if (!/^\d?$/.test(value)) return;
    const next = [...otp];
    next[index] = value;
    setOtp(next);

    if (value && index < 5) {
      const nextInput = document.getElementById(`verify-otp-${index + 1}`);
      nextInput?.focus();
    }
  };

  const handleOtpKeyDown = (event: React.KeyboardEvent<HTMLInputElement>, index: number) => {
    if (event.key === 'Backspace' && !otp[index] && index > 0) {
      const prevInput = document.getElementById(`verify-otp-${index - 1}`);
      prevInput?.focus();
    }
  };

  const redirectByRole = (role: AuthResponse['role']) => {
    switch (role) {
      case 'ROLE_ADMIN':
      case 'ADMIN':
        navigate('/admin');
        return;
      case 'ROLE_PRECEPTOR':
      case 'PRECEPTOR':
        navigate('/preceptor/dashboard');
        return;
      case 'ROLE_STUDENT':
      case 'STUDENT':
        navigate('/student');
        return;
      default:
        navigate('/login');
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setInfoMessage(null);

    const otpValue = otp.join('');
    if (!email.trim()) {
      setError('Email is required.');
      return;
    }
    if (otpValue.length !== 6) {
      setError('Please enter the complete 6-digit OTP.');
      return;
    }

    try {
      setIsLoading(true);
      const response = await authService.verifyOtp(email.trim(), otpValue);

      if (!response?.accessToken || response.userId == null || !response.role) {
        throw new Error('OTP verified but invalid login response received.');
      }

      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('role', response.role);
      localStorage.setItem('userId', String(response.userId));
      localStorage.setItem('displayName', response.displayName || '');

      redirectByRole(response.role);
    } catch (err: any) {
      setError(err.message || 'OTP verification failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendOtp = async () => {
    const sanitizedEmail = email.trim();
    setError(null);
    setInfoMessage(null);

    if (!sanitizedEmail) {
      setError('Please enter your email before requesting a new OTP.');
      return;
    }

    if (isResending || resendCountdown > 0) {
      return;
    }

    try {
      setIsResending(true);
      await authService.resendOtp(sanitizedEmail);
      setOtp(['', '', '', '', '', '']);
      setInfoMessage('A new OTP has been sent to your email.');
      setResendCountdown(RESEND_COOLDOWN_SECONDS);
      const firstInput = document.getElementById('verify-otp-0');
      firstInput?.focus();
    } catch (err: any) {
      setError(err.message || 'Failed to resend OTP. Please try again.');
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-background px-4 py-10 font-body text-on-background">
      <div className="mx-auto w-full max-w-md">
        <div className="rounded-xl border border-outline-variant/20 bg-surface-container-lowest p-8 shadow-[0px_12px_32px_rgba(25,28,30,0.06)]">
          <div className="mb-6 text-center">
            <h1 className="text-2xl font-extrabold tracking-tight text-primary font-headline">Verify Your Account</h1>
            <p className="mt-2 text-sm text-on-surface-variant">Enter the OTP sent to your email to activate your account.</p>
          </div>

          {error ? (
            <div className="mb-5 flex items-center gap-2 rounded-lg bg-error-container p-3 text-sm text-on-error-container">
              <span className="material-symbols-outlined text-base">error</span>
              <span>{error}</span>
            </div>
          ) : null}
          {infoMessage ? (
            <div className="mb-5 flex items-center gap-2 rounded-lg bg-secondary-container/20 p-3 text-sm text-on-secondary-container">
              <span className="material-symbols-outlined text-base">info</span>
              <span>{infoMessage}</span>
            </div>
          ) : null}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold uppercase tracking-wider text-on-surface-variant">Email Address</label>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                className="w-full rounded-lg bg-surface-container-low px-4 py-3 focus:ring-2 focus:ring-primary"
                placeholder="name@example.com"
                required
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold uppercase tracking-wider text-on-surface-variant">6-digit OTP</label>
              <div className="flex justify-between gap-2">
                {otp.map((digit, index) => (
                  <input
                    key={index}
                    id={`verify-otp-${index}`}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    value={digit}
                    onChange={(event) => handleOtpChange(event.target.value, index)}
                    onKeyDown={(event) => handleOtpKeyDown(event, index)}
                    className="h-12 w-12 rounded-lg bg-surface-container-low text-center text-lg font-bold focus:ring-2 focus:ring-primary"
                    required
                  />
                ))}
              </div>
              <div className="flex justify-end pt-1">
                <button
                  type="button"
                  onClick={handleResendOtp}
                  disabled={isResending || resendCountdown > 0}
                  className="text-xs font-semibold text-primary transition-colors hover:text-primary/80 disabled:cursor-not-allowed disabled:text-on-surface-variant"
                >
                  {isResending
                    ? 'Resending OTP...'
                    : resendCountdown > 0
                      ? `Resend OTP in ${resendCountdown}s`
                      : 'Resend OTP'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="flex w-full items-center justify-center gap-2 rounded-full bg-primary py-3 font-bold text-on-primary transition-opacity hover:opacity-90 disabled:opacity-60"
            >
              {isLoading ? (
                <span className="h-5 w-5 animate-spin rounded-full border-2 border-white/30 border-t-white" />
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">verified_user</span>
                  Verify OTP
                </>
              )}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-on-surface-variant">
            Wrong email?
            <Link to="/register" className="ml-1 font-semibold text-primary hover:underline">
              Go back to register
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default VerifyOtp;
