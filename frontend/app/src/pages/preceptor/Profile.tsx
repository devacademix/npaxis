import React, { useEffect, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import ProfileForm, { type PreceptorProfileFormData } from '../../components/preceptor/ProfileForm';
import paymentService, { type SubscriptionStatus } from '../../services/payment';
import { preceptorService } from '../../services/preceptor';
import userService from '../../services/user';
import {
  mapAnalyticsSnapshot,
  mapProfileFormToRequestDTO,
  mapProfileData,
  mapSubscriptionStatusLabel,
  type PreceptorAnalyticsSnapshot,
} from '../../utils/preceptorProfile';

const emptyForm: PreceptorProfileFormData = {
  fullName: '',
  credentials: '',
  specialty: '',
  location: '',
  clinicalSetting: '',
  availableDays: [],
  honorarium: '',
  requirements: '',
  email: '',
  phone: '',
  verificationStatus: 'PENDING',
  premiumStatus: 'Inactive',
  isVerified: false,
  licenseNumber: '',
  licenseState: '',
  licenseFileUrl: '',
};

const emptyAnalytics: PreceptorAnalyticsSnapshot = {
  profileViews: 0,
  contactReveals: 0,
  inquiries: 0,
};

const MAX_PROFILE_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;
const ALLOWED_PROFILE_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/jpg'];

const Profile: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');
  const navigate = useNavigate();

  const [userId, setUserId] = useState<number | null>(null);
  const [formData, setFormData] = useState<PreceptorProfileFormData>(emptyForm);
  const [initialFormData, setInitialFormData] = useState<PreceptorProfileFormData>(emptyForm);
  const [analytics, setAnalytics] = useState<PreceptorAnalyticsSnapshot>(emptyAnalytics);
  const [subscriptionStatus, setSubscriptionStatus] = useState<SubscriptionStatus | null>(null);
  const [profileImageUrl, setProfileImageUrl] = useState('');
  const [savedProfileImageUrl, setSavedProfileImageUrl] = useState('');
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    let isCancelled = false;

    const loadProfile = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const user = await preceptorService.getLoggedInUser();
        if (isCancelled) return;

        setUserId(user.userId);

        const [profile, subscription, stats] = await Promise.all([
          preceptorService.getPreceptorById(user.userId).catch(() => null),
          paymentService.getSubscriptionStatus().catch(() => null),
          preceptorService.getStats(user.userId).catch(() => null),
        ]);

        if (isCancelled) return;

        const mappedProfile = mapProfileData(profile, user);
        const nextForm: PreceptorProfileFormData = {
          ...mappedProfile,
          premiumStatus: mapSubscriptionStatusLabel(subscription),
        };

        setFormData(nextForm);
        setInitialFormData(nextForm);
        setSubscriptionStatus(subscription);
        setAnalytics(mapAnalyticsSnapshot(stats));

        userService.fetchProfilePictureObjectUrl(user.userId).then((imageUrl) => {
          if (isCancelled) return;
          setProfileImageUrl(imageUrl || '');
          setSavedProfileImageUrl(imageUrl || '');
        });
      } catch (err: any) {
        if (isCancelled) return;
        setError(err?.message || 'Failed to load preceptor profile.');
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    };

    loadProfile();

    return () => {
      isCancelled = true;
    };
  }, [isPreceptor]);

  useEffect(() => {
    return () => {
      if (profileImageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(profileImageUrl);
      }
    };
  }, [profileImageUrl]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 3000);
    return () => window.clearTimeout(timer);
  }, [success]);

  if (!isPreceptor) {
    return <Navigate to="/login" replace />;
  }

  const handleChange = (field: keyof PreceptorProfileFormData, value: string | string[] | boolean) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const validate = () => {
    if (!formData.fullName.trim()) return 'Full Name is required.';
    if (!formData.specialty.trim()) return 'Specialty is required.';
    if (!formData.location.trim()) return 'Location is required.';
    return null;
  };

  const handleSave = async () => {
    setError(null);
    setSuccess(null);

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    if (!userId) {
      setError('Unable to identify preceptor account. Please login again.');
      return;
    }

    const payload = mapProfileFormToRequestDTO(formData);

    try {
      setIsSaving(true);
      const updated = await preceptorService.updatePreceptorProfile(userId, payload);

      if (profileImageFile) {
        await userService.uploadProfilePicture(userId, profileImageFile);
        const imageUrl = await userService.fetchProfilePictureObjectUrl(userId);
        setProfileImageUrl(imageUrl || profileImageUrl);
        setSavedProfileImageUrl(imageUrl || profileImageUrl);
        setProfileImageFile(null);
      }

      const mappedProfile = mapProfileData(updated, {
        displayName: formData.fullName,
        email: formData.email,
      });
      const nextData: PreceptorProfileFormData = {
        ...mappedProfile,
        premiumStatus: mapSubscriptionStatusLabel(subscriptionStatus),
      };

      setFormData(nextData);
      setInitialFormData(nextData);
      setSuccess('Profile updated successfully.');
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    setError(null);
    setSuccess(null);
    setFormData(initialFormData);
    setProfileImageFile(null);
    setProfileImageUrl(savedProfileImageUrl);
  };

  return (
    <PreceptorLayout pageTitle="Profile">
      <div className="mx-auto max-w-6xl">
        <div className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">Preceptor Profile</h1>
          <p className="mt-1 text-slate-500">Manage your professional details, visibility, and account status.</p>
        </div>

        {error ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {success ? (
          <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {success}
          </div>
        ) : null}

        {isLoading ? (
          <div className="space-y-4">
            <div className="h-32 animate-pulse rounded-2xl bg-slate-200/70" />
            <div className="h-[420px] animate-pulse rounded-2xl bg-slate-200/70" />
          </div>
        ) : (
          <div className="space-y-6">
            <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
                  <img
                    src={profileImageUrl}
                    onError={(event) => {
                      (event.currentTarget as HTMLImageElement).src = 'https://placehold.co/120x120/e2e8f0/475569?text=Profile';
                    }}
                    alt="Profile"
                    className="h-28 w-28 rounded-full object-cover ring-4 ring-slate-100"
                  />
                  <div>
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="text-2xl font-black tracking-tight text-slate-900">{formData.fullName || 'Preceptor Profile'}</h2>
                      {formData.verificationStatus === 'APPROVED' ? (
                        <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold uppercase tracking-wider text-emerald-700">
                          Verified
                        </span>
                      ) : null}
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                          formData.premiumStatus === 'Active'
                            ? 'bg-blue-100 text-blue-700'
                            : 'bg-slate-100 text-slate-700'
                        }`}
                      >
                        Premium {formData.premiumStatus}
                      </span>
                    </div>
                    <p className="mt-1 text-sm text-slate-500">{formData.specialty || 'Specialty not provided yet'}</p>
                    <p className="mt-1 text-sm text-slate-500">{formData.email || 'Email not available'}</p>
                  </div>
                </div>

                <div>
                  <label className="inline-flex cursor-pointer rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
                    Upload Picture
                    <input
                      type="file"
                      accept="image/png,image/jpeg"
                      className="hidden"
                      onChange={(event) => {
                        const nextFile = event.target.files?.[0] || null;
                        if (!nextFile) {
                          setProfileImageFile(null);
                          return;
                        }

                        if (!ALLOWED_PROFILE_IMAGE_TYPES.includes(nextFile.type)) {
                          setError('Please choose a PNG or JPG profile image.');
                          event.currentTarget.value = '';
                          return;
                        }

                        if (nextFile.size > MAX_PROFILE_IMAGE_SIZE_BYTES) {
                          setError('Profile image must be 10 MB or smaller.');
                          event.currentTarget.value = '';
                          return;
                        }

                        setError(null);
                        setSuccess(`Image selected: ${nextFile.name}`);
                        setProfileImageFile(nextFile);
                        setProfileImageUrl(URL.createObjectURL(nextFile));
                      }}
                    />
                  </label>
                  <p className="mt-2 text-xs text-slate-500">Allowed: PNG, JPG, JPEG. Max size: 10 MB.</p>
                </div>
              </div>
            </section>

            <ProfileForm
              data={formData}
              isSaving={isSaving}
              analytics={analytics}
              onChange={handleChange}
              onSave={handleSave}
              onCancel={handleCancel}
              onGoDashboard={() => navigate('/preceptor/dashboard')}
              onUploadLicense={() => navigate('/preceptor/license')}
              onUpgradePlan={() => navigate('/subscription')}
            />
          </div>
        )}
      </div>
    </PreceptorLayout>
  );
};

export default Profile;
