import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import SettingsForm, { type SettingsState, type SettingsTab } from '../../components/admin/SettingsForm';
import { authService } from '../../services/auth';
import { adminService, type SystemSetting } from '../../services/admin';
import settingsService, { type AdminCurrentUser } from '../../services/settings';

const defaultSettings: SettingsState = {
  general: {
    platformName: '',
    supportEmail: '',
    defaultLanguage: '',
    timezone: '',
    adminName: '',
    adminEmail: '',
  },
  security: {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
    twoFactorEnabled: false,
    sessionTimeout: '',
  },
  integrations: {
    publicApiKey: '',
    secretApiKey: '',
    webhookUrl: '',
    paymentsEnabled: false,
    mailServiceEnabled: false,
    analyticsEnabled: false,
  },
  notifications: {
    emailNotifications: false,
    systemAlerts: false,
    inquiryAlerts: false,
  },
  systemControls: {
    registrationEnabled: false,
    maintenanceMode: false,
  },
};

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const GENERAL_SETTING_KEYS = {
  platformName: 'platform.name',
  supportEmail: 'support.email',
  defaultLanguage: 'platform.defaultLanguage',
  timezone: 'platform.timezone',
  adminName: 'admin.name',
  adminEmail: 'admin.email',
} as const;

type GeneralField = keyof SettingsState['general'];

const GENERAL_FIELDS: GeneralField[] = [
  'platformName',
  'supportEmail',
  'defaultLanguage',
  'timezone',
  'adminName',
  'adminEmail',
];

const normalizeSettingValue = (value: unknown) => (value == null ? '' : String(value).trim());

export const mapSettingsToUI = (settingsList: SystemSetting[]): SettingsState => {
  const settingsMap = new Map(settingsList.map((setting) => [setting.settingKey, setting.value]));

  return {
    ...defaultSettings,
    general: {
      platformName: normalizeSettingValue(settingsMap.get(GENERAL_SETTING_KEYS.platformName)),
      supportEmail: normalizeSettingValue(settingsMap.get(GENERAL_SETTING_KEYS.supportEmail)),
      defaultLanguage: normalizeSettingValue(settingsMap.get(GENERAL_SETTING_KEYS.defaultLanguage)),
      timezone: normalizeSettingValue(settingsMap.get(GENERAL_SETTING_KEYS.timezone)),
      adminName: normalizeSettingValue(settingsMap.get(GENERAL_SETTING_KEYS.adminName)),
      adminEmail: normalizeSettingValue(settingsMap.get(GENERAL_SETTING_KEYS.adminEmail)),
    },
  };
};

export const mapUIToSettingsPayload = (
  currentSettings: SettingsState,
  savedSettings: SettingsState
): Array<{ key: string; value: string }> => {
  return GENERAL_FIELDS.flatMap((field) => {
    const currentValue = normalizeSettingValue(currentSettings.general[field]);
    const savedValue = normalizeSettingValue(savedSettings.general[field]);

    if (currentValue === savedValue) {
      return [];
    }

    return [{ key: GENERAL_SETTING_KEYS[field], value: currentValue }];
  });
};

