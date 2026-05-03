import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Navigate, useNavigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import ProfileForm, { type PreceptorProfileFormData } from '../../components/preceptor/ProfileForm';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import SkeletonBlock from '../../components/ui/SkeletonBlock';
import { useSession } from '../../context/SessionContext';
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

const MAX_PROFILE_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;
const ALLOWED_PROFILE_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/jpg'];

const Profile: React.FC = () => {
  const { currentUser, role, isLoading: isSessionLoading } = useSession();
  const isPreceptor = role === 'PRECEPTOR';
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState<PreceptorProfileFormData>(emptyForm);
  const [initialFormData, setInitialFormData] = useState<PreceptorProfileFormData>(emptyForm);
  const [subscriptionStatus, setSubscriptionStatus] = useState<SubscriptionStatus | null>(null);
  const [profileImageUrl, setProfileImageUrl] = useState('');
  const [savedProfileImageUrl, setSavedProfileImageUrl] = useState('');
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const userQuery = useQuery({
    queryKey: ['preceptor-profile', 'user', currentUser?.userId ?? 'session'],
    queryFn: async () => {
      if (currentUser) {
        return {
          userId: currentUser.userId,
          displayName: currentUser.displayName,
          email: currentUser.email,
        };
      }
      return preceptorService.getLoggedInUser();
    },
    enabled: !isSessionLoading && isPreceptor,
    staleTime: 60_000,
  });

  const profileQuery = useQuery({
    queryKey: ['preceptor-profile', 'details', userQuery.data?.userId ?? 'unknown'],
    queryFn: async () => {
      if (!userQuery.data?.userId) {
        throw new Error('Unable to identify the logged-in preceptor.');
      }

      const [profile, subscription, stats, imageUrl] = await Promise.all([
        preceptorService.getPreceptorById(userQuery.data.userId).catch(() => null),
        paymentService.getSubscriptionStatus().catch(() => null),
        preceptorService.getStats(userQuery.data.userId).catch(() => null),
        userService.fetchProfilePictureObjectUrl(userQuery.data.userId).catch(() => ''),
      ]);

      return { profile, subscription, stats, imageUrl };
    },
    enabled: !isSessionLoading && isPreceptor && Boolean(userQuery.data?.userId),
    placeholderData: (previousData) => previousData,
  });

  const analytics = useMemo<PreceptorAnalyticsSnapshot>(
    () => mapAnalyticsSnapshot(profileQuery.data?.stats ?? null),
    [profileQuery.data?.stats]
  );

  useEffect(() => {
    if (!userQuery.data || !profileQuery.data) return;

    const mappedProfile = mapProfileData(profileQuery.data.profile, userQuery.data);
    const nextForm: PreceptorProfileFormData = {
      ...mappedProfile,
      premiumStatus: mapSubscriptionStatusLabel(profileQuery.data.subscription),
    };

    setFormData(nextForm);
    setInitialFormData(nextForm);
    setSubscriptionStatus(profileQuery.data.subscription ?? null);
    setProfileImageUrl(profileQuery.data.imageUrl || '');
    setSavedProfileImageUrl(profileQuery.data.imageUrl || '');
  }, [profileQuery.data, userQuery.data]);

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

  if (!isSessionLoading && !isPreceptor) {
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

  const updateProfileMutation = useMutation({
    mutationKey: ['preceptor-profile', 'save'],
    mutationFn: async () => {
      if (!userQuery.data?.userId) {
        throw new Error('Unable to identify preceptor account. Please login again.');
      }

      const payload = mapProfileFormToRequestDTO(formData);
      const updated = await preceptorService.updatePreceptorProfile(userQuery.data.userId, payload);

      if (profileImageFile) {
        await userService.uploadProfilePicture(userQuery.data.userId, profileImageFile);
      }

      const imageUrl = profileImageFile
        ? await userService.fetchProfilePictureObjectUrl(userQuery.data.userId)
        : savedProfileImageUrl;

      return { updated, imageUrl };
    },
    onSuccess: async ({ updated, imageUrl }) => {
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
      setProfileImageUrl(imageUrl || profileImageUrl);
      setSavedProfileImageUrl(imageUrl || profileImageUrl);
      setProfileImageFile(null);
      await queryClient.invalidateQueries({ queryKey: ['preceptor-profile'] });
    },
  });

  const isLoading = isSessionLoading || (userQuery.isLoading && !userQuery.data) || (profileQuery.isLoading && !profileQuery.data);
  const isSaving = updateProfileMutation.isPending;
  const pageError = error || userQuery.error?.message || profileQuery.error?.message || updateProfileMutation.error?.message || null;

  const handleSave = async () => {
    setError(null);
    setSuccess(null);

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      await updateProfileMutation.mutateAsync();
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile.');
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

        {pageError ? (
          <div className="mb-4">
            <ErrorState message={pageError} onRetry={() => void profileQuery.refetch()} />
          </div>
        ) : null}

        {success ? (
          <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {success}
          </div>
        ) : null}

        {isLoading ? (
          <div className="space-y-4">
            <SkeletonBlock className="h-32" />
            <SkeletonBlock className="h-[420px]" />
          </div>
        ) : userQuery.data && profileQuery.data ? (
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
        ) : (
          <EmptyState text="We could not load your profile right now." actionLabel="Retry" onAction={() => void profileQuery.refetch()} />
        )}
      </div>
    </PreceptorLayout>
  );
};

export default Profile;
