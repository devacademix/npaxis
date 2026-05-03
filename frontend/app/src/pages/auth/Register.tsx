import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../../services/auth';

const CREDENTIAL_OPTIONS = [
  'MBBS', 'MD', 'DO', 'PhD', 'DNP', 'PA-C', 'NP', 'MSN', 'RN', 'DDS', 'DVM', 'MPH', 'DPT', 'BCPS', 'MBA',
].map((value) => ({ label: value, value }));

const SPECIALTY_OPTIONS = [
  'Cardiology',
  'Internal Medicine',
  'Pediatrics',
  'Surgery',
  'Orthopedic Surgery',
  'Neurology',
  'Psychiatry',
  'Obstetrics & Gynecology',
  'Dermatology',
  'Radiology',
  'Pathology',
  'Anesthesiology',
  'Emergency Medicine',
  'Family Medicine',
  'Oncology',
  'Pulmonology',
  'Gastroenterology',
  'Nephrology',
  'Endocrinology',
  'Rheumatology',
  'Infectious Diseases',
  'Immunology',
  'Hematology',
  'Ophthalmology',
  'Otolaryngology',
  'Urology',
  'Neurosurgery',
  'Psychiatry & Psychology',
  'Nursing',
  'Physical Therapy',
].map((value) => ({ label: value, value }));

const COUNTRY_CODE_OPTIONS = [
  { label: 'India (+91)', value: '+91' },
  { label: 'United States (+1)', value: '+1' },
  { label: 'United Kingdom (+44)', value: '+44' },
  { label: 'Canada (+1)', value: '+1' },
  { label: 'Australia (+61)', value: '+61' },
];