const Settings: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<SettingsTab>('general');
  const [settings, setSettings] = useState<SettingsState>(defaultSettings);
  const [savedSettings, setSavedSettings] = useState<SettingsState>(defaultSettings);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [currentAdmin, setCurrentAdmin] = useState<AdminCurrentUser | null>(null);
  const [verifiedSettingKeys, setVerifiedSettingKeys] = useState<string[]>([]);

  useEffect(() => {
    if (!isAdmin) return;

    const loadSettings = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const [backendSettings, adminIdentity] = await Promise.all([
          adminService.getSettings(),
          settingsService.getCurrentAdmin().catch(() => null),
        ]);
        const mappedSettings = mapSettingsToUI(backendSettings);
        if (adminIdentity) {
          mappedSettings.general.adminName = mappedSettings.general.adminName || adminIdentity.name || '';
          mappedSettings.general.adminEmail = mappedSettings.general.adminEmail || adminIdentity.email || '';
          setCurrentAdmin(adminIdentity);
        }
        setSettings(mappedSettings);
        setSavedSettings(mappedSettings);
      } catch (err: any) {
        setError(err?.message || 'Failed to load admin settings.');
      } finally {
        setIsLoading(false);
      }
    };

    loadSettings();
  }, [isAdmin]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 3000);
    return () => window.clearTimeout(timer);
  }, [success]);

  const hasChanges = useMemo(() => {
    return mapUIToSettingsPayload(settings, savedSettings).length > 0;
  }, [savedSettings, settings]);

  const adminDisplayName = useMemo(() => {
    return settings.general.adminName || localStorage.getItem('displayName') || 'System Admin';
  }, [settings.general.adminName]);

  const updateSection = <TSection extends keyof SettingsState, TField extends keyof SettingsState[TSection]>(
    section: TSection,
    field: TField,
    value: SettingsState[TSection][TField]
  ) => {
    setSettings((prev) => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value,
      },
    }));
  };

  const validateSettings = (): string | null => {
    if (!settings.general.platformName.trim()) return 'Platform name is required.';
    if (settings.general.supportEmail.trim() && !emailRegex.test(settings.general.supportEmail.trim())) {
      return 'Support email is invalid.';
    }
    if (settings.general.adminEmail.trim() && !emailRegex.test(settings.general.adminEmail.trim())) {
      return 'Admin email is invalid.';
    }
    return null;
  };

  const handleSave = async () => {
    setError(null);
    setSuccess(null);

    const validationError = validateSettings();
    if (validationError) {
      setError(validationError);
      return;
    }

    const changedSettings = mapUIToSettingsPayload(settings, savedSettings);
    if (changedSettings.length === 0) {
      return;
    }

    try {
      setIsSaving(true);
      setVerifiedSettingKeys([]);

      await Promise.all(
        changedSettings.map(({ key, value }) => adminService.updateSetting(key, { value }))
      );

      const verifiedKeys = await Promise.all(
        changedSettings.map(async ({ key }) => {
          const verified = await settingsService.getSettingByKey(key);
          return verified.settingKey;
        })
      );

      const adminNameChanged =
        normalizeSettingValue(settings.general.adminName) !== normalizeSettingValue(savedSettings.general.adminName);
      const adminEmailChanged =
        normalizeSettingValue(settings.general.adminEmail) !== normalizeSettingValue(savedSettings.general.adminEmail);

      if (currentAdmin && (adminNameChanged || adminEmailChanged)) {
        await settingsService.updateAdminDetails(currentAdmin.userId, {
          fullName: settings.general.adminName.trim() || currentAdmin.name,
          username: currentAdmin.username,
          password: '',
          email: settings.general.adminEmail.trim() || currentAdmin.email,
          roles: [3],
        });

        setCurrentAdmin((prev) =>
          prev
            ? {
                ...prev,
                name: settings.general.adminName.trim() || prev.name,
                email: settings.general.adminEmail.trim() || prev.email,
              }
            : prev
        );
      }

      setSavedSettings(settings);
      setVerifiedSettingKeys(verifiedKeys);
      setSuccess('Settings saved successfully.');
    } catch (err: any) {
      setError(err?.message || 'Failed to save settings.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleLogout = async () => {
    try {
      await authService.logout();
    } finally {
      navigate('/login');
    }
  };

  if (!isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AdminLayout>
      <div className="mb-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-4xl font-extrabold tracking-tight text-on-surface font-headline">Admin Settings</h1>
          <p className="mt-2 text-slate-500">Configure platform preferences through the live system settings API.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">{adminDisplayName}</div>
          <button type="button" onClick={handleLogout} className="rounded-full bg-slate-900 px-5 py-2.5 text-sm font-bold text-white hover:bg-slate-800">
            Logout
          </button>
        </div>
      </div>

      {error ? <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div> : null}
      {success ? <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}
      {verifiedSettingKeys.length > 0 ? (
        <div className="mb-4 rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-700">
          Verified keys from backend: {verifiedSettingKeys.join(', ')}
        </div>
      ) : null}

      {isLoading ? (
        <div className="flex min-h-[360px] items-center justify-center rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
          <div className="flex items-center gap-3 text-sm font-semibold text-slate-600">
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
            Loading settings...
          </div>
        </div>
      ) : (
        <>
          {currentAdmin ? (
            <div className="mb-5 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm ring-1 ring-slate-200">
              <p className="text-xs font-bold uppercase tracking-[0.24em] text-slate-500">Live Admin Identity</p>
              <div className="mt-3 grid gap-3 md:grid-cols-3">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Name</p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">{currentAdmin.name || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Email</p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">{currentAdmin.email || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Username</p>
                  <p className="mt-1 text-sm font-semibold text-slate-900">{currentAdmin.username || 'N/A'}</p>
                </div>
              </div>
            </div>
          ) : null}

          <SettingsForm
            activeTab={activeTab}
            onTabChange={setActiveTab}
            settings={settings}
            onGeneralChange={(field, value) => updateSection('general', field, value)}
            disabled={isSaving}
          />

          <div className="sticky bottom-4 z-20 mt-6 flex justify-end">
            <button
              type="button"
              onClick={handleSave}
              disabled={isSaving || !hasChanges}
              className="inline-flex items-center gap-2 rounded-full bg-gradient-to-br from-[#003d9b] to-[#0052cc] px-6 py-3 text-sm font-bold text-white shadow-lg shadow-blue-900/20 transition-all hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSaving ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                  Saving...
                </>
              ) : (
                <>
                  <span className="material-symbols-outlined text-base">save</span>
                  Save Settings
                </>
              )}
            </button>
          </div>
        </>
      )}
    </AdminLayout>
  );
};

export default Settings;
