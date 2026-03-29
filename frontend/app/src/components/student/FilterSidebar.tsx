import React from 'react';

export interface BrowseFilters {
  specialty: string;
  location: string;
  availableDays: string[];
  minHonorarium: string;
  maxHonorarium: string;
}

interface FilterSidebarProps {
  filters: BrowseFilters;
  onChange: (next: BrowseFilters) => void;
  onReset: () => void;
}

const SPECIALTY_OPTIONS = [
  'Family Medicine',
  'Internal Medicine',
  'Pediatrics',
  'Psychiatry',
  'Emergency Medicine',
  'Surgery',
  'Obstetrics & Gynecology',
  'Cardiology',
  'Neurology',
];

const DAY_OPTIONS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const toDayLabel = (value: string): string =>
  value.charAt(0) + value.slice(1).toLowerCase();

const FilterSidebar: React.FC<FilterSidebarProps> = ({ filters, onChange, onReset }) => {
  const toggleDay = (day: string) => {
    const exists = filters.availableDays.includes(day);
    const nextDays = exists
      ? filters.availableDays.filter((item) => item !== day)
      : [...filters.availableDays, day];

    onChange({
      ...filters,
      availableDays: nextDays,
    });
  };

  return (
    <aside className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-lg font-bold text-slate-900">Filters</h3>
        <button
          type="button"
          onClick={onReset}
          className="text-xs font-bold uppercase tracking-wider text-blue-700 hover:underline"
        >
          Reset
        </button>
      </div>

      <div className="space-y-4">
        <div>
          <label className="mb-1.5 block text-xs font-bold uppercase tracking-wider text-slate-500">Specialty</label>
          <select
            value={filters.specialty}
            onChange={(event) =>
              onChange({
                ...filters,
                specialty: event.target.value,
              })
            }
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
          >
            <option value="">All Specialties</option>
            {SPECIALTY_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1.5 block text-xs font-bold uppercase tracking-wider text-slate-500">Location</label>
          <input
            type="text"
            value={filters.location}
            onChange={(event) =>
              onChange({
                ...filters,
                location: event.target.value,
              })
            }
            placeholder="City, state, or region"
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
          />
        </div>

        <div>
          <p className="mb-1.5 text-xs font-bold uppercase tracking-wider text-slate-500">Available Days</p>
          <div className="grid grid-cols-2 gap-2">
            {DAY_OPTIONS.map((day) => (
              <label
                key={day}
                className={`flex cursor-pointer items-center gap-2 rounded-lg border px-2.5 py-2 text-xs font-semibold transition-colors ${
                  filters.availableDays.includes(day)
                    ? 'border-blue-200 bg-blue-50 text-blue-700'
                    : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                <input
                  type="checkbox"
                  checked={filters.availableDays.includes(day)}
                  onChange={() => toggleDay(day)}
                  className="h-3.5 w-3.5 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
                {toDayLabel(day)}
              </label>
            ))}
          </div>
        </div>

        <div>
          <p className="mb-1.5 text-xs font-bold uppercase tracking-wider text-slate-500">Honorarium Range</p>
          <div className="grid grid-cols-2 gap-2">
            <input
              type="number"
              min={0}
              value={filters.minHonorarium}
              onChange={(event) =>
                onChange({
                  ...filters,
                  minHonorarium: event.target.value,
                })
              }
              placeholder="Min"
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
            />
            <input
              type="number"
              min={0}
              value={filters.maxHonorarium}
              onChange={(event) =>
                onChange({
                  ...filters,
                  maxHonorarium: event.target.value,
                })
              }
              placeholder="Max"
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
            />
          </div>
        </div>
      </div>
    </aside>
  );
};

export default FilterSidebar;
