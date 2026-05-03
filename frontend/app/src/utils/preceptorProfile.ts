import type { SubscriptionStatus } from '../services/payment';
import type {
  PreceptorAvailableDay,
  PreceptorProfile,
  PreceptorStatsResponse,
  PreceptorUpdatePayload,
} from '../services/preceptor';

export interface MappedPreceptorProfileData {
  fullName: string;
  credentials: string;
  specialty: string;
  location: string;
  clinicalSetting: string;
  honorarium: string;
  email: string;
  phone: string;
  availableDays: string[];
  requirements: string;
  verificationStatus: string;
  isVerified: boolean;
  licenseNumber: string;
  licenseState: string;
  licenseFileUrl: string;
}

export interface PreceptorAnalyticsSnapshot {
  profileViews: number;
  contactReveals: number;
  inquiries: number;
}

interface ProfileFormShape {
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
  licenseNumber: string;
  licenseState: string;
  licenseFileUrl: string;
}

const AVAILABLE_DAY_VALUES: PreceptorAvailableDay[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

const normalizeOptionalText = (value: string | null | undefined): string | null => {
  const trimmed = String(value ?? '').trim();
  return trimmed ? trimmed : null;
};

const normalizeCommaSeparatedList = (value: string): string[] =>
  Array.from(
    new Set(
      value
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
    )
  );

const normalizeAvailableDays = (values: string[]): PreceptorAvailableDay[] =>
  Array.from(
    new Set(
      values
        .map((value) => String(value ?? '').trim().toUpperCase())
        .filter((value): value is PreceptorAvailableDay =>
          AVAILABLE_DAY_VALUES.includes(value as PreceptorAvailableDay)
        )
    )
  );

export const mapProfileData = (
  profile: PreceptorProfile | null | undefined,
  fallbackUser?: { displayName?: string; email?: string } | null
): MappedPreceptorProfileData => ({
  fullName: profile?.displayName || fallbackUser?.displayName || '',
  credentials: profile?.credentials || '',
  specialty: profile?.specialty || '',
  location: profile?.location || '',
  clinicalSetting: profile?.setting || '',
  honorarium: profile?.honorarium || '',
  email: profile?.email || fallbackUser?.email || '',
  phone: profile?.phone || '',
  availableDays: (profile?.availableDays || []).map((day) => String(day).toUpperCase()),
  requirements: profile?.requirements || '',
  verificationStatus: String(profile?.verificationStatus || 'PENDING').toUpperCase(),
  isVerified: String(profile?.verificationStatus || '').toUpperCase() === 'APPROVED' || Boolean(profile?.isVerified),
  licenseNumber: profile?.licenseNumber || '',
  licenseState: profile?.licenseState || '',
  licenseFileUrl: profile?.licenseFileUrl || '',
});

export const mapAnalyticsSnapshot = (
  stats: PreceptorStatsResponse | null | undefined
): PreceptorAnalyticsSnapshot => ({
  profileViews: Number(stats?.profileViews ?? 0),
  contactReveals: Number(stats?.contactReveals ?? 0),
  inquiries: Number(stats?.inquiries ?? 0),
});

export const mapSubscriptionStatusLabel = (subscription: SubscriptionStatus | null | undefined) =>
  subscription?.accessEnabled ? 'Active' : 'Inactive';

export const mapProfileFormToRequestDTO = (form: ProfileFormShape): PreceptorUpdatePayload => ({
  name: form.fullName.trim(),
  credentials: normalizeCommaSeparatedList(form.credentials),
  specialties: normalizeCommaSeparatedList(form.specialty),
  location: form.location.trim(),
  setting: normalizeOptionalText(form.clinicalSetting),
  availableDays: normalizeAvailableDays(form.availableDays),
  honorarium: normalizeOptionalText(form.honorarium),
  requirements: normalizeOptionalText(form.requirements),
  email: normalizeOptionalText(form.email),
  phone: normalizeOptionalText(form.phone),
  licenseNumber: normalizeOptionalText(form.licenseNumber),
  licenseState: normalizeOptionalText(form.licenseState),
  licenseFileUrl: normalizeOptionalText(form.licenseFileUrl),
});
