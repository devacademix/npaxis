import { Suspense, lazy, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { SessionProvider } from './context/SessionContext';
import { authService } from './services/auth';
import ProtectedRoute from './routes/ProtectedRoute';

const Login = lazy(() => import('./pages/auth/Login'));
const ForgotPassword = lazy(() => import('./pages/auth/ForgotPassword'));
const ResetPassword = lazy(() => import('./pages/auth/ResetPassword'));
const Register = lazy(() => import('./pages/auth/Register'));
const VerifyOtp = lazy(() => import('./pages/auth/VerifyOtp'));
const AdminDashboard = lazy(() => import('./pages/admin/Dashboard'));
const AdminManagement = lazy(() => import('./pages/admin/Management'));
const StudentManagement = lazy(() => import('./pages/admin/StudentManagement'));
const PreceptorManagement = lazy(() => import('./pages/admin/PreceptorManagement'));
const RoleManagement = lazy(() => import('./pages/admin/RoleManagement'));
const SystemInitialization = lazy(() => import('./pages/admin/SystemInitialization'));
const PendingPreceptors = lazy(() => import('./pages/admin/PendingPreceptors'));
const AdminUsers = lazy(() => import('./pages/admin/Users'));
const AdminRevenue = lazy(() => import('./pages/admin/Revenue'));
const WebhookMonitoring = lazy(() => import('./pages/admin/WebhookMonitoring'));
const AdminSettings = lazy(() => import('./pages/admin/Settings'));
const AddAdmin = lazy(() => import('./pages/admin/AddAdmin'));
const PreceptorDashboard = lazy(() => import('./pages/preceptor/Dashboard'));
const PreceptorProfile = lazy(() => import('./pages/preceptor/Profile'));
const PreceptorLicense = lazy(() => import('./pages/preceptor/License'));
const PreceptorSubscription = lazy(() => import('./pages/preceptor/Subscription'));
const PreceptorBilling = lazy(() => import('./pages/preceptor/Billing'));
const StudentDashboard = lazy(() => import('./pages/student/Dashboard'));
const StudentBrowse = lazy(() => import('./pages/student/Browse'));
const StudentSaved = lazy(() => import('./pages/student/Saved'));
const StudentProfile = lazy(() => import('./pages/student/Profile'));
const StudentPreceptorDetail = lazy(() => import('./pages/student/PreceptorDetail'));
const StudentInquiry = lazy(() => import('./pages/student/Inquiry'));
const StudentInquiries = lazy(() => import('./pages/student/Inquiries'));
const PreceptorInquiries = lazy(() => import('./pages/preceptor/Inquiries'));
const InfoPage = lazy(() => import('./pages/common/InfoPage'));
const Landing = lazy(() => import('./pages/common/Landing'));
const Contact = lazy(() => import('./pages/common/Contact'));
const PublicBrowse = lazy(() => import('./pages/common/PublicBrowse'));
const About = lazy(() => import('./pages/common/About'));
const StripeReturn = lazy(() => import('./pages/common/StripeReturn'));

const RouteLoader = () => (
  <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
    <div className="flex items-center gap-3 rounded-full bg-white px-5 py-3 text-sm font-semibold text-slate-700 shadow-sm ring-1 ring-slate-200">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
      Loading page...
    </div>
  </div>
);

function App() {
  useEffect(() => {
    const refreshSession = async () => {
      if (!localStorage.getItem('accessToken')) return;
      try {
        await authService.refreshSession();
      } catch (err) {
        console.warn('Session refresh failed, ignoring:', err);
      }
    };

    refreshSession();
    const intervalId = window.setInterval(refreshSession, 1000 * 60 * 3);
    return () => window.clearInterval(intervalId);
  }, []);

  return (
    <SessionProvider>
      <Router>
        <Suspense fallback={<RouteLoader />}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify-otp" element={<VerifyOtp />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
            <Route
              path="/admin/dashboard"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/users"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminUsers />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/students"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <StudentManagement />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/management"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminManagement />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/preceptors"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <PreceptorManagement />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/system"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <SystemInitialization />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/roles"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <RoleManagement />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/revenue"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminRevenue />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/webhooks"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <WebhookMonitoring />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/settings"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminSettings />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/add-admin"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AddAdmin />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/preceptors/pending"
              element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <PendingPreceptors />
                </ProtectedRoute>
              }
            />

            <Route path="/preceptor" element={<Navigate to="/preceptor/dashboard" replace />} />
            <Route
              path="/preceptor/dashboard"
              element={
                <ProtectedRoute allowedRoles={['PRECEPTOR']}>
                  <PreceptorDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/preceptor/profile"
              element={
                <ProtectedRoute allowedRoles={['PRECEPTOR']}>
                  <PreceptorProfile />
                </ProtectedRoute>
              }
            />
            <Route
              path="/preceptor/license"
              element={<Navigate to="/preceptor/license-verification" replace />}
            />
            <Route
              path="/preceptor/license-verification"
              element={
                <ProtectedRoute allowedRoles={['PRECEPTOR']}>
                  <PreceptorLicense />
                </ProtectedRoute>
              }
            />
            <Route
              path="/subscription"
              element={<Navigate to="/preceptor/subscription" replace />}
            />
            <Route
              path="/preceptor/subscription"
              element={
                <ProtectedRoute allowedRoles={['PRECEPTOR']}>
                  <PreceptorSubscription />
                </ProtectedRoute>
              }
            />
            <Route
              path="/billing"
              element={<Navigate to="/preceptor/billing" replace />}
            />
            <Route
              path="/preceptor/billing"
              element={
                <ProtectedRoute allowedRoles={['PRECEPTOR']}>
                  <PreceptorBilling />
                </ProtectedRoute>
              }
            />
            <Route
              path="/preceptor/inquiries"
              element={
                <ProtectedRoute allowedRoles={['PRECEPTOR']}>
                  <PreceptorInquiries />
                </ProtectedRoute>
              }
            />

            <Route path="/student" element={<Navigate to="/student/dashboard" replace />} />
            <Route
              path="/student/dashboard"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentDashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/browse"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentBrowse />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/saved"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentSaved />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/profile"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentProfile />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/inquiries"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentInquiries />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/preceptor-detail/:id"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentPreceptorDetail />
                </ProtectedRoute>
              }
            />
            <Route
              path="/student/inquiry"
              element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <StudentInquiry />
                </ProtectedRoute>
              }
            />

            <Route path="/support" element={<InfoPage title="Support" description="Reach us at support@npaxis.com for help with your account and platform issues." />} />
            <Route path="/privacy-policy" element={<InfoPage title="Privacy Policy" description="NPaxis privacy policy placeholder page." />} />
            <Route path="/terms-of-service" element={<InfoPage title="Terms of Service" description="NPaxis terms of service placeholder page." />} />

            <Route path="/" element={<Landing />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/browse" element={<PublicBrowse />} />
            <Route path="/about" element={<About />} />
            <Route path="/subscription/success" element={<StripeReturn />} />
            <Route path="/subscription/cancel" element={<StripeReturn />} />
            <Route path="/*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </Router>
    </SessionProvider>
  );
}

export default App;
