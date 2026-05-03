import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { authService } from './services/auth';
import Login from './pages/auth/Login';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import Register from './pages/auth/Register';
import VerifyOtp from './pages/auth/VerifyOtp';
import AdminDashboard from './pages/admin/Dashboard';
import AdminManagement from './pages/admin/Management';
import StudentManagement from './pages/admin/StudentManagement';
import PreceptorManagement from './pages/admin/PreceptorManagement';
import RoleManagement from './pages/admin/RoleManagement';
import SystemInitialization from './pages/admin/SystemInitialization';
import PendingPreceptors from './pages/admin/PendingPreceptors';
import AdminUsers from './pages/admin/Users';
import AdminRevenue from './pages/admin/Revenue';
import AdminSettings from './pages/admin/Settings';
import AddAdmin from './pages/admin/AddAdmin';
import PreceptorDashboard from './pages/preceptor/Dashboard';
import PreceptorProfile from './pages/preceptor/Profile';
import PreceptorLicense from './pages/preceptor/License';
import PreceptorSubscription from './pages/preceptor/Subscription';
import PreceptorBilling from './pages/preceptor/Billing';
import StudentDashboard from './pages/student/Dashboard';
import StudentBrowse from './pages/student/Browse';
import StudentSaved from './pages/student/Saved';
import StudentProfile from './pages/student/Profile';
import StudentPreceptorDetail from './pages/student/PreceptorDetail';
import StudentInquiry from './pages/student/Inquiry';
import StudentInquiries from './pages/student/Inquiries';
import PreceptorInquiries from './pages/preceptor/Inquiries';
import ProtectedRoute from './routes/ProtectedRoute';
import InfoPage from './pages/common/InfoPage';
import Landing from './pages/common/Landing';
import Contact from './pages/common/Contact';
import PublicBrowse from './pages/common/PublicBrowse';
import About from './pages/common/About';
import StripeReturn from './pages/common/StripeReturn';

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
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-otp" element={<VerifyOtp />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        <Route
          path="/admin"
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
          path="/preceptor/license-verification"
          element={
            <ProtectedRoute allowedRoles={['PRECEPTOR']}>
              <PreceptorLicense />
            </ProtectedRoute>
          }
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

        <Route
          path="/student"
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
    </Router>
  );
}

export default App;
