import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { authService } from '../../services/auth';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await authService.forgotPassword(email);
      setSuccess(true);
    } catch (err: any) {
      setError(err.message || 'Failed to initiate password reset.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-background font-body text-on-surface flex flex-col min-h-screen">
      <main className="flex-grow flex items-center justify-center px-4 py-20 relative overflow-hidden">
        {/* Abstract Brand Element */}
        <div className="absolute top-0 left-0 w-full h-full pointer-events-none opacity-20">
          <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-secondary-container blur-[100px]"></div>
          <div className="absolute bottom-0 right-0 w-[500px] h-[500px] rounded-full bg-primary-fixed blur-[120px]"></div>
        </div>

        <div className="relative z-10 w-full max-w-md">
          <div className="flex justify-center mb-10">
            <span className="font-headline text-3xl font-black text-primary tracking-tight">NPaxis</span>
          </div>

          <div className="bg-surface-container-lowest rounded-lg p-10 shadow-[0px_12px_32px_rgba(25,28,30,0.06)] border border-outline-variant/10">
            <div className="text-center mb-8">
              <h1 className="font-headline text-2xl font-bold text-on-surface tracking-tight mb-3">Forgot Password?</h1>
              <p className="text-on-surface-variant text-sm leading-relaxed">
                Enter your email address and we'll send you an OTP code to reset your password.
              </p>
            </div>

            {error && (
              <div className="mb-6 p-4 rounded-lg bg-error-container text-on-error-container text-sm flex items-center gap-3">
                <span className="material-symbols-outlined">error</span>
                <span>{error}</span>
              </div>
            )}

            {success ? (
              <div className="text-center space-y-6">
                <div className="bg-secondary-container/10 border border-secondary-container/20 rounded-lg p-6">
                  <span className="material-symbols-outlined text-primary text-4xl mb-3">check_circle</span>
                  <p className="text-on-secondary-container font-medium">OTP code sent to your email.</p>
                </div>
                <Link
                  to={`/reset-password?email=${encodeURIComponent(email)}`}
                  className="block w-full btn-gradient text-on-primary font-semibold py-4 rounded-full shadow-lg hover:opacity-95 transition-all"
                >
                  Proceed to Reset Password
                </Link>
              </div>
            ) : (
              <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                  <label className="block text-xs font-semibold uppercase tracking-wider text-outline mb-2 ml-1" htmlFor="email">Email Address</label>
                  <div className="relative group">
                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-outline group-focus-within:text-primary transition-colors">
                      <span className="material-symbols-outlined text-[20px]">mail</span>
                    </div>
                    <input
                      className="block w-full pl-12 pr-4 py-3.5 bg-surface-container-low border-0 rounded-lg text-on-surface placeholder:text-outline/50 focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all"
                      id="email"
                      type="email"
                      placeholder="name@university.edu"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </div>
                </div>
                <button
                  disabled={isLoading}
                  className="w-full btn-gradient text-on-primary font-semibold py-4 rounded-full shadow-lg hover:opacity-90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 group disabled:opacity-50"
                  type="submit"
                >
                  {isLoading ? (
                    <span className="animate-spin h-5 w-5 border-2 border-on-primary/30 border-t-on-primary rounded-full"></span>
                  ) : (
                    <>
                      <span>Send OTP code</span>
                      <span className="material-symbols-outlined text-[18px] group-hover:translate-x-1 transition-transform">arrow_forward</span>
                    </>
                  )}
                </button>
              </form>
            )}

            <div className="mt-8 text-center">
              <Link to="/login" className="inline-flex items-center gap-2 text-sm font-medium text-primary hover:text-primary-container transition-colors group">
                <span className="material-symbols-outlined text-[18px]">arrow_back</span>
                <span>Back to Login</span>
              </Link>
            </div>
          </div>

          <div className="mt-8 flex justify-center items-center gap-4">
            <div className="flex items-center gap-2 px-3 py-1 bg-surface-container-high rounded-full">
              <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
              <span className="text-[10px] font-bold uppercase tracking-widest text-on-secondary-fixed-variant">Secure Server Active</span>
            </div>
            <div className="text-[10px] font-medium uppercase tracking-widest text-outline">Clinical Precision v2.0</div>
          </div>
        </div>
      </main>

      <footer className="w-full border-t border-slate-200 bg-[#f7f9fb] flex flex-col md:flex-row justify-between items-center px-8 py-12 mt-auto">
        <p className="font-['Inter'] text-sm tracking-wide text-slate-500">© 2024 NPaxis Medical Systems. All rights reserved.</p>
        <div className="flex flex-wrap justify-center gap-x-8 gap-y-4">
          <Link className="font-['Inter'] text-sm tracking-wide text-slate-500 hover:text-[#003d9b] underline transition-all duration-200" to="/privacy-policy">
            Privacy Policy
          </Link>
          <Link className="font-['Inter'] text-sm tracking-wide text-slate-500 hover:text-[#003d9b] underline transition-all duration-200" to="/terms-of-service">
            Terms of Service
          </Link>
          <Link className="font-['Inter'] text-sm tracking-wide text-slate-500 hover:text-[#003d9b] underline transition-all duration-200" to="/support">
            Support
          </Link>
        </div>
      </footer>
    </div>
  );
};

export default ForgotPassword;
