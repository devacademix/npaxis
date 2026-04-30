import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AdminLayout from '../../components/layout/AdminLayout';
import SettingsForm, { type SettingsState, type SettingsTab } from '../../components/admin/SettingsForm';
import { authService } from '../../services/auth';
import settingsService, { type AdminCurrentUser, type PlatformSetting } from '../../services/settings';
import userService from '../../services/user';

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
    publicApiKey: 'pk_live_3f9a0b1c2d4e',
    secretApiKey: 'sk_live_8f2d1e6a7c9b',
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

const mergeSettings = (base: SettingsState, overrides: Partial<SettingsState>): SettingsState => ({
  general: { ...base.general, ...(overrides.general ?? {}) },
  security: { ...base.security, ...(overrides.security ?? {}) },
  integrations: { ...base.integrations, ...(overrides.integrations ?? {}) },
  notifications: { ...base.notifications, ...(overrides.notifications ?? {}) },
  systemControls: { ...base.systemControls, ...(overrides.systemControls ?? {}) },
});

const normalizeSettingKey = (key: string) => key.toLowerCase().replace(/[^a-z0-9]/g, '');

const applyBackendSettings = (base: SettingsState, platformSettings: PlatformSetting[]): SettingsState => {
  const next = mergeSettings(base, {});

  platformSettings.forEach((setting) => {
    const normalized = normalizeSettingKey(setting.key);
    const value = setting.value;

    if (normalized.includes('platform') && normalized.includes('name')) {
      next.general.platformName = String(value ?? next.general.platformName);
    } else if (normalized.includes('support') && normalized.includes('email')) {
      next.general.supportEmail = String(value ?? next.general.supportEmail);
    } else if (normalized.includes('language')) {
      next.general.defaultLanguage = String(value ?? next.general.defaultLanguage);
    } else if (normalized.includes('timezone')) {
      next.general.timezone = String(value ?? next.general.timezone);
    } else if (normalized.includes('public') && normalized.includes('api')) {
      next.integrations.publicApiKey = String(value ?? next.integrations.publicApiKey);
    } else if (normalized.includes('secret') && normalized.includes('api')) {
      next.integrations.secretApiKey = String(value ?? next.integrations.secretApiKey);
    } else if (normalized.includes('webhook')) {
      next.integrations.webhookUrl = String(value ?? next.integrations.webhookUrl);
    } else if (normalized.includes('payment') && normalized.includes('enabled')) {
      next.integrations.paymentsEnabled = Boolean(value);
    } else if (normalized.includes('mail') && normalized.includes('enabled')) {
      next.integrations.mailServiceEnabled = Boolean(value);
    } else if (normalized.includes('analytics') && normalized.includes('enabled')) {
      next.integrations.analyticsEnabled = Boolean(value);
    } else if (normalized.includes('email') && normalized.includes('notification')) {
      next.notifications.emailNotifications = Boolean(value);
    } else if (normalized.includes('system') && normalized.includes('alert')) {
      next.notifications.systemAlerts = Boolean(value);
    } else if (normalized.includes('inquiry') && normalized.includes('alert')) {
      next.notifications.inquiryAlerts = Boolean(value);
    } else if (normalized.includes('registration') && normalized.includes('enabled')) {
      next.systemControls.registrationEnabled = Boolean(value);
    } else if (normalized.includes('maintenance') && normalized.includes('mode')) {
      next.systemControls.maintenanceMode = Boolean(value);
    } else if (normalized.includes('twofactor')) {
      next.security.twoFactorEnabled = Boolean(value);
    } else if (normalized.includes('session') && normalized.includes('timeout')) {
      next.security.sessionTimeout = String(value ?? next.security.sessionTimeout);
    }
  });

  return next;
};