const Register: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'student' | 'preceptor'>('student');

  // Form States
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [countryCode, setCountryCode] = useState('+91');
  const [phoneNumber, setPhoneNumber] = useState('');

  // Student Specific
  const [university, setUniversity] = useState('');
  const [program, setProgram] = useState('');
  const [graduationYear, setGraduationYear] = useState('');

  // Preceptor Specific
  const [credential, setCredential] = useState('');
  const [specialty, setSpecialty] = useState('');
  const [location, setLocation] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const normalizedPhone = phoneNumber.replace(/[^\d]/g, '');
      if (!normalizedPhone) {
        throw new Error('Phone number is required.');
      }

      const formattedPhone = `${countryCode} ${normalizedPhone}`;

      if (activeTab === 'student') {
        const payload = {
          roleId: 1, // 1: Student
          displayName: fullName,
          email,
          password,
          university,
          program,
          graduationYear,
          phone: formattedPhone
        };
        await authService.register(payload);
      } else {
        if (!specialty) {
          throw new Error('Please select at least one specialty.');
        }

        const payload = {
          roleId: 2, // 2: Preceptor
          displayName: fullName,
          email,
          password,
          credentials: credential ? [credential] : [],
          specialties: [specialty],
          location,
          phone: formattedPhone
        };
        await authService.register(payload);
      }

      setSuccess('Registration successful. Please verify the OTP sent to your email.');
      navigate(`/verify-otp?email=${encodeURIComponent(email)}`);
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-surface font-body text-on-surface min-h-screen">
      <nav className="w-full sticky top-0 z-50 bg-slate-50">
        <div className="flex justify-between items-center px-6 py-4 max-w-7xl mx-auto">
          <div className="text-2xl font-black text-blue-800 tracking-tighter font-headline">NPaxis</div>
          <div className="hidden md:flex gap-8 items-center">
            <Link to="/support" className="font-headline font-bold tracking-tight text-slate-600 hover:text-blue-600 transition-colors">
              Support
            </Link>
            <Link to="/login" className="font-headline font-bold tracking-tight text-slate-600 hover:text-blue-600 transition-colors">Login</Link>
            <Link
              to="/register"
              className="bg-primary text-on-primary px-6 py-2 rounded-full font-headline font-bold tracking-tight hover:opacity-90 transition-all btn-gradient"
            >
              Register
            </Link>
          </div>
        </div>
      </nav>

      <main className="min-h-[calc(100vh-160px)] flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-2xl">
          <div className="bg-surface-container-lowest rounded-xl overflow-hidden shadow-[0px_12px_32px_rgba(25,28,30,0.06)] border border-outline-variant/10">
            <div className="p-8 md:p-12">
              <header className="mb-10 text-center">
                <h1 className="font-headline text-4xl font-extrabold tracking-tight text-primary mb-3">Join the Npaxis </h1>
                <p className="text-on-surface-variant font-body">Select your role to begin your clinical journey.</p>
              </header>

              <div className="flex p-1 bg-surface-container-low rounded-lg mb-10">
                <button
                  type="button"
                  onClick={() => { setActiveTab('student'); setError(null); }}
                  className={`flex-1 py-3 px-4 rounded-md text-sm font-bold font-headline transition-all ${activeTab === 'student' ? 'bg-surface-container-lowest text-primary shadow-sm' : 'text-on-surface-variant hover:text-primary'
                    }`}
                >
                  Student
                </button>
                <button
                  type="button"
                  onClick={() => { setActiveTab('preceptor'); setError(null); }}
                  className={`flex-1 py-3 px-4 rounded-md text-sm font-bold font-headline transition-all ${activeTab === 'preceptor' ? 'bg-surface-container-lowest text-primary shadow-sm' : 'text-on-surface-variant hover:text-primary'
                    }`}
                >
                  Preceptor
                </button>
              </div>

              {error && (
                <div className="mb-6 flex items-center gap-3 p-4 rounded-lg bg-error-container text-on-error-container text-sm">
                  <span className="material-symbols-outlined">error</span>
                  <span>{error}</span>
                </div>
              )}

              {success && (
                <div className="mb-6 flex items-center gap-3 rounded-lg bg-emerald-50 p-4 text-sm text-emerald-700">
                  <span className="material-symbols-outlined">check_circle</span>
                  <span>{success}</span>
                </div>
              )}

              <form onSubmit={handleRegister} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

                  {/* Common Fields */}
                  <div className="space-y-2">
                    <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Full Name</label>
                    <div className="relative">
                      <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">person</span>
                      <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="John Doe" type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Email Address</label>
                    <div className="relative">
                      <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">mail</span>
                      <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="john@domain.com" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Phone Number</label>
                    <div className="grid grid-cols-[140px,1fr] gap-3">
                      <select
                        value={countryCode}
                        onChange={(e) => setCountryCode(e.target.value)}
                        className="w-full rounded-lg bg-surface-container-low px-3 py-3 text-sm text-slate-900 focus:ring-2 focus:ring-primary"
                        required
                      >
                        {COUNTRY_CODE_OPTIONS.map((option) => (
                          <option key={`${option.label}-${option.value}`} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                      <div className="relative">
                        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">call</span>
                        <input
                          className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant"
                          placeholder="9876543210"
                          type="tel"
                          value={phoneNumber}
                          onChange={(e) => setPhoneNumber(e.target.value)}
                          required
                        />
                      </div>
                    </div>
                    <p className="text-xs text-slate-500">Selecting a country code and entering a phone number are both required.</p>
                  </div>

                  {activeTab === 'student' ? (
                    <>
                      <div className="space-y-2 md:col-span-2">
                        <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">University</label>
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">school</span>
                          <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="State Health University" type="text" value={university} onChange={(e) => setUniversity(e.target.value)} required />
                        </div>
                      </div>

                      <div className="space-y-2">
                        <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Program</label>
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">clinical_notes</span>
                          <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="FNP Program" type="text" value={program} onChange={(e) => setProgram(e.target.value)} required />
                        </div>
                      </div>

                      <div className="space-y-2">
                        <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Graduation Year</label>
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">event_available</span>
                          <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="2025" type="number" value={graduationYear} onChange={(e) => setGraduationYear(e.target.value)} required />
                        </div>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="space-y-2">
                        <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Credentials</label>
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">verified_user</span>
                          <select
                            className="w-full appearance-none pl-12 pr-10 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all text-slate-900"
                            value={credential}
                            onChange={(e) => setCredential(e.target.value)}
                            disabled={isLoading}
                          >
                            <option value="">Select credential</option>
                            {CREDENTIAL_OPTIONS.map((option) => (
                              <option key={option.value} value={option.value}>
                                {option.label}
                              </option>
                            ))}
                          </select>
                          <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-outline text-xl pointer-events-none">expand_more</span>
                        </div>
                      </div>

                      <div className="space-y-2">
                        <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Specialty</label>
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">medical_services</span>
                          <select
                            className="w-full appearance-none pl-12 pr-10 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all text-slate-900"
                            value={specialty}
                            onChange={(e) => setSpecialty(e.target.value)}
                            required
                            disabled={isLoading}
                          >
                            <option value="">Select specialty</option>
                            {SPECIALTY_OPTIONS.map((option) => (
                              <option key={option.value} value={option.value}>
                                {option.label}
                              </option>
                            ))}
                          </select>
                          <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 text-outline text-xl pointer-events-none">expand_more</span>
                        </div>
                      </div>

                      <div className="space-y-2 md:col-span-2">
                        <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Location</label>
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">location_on</span>
                          <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="City, State" type="text" value={location} onChange={(e) => setLocation(e.target.value)} required />
                        </div>
                      </div>
                    </>
                  )}

                  {/* Password is same for both */}
                  <div className="space-y-2 md:col-span-2">
                    <label className="block text-[0.6875rem] font-bold tracking-[0.05em] uppercase text-outline">Password</label>
                    <div className="relative">
                      <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline text-xl">lock</span>
                      <input className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-lg focus:ring-2 focus:ring-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant" placeholder="••••••••" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} />
                    </div>
                  </div>

                </div>

                <div className="pt-6">
                  <button
                    disabled={isLoading}
                    className="w-full btn-gradient text-on-primary py-4 rounded-full font-headline font-extrabold text-lg shadow-lg hover:shadow-xl hover:opacity-95 transition-all flex justify-center items-center gap-2 disabled:opacity-50"
                    type="submit"
                  >
                    {isLoading ? (
                      <span className="animate-spin h-5 w-5 border-2 border-white/30 border-t-white rounded-full"></span>
                    ) : (
                      "Create Account"
                    )}
                  </button>
                </div>

                <div className="bg-secondary-container/10 border border-secondary-container/20 rounded-lg p-4 flex gap-3">
                  <span className="material-symbols-outlined text-primary">info</span>
                  <p className="text-sm text-on-secondary-container font-medium">A verification code will be sent to your email after submission.</p>
                </div>
              </form>

              <footer className="mt-8 text-center">
                <p className="text-on-surface-variant">
                  Already have an account?
                  <Link to="/login" className="text-primary font-bold hover:underline underline-offset-4 ml-1">Login here</Link>
                </p>
              </footer>
            </div>
          </div>
        </div>
      </main>

      <footer className="w-full border-t border-slate-200/20 bg-slate-100">
        <div className="flex flex-col md:flex-row justify-between items-center px-8 py-12 gap-6 w-full max-w-7xl mx-auto">
          <div className="text-lg font-bold text-slate-900">NPaxis</div>
          <div className="flex flex-wrap justify-center gap-8">
            <Link className="font-label text-sm tracking-wide uppercase text-slate-500 hover:text-blue-600 transition-colors" to="/privacy-policy">
              Privacy Policy
            </Link>
            <Link className="font-label text-sm tracking-wide uppercase text-slate-500 hover:text-blue-600 transition-colors" to="/terms-of-service">
              Terms of Service
            </Link>
            <Link className="font-label text-sm tracking-wide uppercase text-slate-500 hover:text-blue-600 transition-colors" to="/support">
              Contact
            </Link>
          </div>
          <p className="text-slate-500 font-label text-[10px] tracking-widest uppercase">© 2024 NPaxis Clinical Atelier. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default Register;
