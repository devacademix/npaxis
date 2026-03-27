import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/auth';
import type { AuthResponse } from '../../types';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    // Basic Validation
    if (!email || !password) {
      setError('Email and password are required');
      setIsLoading(false);
      return;
    }

    try {
      const response: AuthResponse = await authService.login(email, password);
      if (!response || response.userId == null || !response.accessToken || !response.role) {
        throw new Error('Invalid login response received from server.');
      }
      
      // Store credentials
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('role', response.role);
      localStorage.setItem('userId', String(response.userId));
      localStorage.setItem('displayName', response.displayName);

      // Redirect based on role
      switch (response.role) {
        case 'ROLE_ADMIN':
        case 'ADMIN':
          navigate('/admin');
          break;
        case 'ROLE_PRECEPTOR':
        case 'PRECEPTOR':
          navigate('/preceptor/dashboard');
          break;
        case 'ROLE_STUDENT':
        case 'STUDENT':
          navigate('/student');
          break;
        default:
          navigate('/');
      }
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-background font-body text-on-background min-h-screen flex flex-col">
      <header className="w-full top-0 z-50 bg-[#f7f9fb] flex justify-between items-center px-8 py-4 max-w-full">
        <div className="text-2xl font-black text-[#003d9b] font-headline tracking-tight">
          NPaxis
        </div>
        <div className="flex items-center gap-6 text-sm">
          <Link to="/support" className="text-slate-600 font-medium hover:text-[#0052cc] transition-colors">
            Support
          </Link>
          <Link to="/register" className="text-on-primary bg-primary-container px-5 py-2 rounded-full font-semibold hover:opacity-90 transition-all">Sign Up</Link>
        </div>
      </header>

      <main className="flex-grow flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-md">
          <div className="bg-surface-container-lowest rounded-lg border border-outline-variant/20 shadow-[0px_12px_32px_rgba(25,28,30,0.06)] p-10">
            <div className="text-center mb-8">
              <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-primary-container/10 mb-4">
                <span className="material-symbols-outlined text-primary text-3xl">medical_services</span>
              </div>
              <h1 className="font-headline text-2xl font-extrabold text-primary tracking-tight mb-2">NPaxis</h1>
              <p className="text-on-surface-variant text-sm font-medium">Connect Students with Verified Preceptors</p>
            </div>

            {error && (
              <div className="mb-6 flex items-center gap-3 p-4 rounded-lg bg-error-container text-on-error-container text-sm">
                <span className="material-symbols-outlined">error</span>
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleLogin} className="space-y-5">
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider" htmlFor="email">Email Address</label>
                <div className="relative group">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">mail</span>
                  <input
                    className="w-full pl-10 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all text-on-surface placeholder-outline/50"
                    id="email"
                    type="email"
                    placeholder="name@university.edu"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <div className="flex justify-between items-center">
                  <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider" htmlFor="password">Password</label>
                  <Link to="/forgot-password" title="reset password"  className="text-xs font-semibold text-primary hover:underline" >Forgot password?</Link>
                </div>
                <div className="relative group">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 material-symbols-outlined text-outline">lock</span>
                  <input
                    className="w-full pl-10 pr-12 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all text-on-surface placeholder-outline/50"
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
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

              <button
                disabled={isLoading}
                className="w-full btn-gradient text-on-primary py-3.5 rounded-full font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 group disabled:opacity-50"
                type="submit"
              >
                {isLoading ? (
                  <span className="animate-spin h-5 w-5 border-2 border-on-primary/30 border-t-on-primary rounded-full"></span>
                ) : (
                  <>
                    <span>Login</span>
                    <span className="material-symbols-outlined group-hover:translate-x-1 transition-transform">arrow_forward</span>
                  </>
                )}
              </button>
            </form>

            <div className="mt-8 text-center">
              <p className="text-sm text-on-surface-variant">
                New to NPaxis?
                <Link to="/register" className="text-primary font-bold hover:underline ml-1">Register as a new user</Link>
              </p>
            </div>
          </div>
        </div>
      </main>

      <footer className="w-full border-t border-slate-200 bg-[#f7f9fb] flex flex-col md:flex-row justify-between items-center px-8 py-12 mt-auto">
        <p className="text-slate-500 text-sm tracking-wide mb-4 md:mb-0">© 2024 NPaxis Medical Systems. All rights reserved.</p>
        <div className="flex flex-wrap justify-center gap-8">
          <Link className="text-slate-500 text-sm hover:text-[#003d9b] underline transition-all" to="/privacy-policy">
            Privacy Policy
          </Link>
          <Link className="text-slate-500 text-sm hover:text-[#003d9b] underline transition-all" to="/terms-of-service">
            Terms of Service
          </Link>
          <Link className="text-slate-500 text-sm hover:text-[#003d9b] underline transition-all" to="/support">
            Contact Support
          </Link>
        </div>
      </footer>
    </div>
  );
};

export default Login;
