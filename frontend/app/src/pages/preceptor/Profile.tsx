import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import PreceptorLayout from '../../components/layout/PreceptorLayout';
import ProfileForm, { type PreceptorProfileFormData } from '../../components/preceptor/ProfileForm';
import { preceptorService, type PreceptorUpdatePayload } from '../../services/preceptor';
import userService from '../../services/user';

const emptyForm: PreceptorProfileFormData = {
  fullName: '',
  credentials: '',
  specialty: '',
  location: '',
  setting: '',
  availableDays: [],
  honorarium: '',
  requirements: '',
  email: '',
  phone: '',
  verificationStatus: 'PENDING',
  isPremium: false,
  isVerified: false,
  licenseNumber: '',
  licenseState: '',
  licenseFileUrl: '',
};

const Profile: React.FC = () => {
  const role = localStorage.getItem('role');
  const isPreceptor = role === 'PRECEPTOR' || role === 'ROLE_PRECEPTOR' || (role ?? '').includes('PRECEPTOR');

  const [userId, setUserId] = useState<number | null>(null);
  const [formData, setFormData] = useState<PreceptorProfileFormData>(emptyForm);
  const [initialFormData, setInitialFormData] = useState<PreceptorProfileFormData>(emptyForm);
  const [profileImageUrl, setProfileImageUrl] = useState('');
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isPreceptor) return;

    const loadProfile = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const user = await preceptorService.getLoggedInUser();
        setUserId(user.userId);

        const profile = await preceptorService.getPreceptorById(user.userId);
        const imageUrl = await userService.fetchProfilePictureObjectUrl(user.userId);
        setProfileImageUrl(imageUrl || '');
        const merged: PreceptorProfileFormData = {
          fullName: profile.displayName || user.displayName || '',
          credentials: profile.credentials || '',
          specialty: profile.specialty || '',
          location: profile.location || '',
          setting: profile.setting || '',
          availableDays: (profile.availableDays || []).map((day) => String(day).toUpperCase()),
          honorarium: profile.honorarium || '',
          requirements: profile.requirements || '',
          email: user.email || '',
          phone: profile.phone || '',
          verificationStatus: String(profile.verificationStatus || 'PENDING').toUpperCase(),
          isPremium: Boolean(profile.isPremium),
          isVerified: Boolean(profile.isVerified),
          licenseNumber: profile.licenseNumber || '',
          licenseState: profile.licenseState || '',
          licenseFileUrl: profile.licenseFileUrl || '',
        };

        setFormData(merged);
        setInitialFormData(merged);
      } catch (err: any) {
        setError(err?.message || 'Failed to load preceptor profile.');
      } finally {
        setIsLoading(false);
      }
    };

    loadProfile();
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

    const payload: PreceptorUpdatePayload = {
      name: formData.fullName.trim(),
      credentials: formData.credentials.trim(),
      specialty: formData.specialty.trim(),
      location: formData.location.trim(),
      setting: formData.setting.trim(),
      availableDays: formData.availableDays,
      honorarium: formData.honorarium.trim(),
      requirements: formData.requirements.trim(),
      email: formData.email.trim(),
      phone: formData.phone.trim(),
      licenseNumber: formData.licenseNumber.trim(),
      licenseState: formData.licenseState.trim(),
      licenseFileUrl: formData.licenseFileUrl.trim(),
    };

    try {
      setIsSaving(true);
      const updated = await preceptorService.updatePreceptorProfile(userId, payload);
      if (profileImageFile) {
        await userService.uploadProfilePicture(userId, profileImageFile);
        const imageUrl = await userService.fetchProfilePictureObjectUrl(userId);
        setProfileImageUrl(imageUrl || profileImageUrl);
        setProfileImageFile(null);
      }
      const nextData: PreceptorProfileFormData = {
        ...formData,
        fullName: updated.displayName || formData.fullName,
        credentials: updated.credentials || '',
        specialty: updated.specialty || '',
        location: updated.location || '',
        setting: updated.setting || '',
        availableDays: (updated.availableDays || formData.availableDays).map((day) => String(day).toUpperCase()),
        honorarium: updated.honorarium || '',
        requirements: updated.requirements || '',
        verificationStatus: String(updated.verificationStatus || formData.verificationStatus).toUpperCase(),
        isPremium: Boolean(updated.isPremium),
        isVerified: Boolean(updated.isVerified),
        licenseNumber: updated.licenseNumber || formData.licenseNumber,
        licenseState: updated.licenseState || formData.licenseState,
        licenseFileUrl: updated.licenseFileUrl || formData.licenseFileUrl,
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
  };

  return (
    <PreceptorLayout pageTitle="Profile">
      <div className="mx-auto max-w-6xl">
        <div className="mb-6">
          <h1 className="text-3xl font-black tracking-tight text-slate-900">Preceptor Profile</h1>
          <p className="mt-1 text-slate-500">View and update your professional profile information.</p>
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
              <div className="flex flex-col gap-4 md:flex-row md:items-center">
                <img
                  src={profileImageUrl}
                  onError={(event) => {
                    (event.currentTarget as HTMLImageElement).src = 'https://placehold.co/120x120/e2e8f0/475569?text=Profile';
                  }}
                  alt="Profile"
                  className="h-28 w-28 rounded-full object-cover ring-4 ring-slate-100"
                />
                <div>
                  <h2 className="text-lg font-bold text-slate-900">Profile Picture</h2>
                  <p className="mt-1 text-sm text-slate-500">Upload a clear photo to personalize your account.</p>
                  <label className="mt-3 inline-flex cursor-pointer rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">
                    Choose Image
                    <input
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={(event) => {
                        const nextFile = event.target.files?.[0] || null;
                        setProfileImageFile(nextFile);
                        if (nextFile) {
                          setProfileImageUrl(URL.createObjectURL(nextFile));
                        }
                      }}
                    />
                  </label>
                </div>
              </div>
            </section>
            <ProfileForm data={formData} isSaving={isSaving} onChange={handleChange} onSave={handleSave} onCancel={handleCancel} />
          </div>
        )}
      </div>
    </PreceptorLayout>
  );
};

export default Profile;
