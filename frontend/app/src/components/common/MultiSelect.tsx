import React from 'react';

interface MultiSelectOption {
  label: string;
  value: string;
}

interface MultiSelectProps {
  label: string;
  options: MultiSelectOption[];
  selectedValues: string[];
  onChange: (nextValues: string[]) => void;
  disabled?: boolean;
}

const MultiSelect: React.FC<MultiSelectProps> = ({
  label,
  options,
  selectedValues,
  onChange,
  disabled = false,
}) => {
  const toggleOption = (value: string) => {
    if (disabled) return;
    const isSelected = selectedValues.includes(value);
    if (isSelected) {
      onChange(selectedValues.filter((item) => item !== value));
      return;
    }
    onChange([...selectedValues, value]);
  };

  return (
    <div>
      <label className="mb-2 block text-xs font-bold uppercase tracking-wider text-slate-500">{label}</label>
      <div className="grid grid-cols-2 gap-2 md:grid-cols-4">
        {options.map((option) => {
          const active = selectedValues.includes(option.value);
          return (
            <button
              key={option.value}
              type="button"
              onClick={() => toggleOption(option.value)}
              disabled={disabled}
              className={`inline-flex items-center justify-center gap-1 rounded-lg border px-3 py-2 text-xs font-semibold transition-all ${
                active
                  ? 'border-blue-300 bg-blue-50 text-blue-700'
                  : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'
              } disabled:cursor-not-allowed disabled:opacity-60`}
            >
              {active ? <span className="material-symbols-outlined text-sm">check_small</span> : null}
              {option.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default MultiSelect;
