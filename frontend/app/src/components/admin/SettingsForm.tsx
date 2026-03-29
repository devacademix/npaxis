import React from 'react';
import ToggleSwitch from './ToggleSwitch';

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
  onSecurityChange: (field: keyof SettingsState['security'], value: string | boolean) => void;
  onIntegrationChange: (field: keyof SettingsState['integrations'], value: string | boolean) => void;
  onNotificationToggle: (field: keyof SettingsState['notifications'], value: boolean) => void;
  onSystemControlToggle: (field: keyof SettingsState['systemControls'], value: boolean) => void;
  onCopyKey: (value: string) => void;
  disabled?: boolean;
}

const tabs: Array<{ key: SettingsTab; label: string; icon: string }> = [
  { key: 'general', label: 'General', icon: 'tune' },
  { key: 'security', label: 'Security', icon: 'shield' },
  { key: 'integrations', label: 'API & Integrations', icon: 'hub' },
  { key: 'notifications', label: 'Notifications', icon: 'notifications' },
  { key: 'system-controls', label: 'System Controls', icon: 'settings_power' },
];

const SettingsForm: React.FC<SettingsFormProps> = ({
  activeTab,
  onTabChange,
  settings,
  onGeneralChange,
  onSecurityChange,
  onIntegrationChange,
  onNotificationToggle,
  onSystemControlToggle,
  onCopyKey,
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
          <div className="space-y-4">
            <h3 className="text-xl font-bold text-slate-900">Security Settings</h3>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Current Password</label>
                <input
                  type="password"
                  value={settings.security.currentPassword}
                  disabled={disabled}
                  onChange={(event) => onSecurityChange('currentPassword', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                  placeholder="Required to update admin profile"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">New Password</label>
                <input
                  type="password"
                  value={settings.security.newPassword}
                  disabled={disabled}
                  onChange={(event) => onSecurityChange('newPassword', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Confirm Password</label>
                <input
                  type="password"
                  value={settings.security.confirmPassword}
                  disabled={disabled}
                  onChange={(event) => onSecurityChange('confirmPassword', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                />
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Session Timeout</label>
                <select
                  value={settings.security.sessionTimeout}
                  disabled={disabled}
                  onChange={(event) => onSecurityChange('sessionTimeout', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                >
                  <option value="15">15 minutes</option>
                  <option value="30">30 minutes</option>
                  <option value="60">60 minutes</option>
                  <option value="120">120 minutes</option>
                </select>
              </div>
            </div>
            <ToggleSwitch
              checked={settings.security.twoFactorEnabled}
              disabled={disabled}
              onChange={(checked) => onSecurityChange('twoFactorEnabled', checked)}
              label="Enable Two-Factor Authentication"
              description="Add extra account protection for admin logins."
            />
          </div>
        ) : null}

        {activeTab === 'integrations' ? (
          <div className="space-y-4">
            <h3 className="text-xl font-bold text-slate-900">API & Integrations</h3>
            <div className="space-y-3">
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Public API Key</label>
                <div className="flex gap-2">
                  <input
                    readOnly
                    value={settings.integrations.publicApiKey}
                    className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm"
                  />
                  <button
                    type="button"
                    onClick={() => onCopyKey(settings.integrations.publicApiKey)}
                    className="rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                  >
                    Copy
                  </button>
                </div>
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Secret API Key</label>
                <div className="flex gap-2">
                  <input
                    readOnly
                    value={settings.integrations.secretApiKey}
                    className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm"
                  />
                  <button
                    type="button"
                    onClick={() => onCopyKey(settings.integrations.secretApiKey)}
                    className="rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                  >
                    Copy
                  </button>
                </div>
              </div>
              <div>
                <label className="mb-1 block text-[11px] font-bold uppercase tracking-wider text-slate-500">Webhook URL</label>
                <input
                  value={settings.integrations.webhookUrl}
                  disabled={disabled}
                  onChange={(event) => onIntegrationChange('webhookUrl', event.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                  placeholder="https://example.com/webhook"
                />
              </div>
            </div>
            <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
              <ToggleSwitch
                checked={settings.integrations.paymentsEnabled}
                onChange={(checked) => onIntegrationChange('paymentsEnabled', checked)}
                disabled={disabled}
                label="Payments Integration"
                description="Enable payment gateway integration."
              />
              <ToggleSwitch
                checked={settings.integrations.mailServiceEnabled}
                onChange={(checked) => onIntegrationChange('mailServiceEnabled', checked)}
                disabled={disabled}
                label="Mail Service Integration"
                description="Enable transactional email service."
              />
              <ToggleSwitch
                checked={settings.integrations.analyticsEnabled}
                onChange={(checked) => onIntegrationChange('analyticsEnabled', checked)}
                disabled={disabled}
                label="Analytics Integration"
                description="Send events to analytics pipeline."
              />
            </div>
          </div>
        ) : null}

        {activeTab === 'notifications' ? (
          <div className="space-y-4">
            <h3 className="text-xl font-bold text-slate-900">Notification Settings</h3>
            <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
              <ToggleSwitch
                checked={settings.notifications.emailNotifications}
                onChange={(checked) => onNotificationToggle('emailNotifications', checked)}
                disabled={disabled}
                label="Email Notifications"
                description="Receive admin updates via email."
              />
              <ToggleSwitch
                checked={settings.notifications.systemAlerts}
                onChange={(checked) => onNotificationToggle('systemAlerts', checked)}
                disabled={disabled}
                label="System Alerts"
                description="Alert on critical platform incidents."
              />
              <ToggleSwitch
                checked={settings.notifications.inquiryAlerts}
                onChange={(checked) => onNotificationToggle('inquiryAlerts', checked)}
                disabled={disabled}
                label="Inquiry Alerts"
                description="Notify on new user inquiries."
              />
            </div>
          </div>
        ) : null}

        {activeTab === 'system-controls' ? (
          <div className="space-y-4">
            <h3 className="text-xl font-bold text-slate-900">System Controls</h3>
            <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
              <ToggleSwitch
                checked={settings.systemControls.registrationEnabled}
                onChange={(checked) => onSystemControlToggle('registrationEnabled', checked)}
                disabled={disabled}
                label="Enable User Registration"
                description="Allow new users to sign up on the platform."
              />
              <ToggleSwitch
                checked={settings.systemControls.maintenanceMode}
                onChange={(checked) => onSystemControlToggle('maintenanceMode', checked)}
                disabled={disabled}
                label="Maintenance Mode"
                description="Restrict platform access for maintenance windows."
              />
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default SettingsForm;
