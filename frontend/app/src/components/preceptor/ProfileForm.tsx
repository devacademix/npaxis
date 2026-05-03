import React from 'react';
import MultiSelect from '../common/MultiSelect';

export interface PreceptorProfileFormData {
  fullName: string;
  credentials: string;
  specialty: string;
  location: string;
  clinicalSetting: string;
  availableDays: string[];
  honorarium: string;
  requirements: string;
  email: string;
  phone: string;
  verificationStatus: string;
  premiumStatus: string;
  isVerified: boolean;
  licenseNumber: string;
  licenseState: string;
  licenseFileUrl: string;
}

interface AnalyticsSummary {
  profileViews: number;
  contactReveals: number;
  inquiries: number;
}

interface ProfileFormProps {
  data: PreceptorProfileFormData;
  isSaving: boolean;
  analytics: AnalyticsSummary;
  onChange: (field: keyof PreceptorProfileFormData, value: string | string[] | boolean) => void;
  onSave: () => void;
  onCancel: () => void;
  onGoDashboard: () => void;
  onUploadLicense: () => void;
  onUpgradePlan: () => void;
}

const DAY_OPTIONS = [
  { label: 'Monday', value: 'MONDAY' },
  { label: 'Tuesday', value: 'TUESDAY' },
  { label: 'Wednesday', value: 'WEDNESDAY' },
  { label: 'Thursday', value: 'THURSDAY' },
  { label: 'Friday', value: 'FRIDAY' },
  { label: 'Saturday', value: 'SATURDAY' },
  { label: 'Sunday', value: 'SUNDAY' },
];

const statusBadgeClass = (status: string) => {
  const normalized = status.toUpperCase();
  if (normalized === 'APPROVED') return 'bg-emerald-100 text-emerald-700';
  if (normalized === 'REJECTED') return 'bg-red-100 text-red-700';
  return 'bg-amber-100 text-amber-700';
};

const ProfileForm: React.FC<ProfileFormProps> = ({
  data,
  isSaving,
  analytics,
  onChange,
  onSave,
  onCancel,
  onGoDashboard,
  onUploadLicense,
  onUpgradePlan,
}) => {
  return (
    <div className="space-y-6">
      <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-blue-100 text-2xl font-black text-blue-700">
              {data.fullName ? data.fullName.charAt(0).toUpperCase() : 'P'}
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-2xl font-black tracking-tight text-slate-900">{data.fullName || 'Preceptor Profile'}</h2>
                {data.isVerified ? (
                  <span className="inline-flex items-center rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-bold text-emerald-700">
                    Verified
                  </span>
                ) : null}
              </div>
              <p className="text-sm font-medium text-slate-500">{data.credentials || 'Add your credentials'}</p>
            </div>
          </div>
          <div className="flex flex-wrap gap-2">
            <span className={`rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${statusBadgeClass(data.verificationStatus)}`}>
              Verification: {data.verificationStatus || 'PENDING'}
            </span>
            <span
              className={`rounded-full px-3 py-1 text-xs font-bold uppercase tracking-wider ${
                data.premiumStatus === 'Active' ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-700'
              }`}
            >
              Premium: {data.premiumStatus || 'Inactive'}
            </span>
          </div>
        </div>
      </section>

      <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <h3 className="mb-4 text-xl font-bold text-slate-900">Professional Profile</h3>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Full Name</label>
            <input
              value={data.fullName}
              onChange={(event) => onChange('fullName', event.target.value)}
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
              required
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Credentials</label>
            <input
              value={data.credentials}
              onChange={(event) => onChange('credentials', event.target.value)}
              placeholder="MD, NP, PA"
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Specialty</label>
            <input
              value={data.specialty}
              onChange={(event) => onChange('specialty', event.target.value)}
              placeholder="Add your specialty"
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
              required
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Location</label>
            <input
              value={data.location}
              onChange={(event) => onChange('location', event.target.value)}
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
              required
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Clinical Setting</label>
            <input
              value={data.clinicalSetting}
              onChange={(event) => onChange('clinicalSetting', event.target.value)}
              placeholder="Hospital, Private Clinic..."
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Honorarium</label>
            <input
              value={data.honorarium}
              onChange={(event) => onChange('honorarium', event.target.value)}
              placeholder="e.g. 2000/session"
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Email</label>
            <input
              value={data.email}
              readOnly
              className="w-full cursor-not-allowed rounded-lg border border-slate-200 bg-slate-100 px-3 py-2 text-sm text-slate-600"
            />
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Phone</label>
            <input
              value={data.phone}
              onChange={(event) => onChange('phone', event.target.value)}
              placeholder="+91 98xxxxxxx"
              className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              disabled={isSaving}
            />
          </div>
        </div>

        <div className="mt-4">
          <MultiSelect
            label="Available Days"
            options={DAY_OPTIONS}
            selectedValues={data.availableDays}
            onChange={(nextValues) => onChange('availableDays', nextValues)}
            disabled={isSaving}
          />
        </div>

        <div className="mt-4">
          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Requirements</label>
          <textarea
            value={data.requirements}
            onChange={(event) => onChange('requirements', event.target.value)}
            rows={4}
            placeholder="Describe onboarding expectations, prerequisites, and preferred student experience."
            className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
            disabled={isSaving}
          />
        </div>
      </section>

      <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <div className="mb-4">
          <h3 className="text-xl font-bold text-slate-900">Analytics Snapshot</h3>
          <p className="mt-1 text-sm text-slate-500">Live performance metrics from your analytics endpoint.</p>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {[
            ['Profile Views', analytics.profileViews],
            ['Contact Reveals', analytics.contactReveals],
            ['Total Inquiries', analytics.inquiries],
          ].map(([label, value]) => (
            <div key={String(label)} className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-bold uppercase tracking-wider text-slate-500">{label}</p>
              <p className="mt-2 text-3xl font-black tracking-tight text-slate-900">{Number(value ?? 0).toLocaleString('en-IN')}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
        <div className="mb-4">
          <h3 className="text-xl font-bold text-slate-900">Quick Actions</h3>
          <p className="mt-1 text-sm text-slate-500">Jump to the most common profile and account actions.</p>
        </div>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <button
            type="button"
            onClick={onGoDashboard}
            className="rounded-2xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            Go to Dashboard
          </button>
          <button
            type="button"
            onClick={onUploadLicense}
            className="rounded-2xl border border-slate-200 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            Upload License
          </button>
          <button
            type="button"
            onClick={onUpgradePlan}
            className="rounded-2xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-semibold text-blue-700 hover:bg-blue-100"
          >
            Upgrade Plan
          </button>
        </div>
      </section>

      <div className="sticky bottom-4 z-20 flex justify-end gap-3">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSaving}
          className="rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-bold text-slate-700 hover:bg-slate-50 disabled:opacity-60"
        >
          Cancel
        </button>
        <button
          type="button"
          onClick={onSave}
          disabled={isSaving}
          className="inline-flex items-center gap-2 rounded-full bg-blue-700 px-6 py-2.5 text-sm font-bold text-white hover:bg-blue-800 disabled:opacity-60"
        >
          {isSaving ? (
            <>
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
              Saving...
            </>
          ) : (
            <>
              <span className="material-symbols-outlined text-base">save</span>
              Save Changes
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default ProfileForm;
