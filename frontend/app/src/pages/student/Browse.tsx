import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import FilterSidebar, { type BrowseFilters } from '../../components/student/FilterSidebar';
import PreceptorCard from '../../components/student/PreceptorCard';
import { preceptorService, type PreceptorSearchItem } from '../../services/preceptor';
import type { StudentPreceptor } from '../../services/student';

const PAGE_SIZE = 9;
const FETCH_BATCH_SIZE = 120;

const defaultFilters: BrowseFilters = {
  specialty: '',
  location: '',
  availableDays: [],
  minHonorarium: '',
  maxHonorarium: '',
};

const parseHonorarium = (value?: string): number | null => {
  if (!value) return null;
  const match = value.replace(/,/g, '').match(/\d+(\.\d+)?/);
  if (!match) return null;
  const parsed = Number(match[0]);
  return Number.isFinite(parsed) ? parsed : null;
};

const mapSearchItemToStudentPreceptor = (item: PreceptorSearchItem): StudentPreceptor => ({
  userId: item.userId,
  displayName: item.displayName,
  specialty: item.specialty,
  location: item.location,
  credentials: item.credentials,
  setting: item.setting,
  honorarium: item.honorarium,
  requirements: item.requirements,
  isVerified: item.isVerified,
  isPremium: item.isPremium,
});

const Browse: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');
  const navigate = useNavigate();

  const [filters, setFilters] = useState<BrowseFilters>(defaultFilters);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');

  const [preceptors, setPreceptors] = useState<StudentPreceptor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setDebouncedSearchQuery(searchQuery.trim().toLowerCase());
    }, 300);

    return () => window.clearTimeout(timer);
  }, [searchQuery]);

  useEffect(() => {
    if (!isStudent) return;

    const loadPreceptors = async () => {
      try {
        setIsLoading(true);
        setError(null);

        const response = await preceptorService.searchPreceptors({
          specialty: filters.specialty || undefined,
          location: filters.location.trim() || undefined,
          availableDays: filters.availableDays,
          page: 0,
          size: FETCH_BATCH_SIZE,
        });

        setPreceptors(response.items.map(mapSearchItemToStudentPreceptor));
      } catch (err: any) {
        setError(err?.message || 'Unable to load preceptors. Please try again.');
        setPreceptors([]);
      } finally {
        setIsLoading(false);
      }
    };

    loadPreceptors();
  }, [filters.specialty, filters.location, filters.availableDays, isStudent]);

  useEffect(() => {
    setCurrentPage(1);
  }, [debouncedSearchQuery, filters.minHonorarium, filters.maxHonorarium, filters.specialty, filters.location, filters.availableDays]);

  const filteredPreceptors = useMemo(() => {
    const minValue = filters.minHonorarium ? Number(filters.minHonorarium) : null;
    const maxValue = filters.maxHonorarium ? Number(filters.maxHonorarium) : null;

    return preceptors.filter((preceptor) => {
      const searchable = `${preceptor.displayName} ${preceptor.specialty ?? ''} ${preceptor.location ?? ''}`.toLowerCase();
      const matchesSearch = !debouncedSearchQuery || searchable.includes(debouncedSearchQuery);

      const honorariumValue = parseHonorarium(preceptor.honorarium);
      const matchesMin = minValue == null || honorariumValue == null || honorariumValue >= minValue;
      const matchesMax = maxValue == null || honorariumValue == null || honorariumValue <= maxValue;

      return matchesSearch && matchesMin && matchesMax;
    });
  }, [debouncedSearchQuery, filters.maxHonorarium, filters.minHonorarium, preceptors]);

  const totalPages = Math.max(1, Math.ceil(filteredPreceptors.length / PAGE_SIZE));
  const paginatedPreceptors = useMemo(() => {
    const start = (currentPage - 1) * PAGE_SIZE;
    return filteredPreceptors.slice(start, start + PAGE_SIZE);
  }, [currentPage, filteredPreceptors]);

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  return (
    <StudentLayout
      pageTitle="Browse Preceptors"
      topSearch={{
        value: searchQuery,
        onChange: setSearchQuery,
        placeholder: 'Search by name, specialty, or location...',
      }}
    >
      <div className="mx-auto max-w-7xl">
        <section className="mb-5 md:hidden">
          <div className="relative">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">search</span>
            <input
              type="text"
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              placeholder="Search preceptors..."
              className="w-full rounded-full border border-slate-200 bg-white py-2 pl-10 pr-4 text-sm text-slate-700 outline-none transition-all focus:border-blue-300 focus:ring-2 focus:ring-blue-100"
            />
          </div>
        </section>

        {error ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[300px_minmax(0,1fr)]">
          <FilterSidebar
            filters={filters}
            onChange={setFilters}
            onReset={() => {
              setFilters(defaultFilters);
              setSearchQuery('');
            }}
          />

          <section className="space-y-5">
            <div className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="text-xl font-bold text-slate-900">Preceptor Listings</h2>
                  <p className="text-sm text-slate-500">
                    {isLoading
                      ? 'Loading preceptors...'
                      : `${filteredPreceptors.length} preceptor${filteredPreceptors.length === 1 ? '' : 's'} found`}
                  </p>
                </div>
              </div>
            </div>

            {isLoading ? (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
                {Array.from({ length: PAGE_SIZE }, (_, index) => (
                  <div key={index} className="h-[320px] animate-pulse rounded-2xl bg-slate-200/70" />
                ))}
              </div>
            ) : paginatedPreceptors.length > 0 ? (
              <>
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
                  {paginatedPreceptors.map((preceptor) => (
                    <PreceptorCard
                      key={preceptor.userId}
                      preceptor={preceptor}
                      variant="full"
                      onViewProfile={() => navigate(`/student/preceptor-detail/${preceptor.userId}`)}
                    />
                  ))}
                </div>

                <div className="flex flex-col items-center justify-between gap-3 rounded-xl bg-white px-4 py-3 shadow-sm ring-1 ring-slate-200 sm:flex-row">
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                    Page {currentPage} of {totalPages}
                  </p>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setCurrentPage((previous) => Math.max(1, previous - 1))}
                      disabled={currentPage === 1}
                      className="rounded-md border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      Previous
                    </button>

                    {Array.from({ length: totalPages }, (_, index) => index + 1)
                      .slice(Math.max(0, currentPage - 3), Math.max(5, currentPage + 2))
                      .map((pageNumber) => (
                        <button
                          key={pageNumber}
                          type="button"
                          onClick={() => setCurrentPage(pageNumber)}
                          className={`rounded-md px-3 py-1.5 text-xs font-bold ${
                            currentPage === pageNumber
                              ? 'bg-blue-600 text-white'
                              : 'border border-slate-200 text-slate-700 hover:bg-slate-100'
                          }`}
                        >
                          {pageNumber}
                        </button>
                      ))}

                    <button
                      type="button"
                      onClick={() => setCurrentPage((previous) => Math.min(totalPages, previous + 1))}
                      disabled={currentPage === totalPages}
                      className="rounded-md border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-16 text-center">
                <p className="text-base font-semibold text-slate-700">No preceptors found</p>
                <p className="mt-1 text-sm text-slate-500">Try adjusting your search keywords or filters.</p>
              </div>
            )}
          </section>
        </div>
      </div>
    </StudentLayout>
  );
};

export default Browse;
