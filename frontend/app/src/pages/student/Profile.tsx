import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import { studentService, type StudentProfile, type StudentUser } from '../../services/student';
import userService from '../../services/user';

const StudentProfilePage: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');

  const [user, setUser] = useState<StudentUser | null>(null);
  const [profile, setProfile] = useState<StudentProfile | null>(null);
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
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isStudent) return;
    const loadProfile = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const currentUser = await studentService.getLoggedInUser();
        const currentProfile = await studentService.getStudentProfile(currentUser.userId);
        setUser(currentUser);
        setProfile(currentProfile);
        const imageUrl = await userService.fetchProfilePictureObjectUrl(currentUser.userId);
        setPreviewUrl(imageUrl || '');
        setFormData({
          displayName: currentProfile.displayName || currentUser.displayName || '',
          email: currentProfile.email || currentUser.email || '',
          university: currentProfile.university || '',
          program: currentProfile.program || '',
          graduationYear: currentProfile.graduationYear || '',
          phone: currentProfile.phone || '',
        });
      } catch (err: any) {
        setError(err?.message || 'Failed to load profile.');
      } finally {
        setIsLoading(false);
      }
    };
    loadProfile();
  }, [isStudent]);

  useEffect(() => {
    return () => {
      if (previewUrl.startsWith('blob:')) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  const handleSave = async () => {
    if (!user) return;
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);

      let updated = profile;
      const studentPayload = {
        university: formData.university.trim(),
        program: formData.program.trim(),
        graduationYear: formData.graduationYear.trim(),
        phone: formData.phone.trim(),
      };

      updated = await studentService.updateStudentDetails(user.userId, studentPayload);
      if (file) {
        await userService.uploadProfilePicture(user.userId, file);
      }

      setProfile((prev) => ({
        ...(prev ?? updated ?? {
          userId: user.userId,
          displayName: formData.displayName.trim(),
          email: formData.email.trim(),
        }),
        ...(updated ?? {}),
        displayName: formData.displayName.trim() || prev?.displayName || user.displayName,
        email: formData.email.trim() || prev?.email || user.email,
      }));

      setSuccess(file ? 'Profile and picture updated successfully.' : 'Profile updated successfully.');
      if (file) {
        const imageUrl = await userService.fetchProfilePictureObjectUrl(user.userId);
        setPreviewUrl(imageUrl || previewUrl);
        setFile(null);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <StudentLayout pageTitle="Profile">
      <div className="mx-auto max-w-5xl space-y-6">
        <div>
          <h2 className="text-3xl font-black tracking-tight text-slate-900">Student Profile</h2>
          <p className="mt-1 text-sm text-slate-500">Update your personal details and upload your profile photo.</p>
        </div>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}

        {isLoading ? (
          <div className="space-y-4">
            <div className="h-32 animate-pulse rounded-2xl bg-slate-200/70" />
            <div className="h-96 animate-pulse rounded-2xl bg-slate-200/70" />
          </div>
        ) : (
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
        )}
      </div>
    </StudentLayout>
  );
};

export default StudentProfilePage;
