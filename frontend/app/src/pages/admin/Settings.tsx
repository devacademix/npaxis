import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import SettingsForm, { type SettingsState, type SettingsTab } from '../../components/admin/SettingsForm';
import { authService } from '../../services/auth';
import settingsService, { type AdminCurrentUser, type SystemSetting } from '../../services/settings';

const defaultSettings: SettingsState = {
  general: {
    platformName: 'NPaxis',
    supportEmail: 'support@npaxis.com',
    defaultLanguage: 'English',
    timezone: 'Asia/Kolkata',
    adminName: '',
    adminEmail: '',
  },
  security: {
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
    twoFactorEnabled: false,
    sessionTimeout: '30',
  },
  integrations: {
    publicApiKey: 'Configured on server',
    secretApiKey: 'Hidden on server',
    webhookUrl: '',
    paymentsEnabled: true,
    mailServiceEnabled: true,
    analyticsEnabled: false,
  },
  notifications: {
    emailNotifications: true,
    systemAlerts: true,
    inquiryAlerts: true,
  },
  systemControls: {
    registrationEnabled: true,
    maintenanceMode: false,
  },
};

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const SETTING_KEYS = {
  platformName: 'platformName',
  supportEmail: 'supportEmail',
  defaultLanguage: 'defaultLanguage',
  timezone: 'timezone',
  webhookUrl: 'webhookUrl',
  paymentsEnabled: 'paymentsEnabled',
  mailServiceEnabled: 'mailServiceEnabled',
  analyticsEnabled: 'analyticsEnabled',
  emailNotifications: 'emailNotifications',
  systemAlerts: 'systemAlerts',
  inquiryAlerts: 'inquiryAlerts',
  registrationEnabled: 'registrationEnabled',
  maintenanceMode: 'maintenanceMode',
};

const mapSettings = (settingsList: SystemSetting[], admin: AdminCurrentUser): SettingsState => {
  const settingsMap = new Map(settingsList.map((setting) => [setting.settingKey, setting.value]));
  return {
    ...defaultSettings,
    general: {
      ...defaultSettings.general,
      platformName: String(settingsMap.get(SETTING_KEYS.platformName) ?? defaultSettings.general.platformName),
      supportEmail: String(settingsMap.get(SETTING_KEYS.supportEmail) ?? admin.email ?? defaultSettings.general.supportEmail),
      defaultLanguage: String(settingsMap.get(SETTING_KEYS.defaultLanguage) ?? defaultSettings.general.defaultLanguage),
      timezone: String(settingsMap.get(SETTING_KEYS.timezone) ?? defaultSettings.general.timezone),
      adminName: admin.name || '',
      adminEmail: admin.email || '',
    },
    security: {
      ...defaultSettings.security,
    },
    integrations: {
      ...defaultSettings.integrations,
      webhookUrl: String(settingsMap.get(SETTING_KEYS.webhookUrl) ?? ''),
      paymentsEnabled: Boolean(settingsMap.get(SETTING_KEYS.paymentsEnabled) ?? defaultSettings.integrations.paymentsEnabled),
      mailServiceEnabled: Boolean(settingsMap.get(SETTING_KEYS.mailServiceEnabled) ?? defaultSettings.integrations.mailServiceEnabled),
      analyticsEnabled: Boolean(settingsMap.get(SETTING_KEYS.analyticsEnabled) ?? defaultSettings.integrations.analyticsEnabled),
    },
    notifications: {
      ...defaultSettings.notifications,
      emailNotifications: Boolean(settingsMap.get(SETTING_KEYS.emailNotifications) ?? defaultSettings.notifications.emailNotifications),
      systemAlerts: Boolean(settingsMap.get(SETTING_KEYS.systemAlerts) ?? defaultSettings.notifications.systemAlerts),
      inquiryAlerts: Boolean(settingsMap.get(SETTING_KEYS.inquiryAlerts) ?? defaultSettings.notifications.inquiryAlerts),
    },
    systemControls: {
      ...defaultSettings.systemControls,
      registrationEnabled: Boolean(settingsMap.get(SETTING_KEYS.registrationEnabled) ?? defaultSettings.systemControls.registrationEnabled),
      maintenanceMode: Boolean(settingsMap.get(SETTING_KEYS.maintenanceMode) ?? defaultSettings.systemControls.maintenanceMode),
    },
  };
};

