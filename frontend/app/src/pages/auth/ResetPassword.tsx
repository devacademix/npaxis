import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../../services/auth';

const RESEND_COOLDOWN_SECONDS = 30;

const ResetPassword: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialEmail = searchParams.get('email') || '';

  const [email, setEmail] = useState(initialEmail);
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [isLoading, setIsLoading] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(0);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (resendCountdown <= 0) return;
    const timerId = window.setTimeout(() => {
      setResendCountdown((previous) => Math.max(previous - 1, 0));
    }, 1000);

    return () => {
      window.clearTimeout(timerId);
    };
  }, [resendCountdown]);

  const handleOtpChange = (index: number, value: string) => {
    if (!/^[0-9]*$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < 5) {
      const nextInput = document.getElementById(`otp-${index + 1}`);
      nextInput?.focus();
    }
  };

  const handleOtpKeyDown = (index: number, event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Backspace' && !otp[index] && index > 0) {
      const prevInput = document.getElementById(`otp-${index - 1}`);
      prevInput?.focus();
    }
  };

  const handleResendOtp = async () => {
    const trimmedEmail = email.trim();
    setError(null);
    setInfoMessage(null);

    if (!trimmedEmail) {
      setError('Please enter your email first.');
      return;
    }

    if (isResending || resendCountdown > 0) {
      return;
    }

    try {
      setIsResending(true);
      await authService.resendOtp(trimmedEmail);
      setOtp(['', '', '', '', '', '']);
      setInfoMessage('A new OTP has been sent to your email.');
      setResendCountdown(RESEND_COOLDOWN_SECONDS);
      document.getElementById('otp-0')?.focus();
    } catch (err: any) {
      setError(err?.message || 'Failed to resend OTP. Please try again.');
    } finally {
      setIsResending(false);
    }
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setInfoMessage(null);

    const otpString = otp.join('');

    if (!email.trim()) {
      setError('Please verify your email address.');
      return;
    }

    if (otpString.length !== 6) {
      setError('Please enter the complete 6-digit OTP.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }

    setIsLoading(true);

    try {
      await authService.verifyOtp(email.trim(), otpString);
      await authService.resetPassword(email.trim(), password);

      setSuccess(true);
      window.setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (err: any) {
      setError(err?.message || 'Failed to reset password. Please check OTP and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-surface font-body text-on-surface flex flex-col min-h-screen">
      <header className="w-full top-0 z-50 bg-[#f7f9fb] flex justify-between items-center px-8 py-4 max-w-full">
        <div className="text-2xl font-black text-[#003d9b] font-headline tracking-tight">NPaxis</div>
        <div className="flex items-center gap-6">
          <Link to="/support" className="text-slate-600 text-sm font-medium hover:text-[#003d9b] transition-colors">
            Support
          </Link>
        </div>
      </header>

      <main className="flex-grow flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-[480px] bg-surface-container-lowest shadow-[0px_12px_32px_rgba(25,28,30,0.06)] p-8 md:p-10 rounded-lg flex flex-col relative z-10">
          <div className="mb-8 text-center md:text-left">
            <h1 className="text-3xl font-extrabold text-primary font-headline tracking-tight mb-3">Reset Password</h1>
            <p className="text-on-surface-variant text-sm leading-relaxed">
              An OTP has been sent to your email. Enter the code and your new password below.
            </p>
          </div>

          {error ? (
            <div className="mb-6 flex items-center gap-3 p-4 rounded-lg bg-error-container text-on-error-container text-sm">
              <span className="material-symbols-outlined">error</span>
              <span>{error}</span>
            </div>
          ) : null}

          {infoMessage ? (
            <div className="mb-6 flex items-center gap-3 p-4 rounded-lg bg-blue-50 text-blue-700 text-sm">
              <span className="material-symbols-outlined">info</span>
              <span>{infoMessage}</span>
            </div>
          ) : null}

          {success ? (
            <div className="text-center space-y-6 py-4">
              <div className="bg-secondary-container/10 border border-secondary-container/20 rounded-lg p-6">
                <span className="material-symbols-outlined text-primary text-4xl mb-3">task_alt</span>
                <p className="text-on-secondary-container font-medium text-lg">Password Reset Successfully!</p>
                <p className="text-sm mt-2 text-on-surface-variant">Redirecting you to login...</p>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-2">
                <label className="block text-[0.6875rem] font-semibold text-outline uppercase tracking-[0.05em]">Confirm Email</label>
                <input
                  className="w-full h-12 px-4 bg-surface-container-low border-0 focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest rounded transition-all"
                  type="email"
                  placeholder="name@university.edu"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </div>

              <div className="space-y-2">
                <label className="block text-[0.6875rem] font-semibold text-outline uppercase tracking-[0.05em]">6-digit OTP</label>
                <div className="flex justify-between gap-2">
                  {otp.map((digit, index) => (
                    <input
                      key={index}
                      id={`otp-${index}`}
                      type="text"
                      className="w-full h-14 text-center text-xl font-bold bg-surface-container-low border-0 focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest rounded transition-all duration-200"
                      maxLength={1}
                      placeholder="*"
                      value={digit}
                      onChange={(event) => handleOtpChange(index, event.target.value)}
                      onKeyDown={(event) => handleOtpKeyDown(index, event)}
                    />
                  ))}
                </div>
                <div className="flex justify-end pt-1">
                  <button
                    type="button"
                    onClick={handleResendOtp}
                    disabled={isResending || resendCountdown > 0}
                    className="text-xs font-semibold text-primary hover:text-primary-container transition-colors disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {isResending
                      ? 'Resending OTP...'
                      : resendCountdown > 0
                        ? `Resend OTP in ${resendCountdown}s`
                        : 'Resend OTP'}
                  </button>
                </div>
              </div>

              <div className="space-y-2">
                <label className="block text-[0.6875rem] font-semibold text-outline uppercase tracking-[0.05em]">New Password</label>
                <div className="relative">
                  <input
                    className="w-full h-12 px-4 bg-surface-container-low border-0 focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest rounded transition-all duration-200 pr-12"
                    placeholder="Enter new password"
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors"
                  >
                    <span className="material-symbols-outlined">{showPassword ? 'visibility_off' : 'visibility'}</span>
                  </button>
                </div>
              </div>

              <div className="space-y-2">
                <label className="block text-[0.6875rem] font-semibold text-outline uppercase tracking-[0.05em]">Confirm New Password</label>
                <div className="relative">
                  <input
                    className="w-full h-12 px-4 bg-surface-container-low border-0 focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest rounded transition-all duration-200 pr-12"
                    placeholder="Re-enter new password"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={confirmPassword}
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors"
                  >
                    <span className="material-symbols-outlined">{showConfirmPassword ? 'visibility_off' : 'visibility'}</span>
                  </button>
                </div>
              </div>

              <div className="pt-4">
                <button
                  disabled={isLoading}
                  className="w-full h-12 rounded-full bg-gradient-to-br from-[#003d9b] to-[#0052cc] text-white font-bold tracking-tight hover:opacity-90 active:scale-[0.98] transition-all shadow-sm flex justify-center items-center gap-2 group disabled:opacity-50"
                  type="submit"
                >
                  {isLoading ? (
                    <span className="animate-spin h-5 w-5 border-2 border-white/30 border-t-white rounded-full" />
                  ) : (
                    'Reset Password'
                  )}
                </button>
              </div>
            </form>
          )}

          <div className="mt-8 text-center">
            <Link to="/login" className="inline-flex items-center gap-2 text-sm text-outline hover:text-primary transition-colors group">
              <span className="material-symbols-outlined text-[18px] transition-transform group-hover:-translate-x-1">arrow_back</span>
              Back to login
            </Link>
          </div>
        </div>
      </main>

      <footer className="w-full border-t border-slate-200 bg-[#f7f9fb] flex flex-col md:flex-row justify-between items-center px-8 py-12 mt-auto">
        <div className="font-['Inter'] text-sm tracking-wide text-slate-500 mb-6 md:mb-0">
          Copyright 2024 NPaxis Medical Systems. All rights reserved.
        </div>
        <div className="flex flex-wrap justify-center gap-6">
          <Link className="font-['Inter'] text-sm tracking-wide text-slate-500 hover:text-[#003d9b] underline transition-all duration-200" to="/privacy-policy">
            Privacy Policy
          </Link>
          <Link className="font-['Inter'] text-sm tracking-wide text-slate-500 hover:text-[#003d9b] underline transition-all duration-200" to="/terms-of-service">
            Terms of Service
          </Link>
          <Link className="font-['Inter'] text-sm tracking-wide text-slate-500 hover:text-[#003d9b] underline transition-all duration-200" to="/support">
            Contact Support
          </Link>
        </div>
      </footer>

      <div className="fixed inset-0 pointer-events-none -z-10 overflow-hidden">
        <div className="absolute -top-[10%] -right-[5%] w-[40%] h-[40%] bg-secondary-container opacity-10 blur-[120px] rounded-full" />
        <div className="absolute -bottom-[5%] -left-[5%] w-[30%] h-[30%] bg-primary opacity-5 blur-[100px] rounded-full" />
      </div>
    </div>
  );
};

export default ResetPassword;
