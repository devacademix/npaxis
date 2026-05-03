import React from 'react';

export type SettingsTab = 'general' | 'security' | 'integrations' | 'notifications' | 'system-controls';

export interface SettingsState {
  general: {
    platformName: string;
    supportEmail: string;
    defaultLanguage: string;
    timezone: string;
    adminName: string;
    adminEmail: string;
  };
  security: {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
    twoFactorEnabled: boolean;
    sessionTimeout: string;
  };
  integrations: {
    publicApiKey: string;
    secretApiKey: string;
    webhookUrl: string;
    paymentsEnabled: boolean;
    mailServiceEnabled: boolean;
    analyticsEnabled: boolean;
  };
  notifications: {
    emailNotifications: boolean;
    systemAlerts: boolean;
    inquiryAlerts: boolean;
  };
  systemControls: {
    registrationEnabled: boolean;
    maintenanceMode: boolean;
  };
}

interface SettingsFormProps {
  activeTab: SettingsTab;
  onTabChange: (tab: SettingsTab) => void;
  settings: SettingsState;
  onGeneralChange: (field: keyof SettingsState['general'], value: string) => void;
  disabled?: boolean;
}

const tabs: Array<{ key: SettingsTab; label: string; icon: string }> = [
  { key: 'general', label: 'General', icon: 'tune' },
  { key: 'security', label: 'Security', icon: 'shield' },
  { key: 'integrations', label: 'API & Integrations', icon: 'hub' },
  { key: 'notifications', label: 'Notifications', icon: 'notifications' },
  { key: 'system-controls', label: 'System Controls', icon: 'settings_power' },
];

const ReadOnlyPlaceholder = ({
  title,
  description,
}: {
  title: string;
  description: string;
}) => (
  <div className="space-y-4">
    <h3 className="text-xl font-bold text-slate-900">{title}</h3>
    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
      <div className="flex items-start gap-3">
        <div className="rounded-full bg-slate-200 p-2 text-slate-600">
          <span className="material-symbols-outlined text-base">lock</span>
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-900">Read-only placeholder</p>
          <p className="mt-1 text-sm text-slate-600">{description}</p>
        </div>
      </div>
    </div>
  </div>
);

const SettingsForm: React.FC<SettingsFormProps> = ({
  activeTab,
  onTabChange,
  settings,
  onGeneralChange,
  disabled = false,
}) => {
  return (
    <div className="space-y-5">
      <div className="overflow-x-auto">
        <div className="inline-flex rounded-xl bg-slate-100 p-1 shadow-sm ring-1 ring-slate-200">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => onTabChange(tab.key)}
              className={`inline-flex items-center gap-2 rounded-lg px-4 py-2 text-xs font-bold uppercase tracking-wider transition-all ${
                activeTab === tab.key
                  ? 'bg-white text-blue-700 shadow-sm'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <span className="material-symbols-outlined text-base">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 transition-all duration-300">
        {activeTab === 'general' ? (
          <div className="space-y-4">
            <h3 className="text-xl font-bold text-slate-900">General Settings</h3>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Platform Name</label>
                <input
                  value={settings.general.platformName}
                  disabled={disabled}
                  onChange={(event) => onGeneralChange('platformName', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Support Email</label>
                <input
                  type="email"
                  value={settings.general.supportEmail}
                  disabled={disabled}
                  onChange={(event) => onGeneralChange('supportEmail', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Default Language</label>
                <select
                  value={settings.general.defaultLanguage}
                  disabled={disabled}
                  onChange={(event) => onGeneralChange('defaultLanguage', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                >
                  <option value="English">English</option>
                  <option value="Hindi">Hindi</option>
                  <option value="Spanish">Spanish</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Timezone</label>
                <select
                  value={settings.general.timezone}
                  disabled={disabled}
                  onChange={(event) => onGeneralChange('timezone', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                >
                  <option value="Asia/Kolkata">Asia/Kolkata</option>
                  <option value="UTC">UTC</option>
                  <option value="America/New_York">America/New_York</option>
                </select>
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Admin Name</label>
                <input
                  value={settings.general.adminName}
                  disabled={disabled}
                  onChange={(event) => onGeneralChange('adminName', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Admin Email</label>
                <input
                  type="email"
                  value={settings.general.adminEmail}
                  disabled={disabled}
                  onChange={(event) => onGeneralChange('adminEmail', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
              </div>
            </div>
          </div>
        ) : null}

        {activeTab === 'security' ? (
          <ReadOnlyPlaceholder
            title="Security Settings"
            description="Security controls are currently visible in read-only mode. Backend system settings support is enabled only for General settings right now."
          />
        ) : null}

        {activeTab === 'integrations' ? (
          <ReadOnlyPlaceholder
            title="API & Integrations"
            description="Integration settings remain available as a read-only placeholder until matching backend system setting keys are introduced."
          />
        ) : null}

        {activeTab === 'notifications' ? (
          <ReadOnlyPlaceholder
            title="Notification Settings"
            description="Notification preferences are currently shown as a placeholder. Only General settings are connected to backend read/write APIs in this version."
          />
        ) : null}

        {activeTab === 'system-controls' ? (
          <ReadOnlyPlaceholder
            title="System Controls"
            description="System control options are intentionally read-only here so the UI stays aligned with the current backend contract."
          />
        ) : null}
      </div>
    </div>
  );
};

export default SettingsForm;