const Settings: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<SettingsTab>('general');
  const [settings, setSettings] = useState<SettingsState>(defaultSettings);
  const [adminUser, setAdminUser] = useState<AdminCurrentUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (!isAdmin) return;
    const loadAdmin = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const [currentAdmin, backendSettings] = await Promise.all([
          settingsService.getCurrentAdmin(),
          settingsService.getAllSettings().catch(() => []),
        ]);
        setAdminUser(currentAdmin);
        setSettings(mapSettings(backendSettings, currentAdmin));
      } catch (err: any) {
        setError(err?.message || 'Failed to load admin settings.');
      } finally {
        setIsLoading(false);
      }
    };
    loadAdmin();
  }, [isAdmin]);

  useEffect(() => {
    if (!success) return;
    const timer = window.setTimeout(() => setSuccess(null), 3000);
    return () => window.clearTimeout(timer);
  }, [success]);

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
    if (!emailRegex.test(settings.general.supportEmail.trim())) return 'Support email is invalid.';
    if (!emailRegex.test(settings.general.adminEmail.trim())) return 'Admin email is invalid.';
    if (settings.integrations.webhookUrl.trim()) {
      try {
        new URL(settings.integrations.webhookUrl.trim());
      } catch {
        return 'Webhook URL must be a valid URL.';
      }
    }
    if (settings.security.newPassword && settings.security.newPassword.length < 8) {
      return 'New password must be at least 8 characters long.';
    }
    if (settings.security.newPassword !== settings.security.confirmPassword) {
      return 'New password and confirm password do not match.';
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

    try {
      setIsSaving(true);

      if (adminUser && settings.security.currentPassword.trim()) {
        await settingsService.updateAdminDetails(adminUser.userId, {
          fullName: settings.general.adminName.trim() || adminUser.name,
          username: adminUser.username,
          password: settings.security.newPassword.trim() || settings.security.currentPassword.trim(),
          email: settings.general.adminEmail.trim(),
          roles: [3],
        });
      }

      const updates: Array<[string, any]> = [
        [SETTING_KEYS.platformName, settings.general.platformName.trim()],
        [SETTING_KEYS.supportEmail, settings.general.supportEmail.trim()],
        [SETTING_KEYS.defaultLanguage, settings.general.defaultLanguage],
        [SETTING_KEYS.timezone, settings.general.timezone],
        [SETTING_KEYS.webhookUrl, settings.integrations.webhookUrl.trim()],
        [SETTING_KEYS.paymentsEnabled, settings.integrations.paymentsEnabled],
        [SETTING_KEYS.mailServiceEnabled, settings.integrations.mailServiceEnabled],
        [SETTING_KEYS.analyticsEnabled, settings.integrations.analyticsEnabled],
        [SETTING_KEYS.emailNotifications, settings.notifications.emailNotifications],
        [SETTING_KEYS.systemAlerts, settings.notifications.systemAlerts],
        [SETTING_KEYS.inquiryAlerts, settings.notifications.inquiryAlerts],
        [SETTING_KEYS.registrationEnabled, settings.systemControls.registrationEnabled],
        [SETTING_KEYS.maintenanceMode, settings.systemControls.maintenanceMode],
      ];

      for (const [key, value] of updates) {
        await settingsService.updateSetting(key, value);
      }

      setSettings((prev) => ({
        ...prev,
        security: {
          ...prev.security,
          currentPassword: '',
          newPassword: '',
          confirmPassword: '',
        },
      }));
      setSuccess('Admin settings saved successfully.');
    } catch (err: any) {
      setError(err?.message || 'Failed to save settings.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCopyKey = async (value: string) => {
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(value);
      }
      setSuccess('Copied to clipboard.');
    } catch {
      setError('Failed to copy API key.');
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
          <p className="mt-2 text-slate-500">Configure platform preferences, security, and integrations.</p>
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

      {isLoading ? (
        <div className="flex min-h-[360px] items-center justify-center rounded-2xl bg-white shadow-sm ring-1 ring-slate-200">
          <div className="flex items-center gap-3 text-sm font-semibold text-slate-600">
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-blue-600" />
            Loading settings...
          </div>
        </div>
      ) : (
        <>
          <SettingsForm
            activeTab={activeTab}
            onTabChange={setActiveTab}
            settings={settings}
            onGeneralChange={(field, value) => updateSection('general', field, value)}
            onSecurityChange={(field, value) => updateSection('security', field, value as never)}
            onIntegrationChange={(field, value) => updateSection('integrations', field, value as never)}
            onNotificationToggle={(field, value) => updateSection('notifications', field, value)}
            onSystemControlToggle={(field, value) => updateSection('systemControls', field, value)}
            onCopyKey={handleCopyKey}
            disabled={isSaving}
          />

          <div className="sticky bottom-4 z-20 mt-6 flex justify-end">
            <button
              type="button"
              onClick={handleSave}
              disabled={isSaving}
              className="inline-flex items-center gap-2 rounded-full bg-gradient-to-br from-[#003d9b] to-[#0052cc] px-6 py-3 text-sm font-bold text-white shadow-lg shadow-blue-900/20 transition-all hover:opacity-90 disabled:opacity-60"
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