const Settings: React.FC = () => {
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ADMIN' || role === 'ROLE_ADMIN';
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<SettingsTab>('general');
  const [settings, setSettings] = useState<SettingsState>(() => {
    const cached = settingsService.getTemporarySettings<SettingsState>();
    return cached ? mergeSettings(defaultSettings, cached) : defaultSettings;
  });
  const [adminUser, setAdminUser] = useState<AdminCurrentUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [platformSettings, setPlatformSettings] = useState<PlatformSetting[]>([]);
  const [profileImageUrl, setProfileImageUrl] = useState<string | null>(null);
  const [profileImageFile, setProfileImageFile] = useState<File | null>(null);
  const [isUploadingImage, setIsUploadingImage] = useState(false);

  useEffect(() => {
    if (!isAdmin) return;
    const loadAdmin = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const [currentAdmin, backendSettings] = await Promise.all([
          settingsService.getCurrentAdmin(),
          settingsService.getPlatformSettings().catch(() => []),
        ]);
        setAdminUser(currentAdmin);
        try {
          const blob = await userService.getProfilePictureBlob(currentAdmin.userId);
          setProfileImageUrl(URL.createObjectURL(blob));
        } catch {
          setProfileImageUrl(null);
        }
        setPlatformSettings(backendSettings);
        setSettings((prev) => {
          const withBackend = applyBackendSettings(prev, backendSettings);
          return {
            ...withBackend,
            general: {
              ...withBackend.general,
              adminName: withBackend.general.adminName || currentAdmin.name || '',
              adminEmail: withBackend.general.adminEmail || currentAdmin.email || '',
              supportEmail:
                withBackend.general.supportEmail === defaultSettings.general.supportEmail && currentAdmin.email
                  ? currentAdmin.email
                  : withBackend.general.supportEmail,
            },
          };
        });
      } catch (err: any) {
        setError(err?.message || 'Failed to load admin settings.');
      } finally {
        setIsLoading(false);
      }
    };
    loadAdmin();
  }, [isAdmin]);

  useEffect(() => {
    return () => {
      if (profileImageUrl) {
        URL.revokeObjectURL(profileImageUrl);
      }
    };
  }, [profileImageUrl]);

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
    if (!settings.general.platformName.trim()) {
      return 'Platform name is required.';
    }
    if (!emailRegex.test(settings.general.supportEmail.trim())) {
      return 'Support email is invalid.';
    }
    if (!emailRegex.test(settings.general.adminEmail.trim())) {
      return 'Admin email is invalid.';
    }
    if (settings.integrations.webhookUrl.trim()) {
      try {
        // URL validation
        new URL(settings.integrations.webhookUrl.trim());
      } catch {
        return 'Webhook URL must be a valid URL.';
      }
    }
    const hasPasswordInput =
      settings.security.currentPassword.trim() ||
      settings.security.newPassword.trim() ||
      settings.security.confirmPassword.trim();
    if (hasPasswordInput) {
      if (!settings.security.currentPassword.trim()) {
        return 'Current password is required when changing password.';
      }
      if (settings.security.newPassword.trim() && settings.security.newPassword.trim().length < 8) {
        return 'New password must be at least 8 characters long.';
      }
      if (settings.security.newPassword.trim() !== settings.security.confirmPassword.trim()) {
        return 'New password and confirm password do not match.';
      }
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

      let profileMessage = 'Settings saved locally.';

      const profilePassword = settings.security.newPassword.trim() || settings.security.currentPassword.trim();
      if (adminUser && profilePassword) {
        await settingsService.updateAdminDetails(adminUser.userId, {
          fullName: settings.general.adminName.trim() || adminUser.name,
          username: adminUser.username,
          password: profilePassword,
          email: settings.general.adminEmail.trim(),
          roles: [3],
        });
        profileMessage = 'Admin profile updated and settings saved.';
      } else if (adminUser) {
        profileMessage =
          'Profile not updated (enter current password to apply profile changes). Other settings saved locally.';
      }

      const platformValueMap = new Map<string, unknown>();
      platformSettings.forEach((setting) => {
        const normalized = normalizeSettingKey(setting.key);
        if (normalized.includes('platform') && normalized.includes('name')) {
          platformValueMap.set(setting.key, settings.general.platformName.trim());
        } else if (normalized.includes('support') && normalized.includes('email')) {
          platformValueMap.set(setting.key, settings.general.supportEmail.trim());
        } else if (normalized.includes('language')) {
          platformValueMap.set(setting.key, settings.general.defaultLanguage);
        } else if (normalized.includes('timezone')) {
          platformValueMap.set(setting.key, settings.general.timezone);
        } else if (normalized.includes('public') && normalized.includes('api')) {
          platformValueMap.set(setting.key, settings.integrations.publicApiKey);
        } else if (normalized.includes('secret') && normalized.includes('api')) {
          platformValueMap.set(setting.key, settings.integrations.secretApiKey);
        } else if (normalized.includes('webhook')) {
          platformValueMap.set(setting.key, settings.integrations.webhookUrl.trim());
        } else if (normalized.includes('payment') && normalized.includes('enabled')) {
          platformValueMap.set(setting.key, settings.integrations.paymentsEnabled);
        } else if (normalized.includes('mail') && normalized.includes('enabled')) {
          platformValueMap.set(setting.key, settings.integrations.mailServiceEnabled);
        } else if (normalized.includes('analytics') && normalized.includes('enabled')) {
          platformValueMap.set(setting.key, settings.integrations.analyticsEnabled);
        } else if (normalized.includes('email') && normalized.includes('notification')) {
          platformValueMap.set(setting.key, settings.notifications.emailNotifications);
        } else if (normalized.includes('system') && normalized.includes('alert')) {
          platformValueMap.set(setting.key, settings.notifications.systemAlerts);
        } else if (normalized.includes('inquiry') && normalized.includes('alert')) {
          platformValueMap.set(setting.key, settings.notifications.inquiryAlerts);
        } else if (normalized.includes('registration') && normalized.includes('enabled')) {
          platformValueMap.set(setting.key, settings.systemControls.registrationEnabled);
        } else if (normalized.includes('maintenance') && normalized.includes('mode')) {
          platformValueMap.set(setting.key, settings.systemControls.maintenanceMode);
        } else if (normalized.includes('twofactor')) {
          platformValueMap.set(setting.key, settings.security.twoFactorEnabled);
        } else if (normalized.includes('session') && normalized.includes('timeout')) {
          platformValueMap.set(setting.key, settings.security.sessionTimeout);
        }
      });

      const changedEntries = Array.from(platformValueMap.entries());
      if (changedEntries.length > 0) {
        await Promise.all(changedEntries.map(([key, value]) => settingsService.updatePlatformSetting(key, value)));
        const refreshedSetting = await settingsService.getPlatformSetting(changedEntries[0][0]);
        setPlatformSettings((prev) => {
          const remainder = prev.filter((item) => item.key !== refreshedSetting.key);
          return [...remainder, refreshedSetting];
        });
      }

      await settingsService.saveTemporarySettings({
        ...settings,
        security: {
          ...settings.security,
          currentPassword: '',
          newPassword: '',
          confirmPassword: '',
        },
      });

      setSettings((prev) => ({
        ...prev,
        security: {
          ...prev.security,
          currentPassword: '',
          newPassword: '',
          confirmPassword: '',
        },
      }));
      setSuccess(changedEntries.length > 0 ? `${profileMessage} Platform settings synced.` : profileMessage);
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
      } else {
        const input = document.createElement('input');
        input.value = value;
        document.body.appendChild(input);
        input.select();
        document.execCommand('copy');
        document.body.removeChild(input);
      }
      setSuccess('Copied to clipboard.');
    } catch {
      setError('Failed to copy API key.');
    }
  };

  const handleProfileImageUpload = async () => {
    if (!adminUser || !profileImageFile) return;
    try {
      setIsUploadingImage(true);
      setError(null);
      await userService.uploadProfilePicture(adminUser.userId, profileImageFile);
      const blob = await userService.getProfilePictureBlob(adminUser.userId);
      setProfileImageUrl((previous) => {
        if (previous) URL.revokeObjectURL(previous);
        return URL.createObjectURL(blob);
      });
      setProfileImageFile(null);
      setSuccess('Profile picture updated successfully.');
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile picture.');
    } finally {
      setIsUploadingImage(false);
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
          <div className="rounded-full bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">
            {adminDisplayName}
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-full bg-slate-900 px-5 py-2.5 text-sm font-bold text-white hover:bg-slate-800"
          >
            Logout
          </button>
        </div>
      </div>

      {error ? (
        <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      ) : null}

      {success ? (
        <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
          {success}
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
          <div className="mb-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div className="flex items-center gap-4">
                <div className="flex h-20 w-20 items-center justify-center overflow-hidden rounded-full border border-slate-200 bg-slate-100">
                  {profileImageUrl ? (
                    <img src={profileImageUrl} alt={adminDisplayName} className="h-full w-full object-cover" />
                  ) : (
                    <span className="material-symbols-outlined text-4xl text-slate-400">admin_panel_settings</span>
                  )}
                </div>
                <div>
                  <h2 className="text-lg font-bold text-slate-900">Profile Picture</h2>
                  <p className="text-sm text-slate-500">Upload your current admin profile image.</p>
                </div>
              </div>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                <input
                  type="file"
                  accept="image/*"
                  disabled={isUploadingImage}
                  onChange={(event) => setProfileImageFile(event.target.files?.[0] ?? null)}
                  className="rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
                <button
                  type="button"
                  onClick={handleProfileImageUpload}
                  disabled={!profileImageFile || isUploadingImage}
                  className="rounded-full bg-blue-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-700 disabled:opacity-60"
                >
                  {isUploadingImage ? 'Uploading...' : 'Upload Image'}
                </button>
              </div>
            </div>
          </div>
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
