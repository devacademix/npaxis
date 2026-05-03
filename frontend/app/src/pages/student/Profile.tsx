import React, { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Navigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import EmptyState from '../../components/ui/EmptyState';
import ErrorState from '../../components/ui/ErrorState';
import SkeletonBlock from '../../components/ui/SkeletonBlock';
import { useSession } from '../../context/SessionContext';
import { studentService, type StudentProfile, type StudentUser } from '../../services/student';
import userService from '../../services/user';

const StudentProfilePage: React.FC = () => {
  const { currentUser, role, isLoading: isSessionLoading } = useSession();
  const isStudent = role === 'STUDENT';
  const queryClient = useQueryClient();

  const [formData, setFormData] = useState({
    displayName: '',
    email: '',
    university: '',
    program: '',
    graduationYear: '',
    phone: '',
  });
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const userQuery = useQuery<StudentUser>({
    queryKey: ['student-profile', 'user', currentUser?.userId ?? 'session'],
    queryFn: async () => {
      if (currentUser) {
        return {
          userId: currentUser.userId,
          displayName: currentUser.displayName,
          email: currentUser.email,
        };
      }
      return studentService.getLoggedInUser();
    },
    enabled: !isSessionLoading && isStudent,
    staleTime: 60_000,
  });

  const profileQuery = useQuery<{ profile: StudentProfile; imageUrl: string }>({
    queryKey: ['student-profile', 'details', userQuery.data?.userId ?? 'unknown'],
    queryFn: async () => {
      if (!userQuery.data?.userId) {
        throw new Error('Unable to identify the logged-in student.');
      }
      const [profile, imageUrl] = await Promise.all([
        studentService.getStudentProfile(userQuery.data.userId),
        userService.fetchProfilePictureObjectUrl(userQuery.data.userId).catch(() => ''),
      ]);
      return { profile, imageUrl: imageUrl || '' };
    },
    enabled: !isSessionLoading && isStudent && Boolean(userQuery.data?.userId),
    placeholderData: (previousData) => previousData,
  });

  useEffect(() => {
    if (!userQuery.data || !profileQuery.data) return;
    const currentProfile = profileQuery.data.profile;
    setPreviewUrl(profileQuery.data.imageUrl || '');
    setFormData({
      displayName: currentProfile.displayName || userQuery.data.displayName || '',
      email: currentProfile.email || userQuery.data.email || '',
      university: currentProfile.university || '',
      program: currentProfile.program || '',
      graduationYear: currentProfile.graduationYear || '',
      phone: currentProfile.phone || '',
    });
  }, [profileQuery.data, userQuery.data]);

  useEffect(() => {
    return () => {
      if (previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  if (!isSessionLoading && !isStudent) {
    return <Navigate to="/login" replace />;
  }

  const saveMutation = useMutation({
    mutationKey: ['student-profile', 'save'],
    mutationFn: async () => {
      if (!userQuery.data) {
        throw new Error('Unable to identify the logged-in student.');
      }

      const studentPayload = {
        university: formData.university.trim(),
        program: formData.program.trim(),
        graduationYear: formData.graduationYear.trim(),
        phone: formData.phone.trim(),
      };

      const updated = await studentService.updateStudentDetails(userQuery.data.userId, studentPayload);
      if (file) {
        await userService.uploadProfilePicture(userQuery.data.userId, file);
      }
      const imageUrl = file ? await userService.fetchProfilePictureObjectUrl(userQuery.data.userId) : previewUrl;
      return { updated, imageUrl: imageUrl || '' };
    },
    onSuccess: async ({ imageUrl }) => {
      setSuccess(file ? 'Profile and picture updated successfully.' : 'Profile updated successfully.');
      setFile(null);
      setPreviewUrl(imageUrl || previewUrl);
      await queryClient.invalidateQueries({ queryKey: ['student-profile'] });
    },
  });

  const user = userQuery.data ?? null;
  const profile = profileQuery.data?.profile ?? null;
  const isLoading = isSessionLoading || (userQuery.isLoading && !userQuery.data) || (profileQuery.isLoading && !profileQuery.data);
  const isSaving = saveMutation.isPending;
  const pageError = error || userQuery.error?.message || profileQuery.error?.message || saveMutation.error?.message || null;

  const handleSave = async () => {
    try {
      setError(null);
      setSuccess(null);
      await saveMutation.mutateAsync();
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile.');
    }
  };

  return (
    <StudentLayout pageTitle="Profile">
      <div className="mx-auto max-w-5xl space-y-6">
        <div>
          <h2 className="text-3xl font-black tracking-tight text-slate-900">Student Profile</h2>
          <p className="mt-1 text-sm text-slate-500">Update your personal details and upload your profile photo.</p>
        </div>

        {pageError ? <ErrorState message={pageError} onRetry={() => void profileQuery.refetch()} /> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}

        {isLoading ? (
          <div className="space-y-4">
            <SkeletonBlock className="h-32" />
            <SkeletonBlock className="h-96" />
          </div>
        ) : user && profile ? (
          <div className="grid gap-6 lg:grid-cols-[320px,1fr]">
            <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <div className="flex flex-col items-center text-center">
                <img
                  src={previewUrl}
                  onError={(event) => {
                    (event.currentTarget as HTMLImageElement).src = 'https://placehold.co/160x160/e2e8f0/475569?text=User';
                  }}
                  alt="Profile"
                  className="h-40 w-40 rounded-full object-cover ring-4 ring-slate-100"
                />
                <p className="mt-4 text-lg font-bold text-slate-900">{profile?.displayName}</p>
                <p className="text-sm text-slate-500">{profile?.email}</p>
                <label className="mt-5 w-full rounded-xl border border-dashed border-slate-300 px-4 py-4 text-sm font-semibold text-slate-600 hover:bg-slate-50">
                  Upload Profile Picture
                  <input
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={(event) => {
                      const selectedFile = event.target.files?.[0] || null;
                      setFile(selectedFile);
                      if (selectedFile) {
                        setPreviewUrl(URL.createObjectURL(selectedFile));
                      }
                    }}
                  />
                </label>
                <p className="mt-2 text-xs text-slate-500">
                  Recommended: square image `400 x 400 px` or larger. Allowed size: up to `10 MB`.
                </p>
              </div>
            </section>

            <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <div className="grid gap-4 md:grid-cols-2">
                {[
                  ['displayName', 'Full Name'],
                  ['email', 'Email'],
                  ['university', 'University'],
                  ['program', 'Program'],
                  ['graduationYear', 'Graduation Year'],
                  ['phone', 'Phone'],
                ].map(([key, label]) => (
                  <label key={key} className="space-y-1 text-xs font-semibold uppercase tracking-wider text-slate-500">
                    {label}
                    <input
                      value={(formData as any)[key]}
                      onChange={(event) => setFormData((prev) => ({ ...prev, [key]: event.target.value }))}
                      readOnly={key === 'displayName' || key === 'email'}
                      className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm font-medium text-slate-700"
                    />
                  </label>
                ))}
              </div>

              <p className="mt-4 text-xs font-medium text-slate-500">
                Name and email are managed from your account record. This page updates your student profile details and photo.
              </p>

              <div className="mt-6 flex justify-end">
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={isSaving}
                  className="rounded-full bg-blue-700 px-6 py-3 text-sm font-bold text-white hover:bg-blue-800 disabled:opacity-60"
                >
                  {isSaving ? 'Saving...' : 'Save Profile'}
                </button>
              </div>
            </section>
          </div>
        ) : (
          <EmptyState text="We could not load your profile right now." actionLabel="Retry" onAction={() => void profileQuery.refetch()} />
        )}
      </div>
    </StudentLayout>
  );
};

export default StudentProfilePage;
