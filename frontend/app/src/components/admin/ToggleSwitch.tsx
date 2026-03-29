import React from 'react';

interface ToggleSwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  label: string;
  description?: string;
  disabled?: boolean;
}

const ToggleSwitch: React.FC<ToggleSwitchProps> = ({
  checked,
  onChange,
  label,
  description,
  disabled = false,
}) => {
  return (
    <div className="flex items-start justify-between gap-4 rounded-lg border border-slate-200 bg-white p-3">
      <div>
        <p className="text-sm font-semibold text-slate-800">{label}</p>
        {description ? <p className="mt-1 text-xs text-slate-500">{description}</p> : null}
      </div>
      <label className="inline-flex cursor-pointer items-center">
        <input
          type="checkbox"
          checked={checked}
          disabled={disabled}
          onChange={(event) => onChange(event.target.checked)}
          className="peer sr-only"
        />
        <span className="relative h-6 w-11 rounded-full bg-slate-200 transition-colors peer-checked:bg-blue-600 peer-disabled:cursor-not-allowed peer-disabled:opacity-50">
          <span className="absolute left-0.5 top-0.5 h-5 w-5 rounded-full bg-white transition-transform peer-checked:translate-x-5" />
        </span>
      </label>
    </div>
  );
};

export default ToggleSwitch;
