import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import adminService, {
  type AdminPreceptorBillingInfo,
  type AdminPreceptorContactInfo,
  type AdminPreceptorDetail,
  type AdminPreceptorListItem,
  type VerificationHistoryItem,
} from '../../services/admin';

type PreceptorTab = 'approved' | 'rejected' | 'pending';
type DetailTab = 'overview' | 'billing' | 'contact';

const PAGE_SIZE = 10;

const tabs: Array<{ key: PreceptorTab; label: string }> = [
  { key: 'approved', label: 'Approved' },
  { key: 'rejected', label: 'Rejected' },
  { key: 'pending', label: 'Pending' },
];

interface EditablePreceptorForm {
  displayName: string;
  phone: string;
  specialty: string;
  location: string;
  honorarium: string;
  requirements: string;
}

const PreceptorManagement: React.FC = () => {
  const [activeTab, setActiveTab] = useState<PreceptorTab>('approved');
  const [preceptors, setPreceptors] = useState<AdminPreceptorListItem[]>([]);
  const [selected, setSelected] = useState<AdminPreceptorDetail | null>(null);
  const [editForm, setEditForm] = useState<EditablePreceptorForm | null>(null);
  const [activeDetailTab, setActiveDetailTab] = useState<DetailTab>('overview');
  const [billingInfo, setBillingInfo] = useState<AdminPreceptorBillingInfo | null>(null);
  const [contactInfo, setContactInfo] = useState<AdminPreceptorContactInfo | null>(null);
  const [history, setHistory] = useState<VerificationHistoryItem[]>([]);
  const [filters, setFilters] = useState({ specialty: '', location: '' });
  const [appliedFilters, setAppliedFilters] = useState({ specialty: '', location: '' });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [note, setNote] = useState('');
  const [rejectReason, setRejectReason] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [billingLoading, setBillingLoading] = useState(false);
  const [contactLoading, setContactLoading] = useState(false);
  const [billingError, setBillingError] = useState<string | null>(null);
  const [contactError, setContactError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const hasSearchFilters = Boolean(appliedFilters.specialty || appliedFilters.location);

  const loadPreceptors = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = {
        page,
        size: PAGE_SIZE,
        specialty: appliedFilters.specialty || undefined,
        location: appliedFilters.location || undefined,
      };

      const response =
        activeTab === 'approved'
          ? hasSearchFilters
            ? await adminService.getAdminPreceptors({
                ...params,
                verificationStatus: 'APPROVED',
              })
            : await adminService.getApprovedPreceptors({ page, size: PAGE_SIZE })
          : activeTab === 'rejected'
            ? hasSearchFilters
              ? await adminService.getAdminPreceptors({
                  ...params,
                  verificationStatus: 'REJECTED',
                })
              : await adminService.getRejectedPreceptors({ page, size: PAGE_SIZE })
            : hasSearchFilters
              ? await adminService.getAdminPreceptors({
                  ...params,
                  verificationStatus: 'PENDING',
                })
              : await adminService.getPendingPreceptors({ page, size: PAGE_SIZE });

      const normalizedItems: AdminPreceptorListItem[] = response.items.map((item: any) => ({
        userId: Number(item?.userId ?? item?.id ?? 0),
        displayName: String(item?.displayName ?? item?.name ?? 'Unknown Preceptor'),
        email: String(item?.email ?? 'N/A'),
        specialty: item?.specialty ?? item?.credentials ?? undefined,
        location: item?.location ?? undefined,
        verificationStatus: item?.verificationStatus ?? item?.status ?? activeTab.toUpperCase(),
        isPremium: Boolean(item?.isPremium),
      }));

      setPreceptors(normalizedItems);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err: any) {
      setError(err?.message || 'Unable to load preceptors.');
      setPreceptors([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setIsLoading(false);
    }
  };

  const loadDetail = async (userId: number) => {
    try {
      setDetailLoading(true);
      const [detail, verificationHistory] = await Promise.all([
        adminService.getAdminPreceptorDetail(userId),
        adminService.getPreceptorVerificationHistory(userId).catch(() => []),
      ]);
      setSelected(detail);
      setEditForm({
        displayName: detail.displayName || '',
        phone: detail.phone || '',
        specialty: detail.specialty || '',
        location: detail.location || '',
        honorarium: detail.honorarium || '',
        requirements: detail.requirements || '',
      });
      setActiveDetailTab('overview');
      setBillingInfo(null);
      setContactInfo(null);
      setBillingError(null);
      setContactError(null);
      setHistory(verificationHistory);
    } catch (err: any) {
      setError(err?.message || 'Unable to load preceptor detail.');
    } finally {
      setDetailLoading(false);
    }
  };

  useEffect(() => {
    loadPreceptors();
  }, [activeTab, appliedFilters.location, appliedFilters.specialty, page]);

  useEffect(() => {
    setPage(0);
    setSelected(null);
    setEditForm(null);
    setBillingInfo(null);
    setContactInfo(null);
    setHistory([]);
  }, [activeTab]);

  const handleAddNote = async () => {
    if (!selected || !note.trim()) return;
    try {
      await adminService.addPreceptorVerificationNote(selected.userId, note.trim());
      setSuccess('Verification note added.');
      setNote('');
      await loadDetail(selected.userId);
    } catch (err: any) {
      setError(err?.message || 'Failed to add note.');
    }
  };

  const handleReject = async () => {
    if (!selected || !rejectReason.trim()) return;
    try {
      await adminService.rejectPreceptorWithReason(selected.userId, rejectReason.trim());
      setSuccess('Preceptor rejected successfully.');
      setRejectReason('');
      await loadPreceptors();
      await loadDetail(selected.userId);
    } catch (err: any) {
      setError(err?.message || 'Failed to reject preceptor.');
    }
  };

  const handleEditFieldChange = (field: keyof EditablePreceptorForm, value: string) => {
    setEditForm((prev) => (prev ? { ...prev, [field]: value } : prev));
  };

  const handleSaveEdit = async () => {
    if (!selected || !editForm) return;

    try {
      setIsSavingEdit(true);
      setError(null);
      setSuccess(null);

      const payload = {
        ...selected,
        displayName: editForm.displayName.trim(),
        phone: editForm.phone.trim(),
        specialty: editForm.specialty
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
        location: editForm.location.trim(),
        honorarium: editForm.honorarium.trim(),
        requirements: editForm.requirements.trim(),
      };

      const updated = await adminService.updateAdminPreceptor(selected.userId, payload);
      setSelected(updated);
      setEditForm({
        displayName: updated.displayName || '',
        phone: updated.phone || '',
        specialty: updated.specialty || '',
        location: updated.location || '',
        honorarium: updated.honorarium || '',
        requirements: updated.requirements || '',
      });
      setSuccess('Preceptor updated successfully.');
      await loadPreceptors();
    } catch (err: any) {
      setError(err?.message || 'Failed to update preceptor.');
    } finally {
      setIsSavingEdit(false);
    }
  };

  const handleDetailTabChange = async (tab: DetailTab) => {
    setActiveDetailTab(tab);
    if (!selected) return;

    if (tab === 'billing' && !billingInfo && !billingLoading) {
      try {
        setBillingLoading(true);
        setBillingError(null);
        const response = await adminService.getAdminPreceptorBilling(selected.userId);
        setBillingInfo(response);
      } catch (err: any) {
        setBillingError(err?.message || 'Unable to load billing info.');
      } finally {
        setBillingLoading(false);
      }
    }

    if (tab === 'contact' && !contactInfo && !contactLoading) {
      try {
        setContactLoading(true);
        setContactError(null);
        const response = await adminService.getAdminPreceptorContact(selected.userId);
        setContactInfo(response);
      } catch (err: any) {
        setContactError(err?.message || 'Unable to load contact info.');
      } finally {
        setContactLoading(false);
      }
    }
  };

  const formatDateTime = (value?: string) => {
    if (!value) return 'N/A';
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return 'N/A';
    return parsed.toLocaleString();
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Preceptor Management</p>
          <h1 className="text-3xl font-bold text-slate-900">Admin Preceptor Operations</h1>
          <p className="text-sm text-slate-500">Review approved, rejected, and pending preceptors with backend-powered status tabs and detail tools.</p>
        </header>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}

        <section className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={`rounded-full px-5 py-2.5 text-sm font-bold transition ${
                activeTab === tab.key
                  ? 'bg-blue-700 text-white shadow-sm'
                  : 'bg-white text-slate-600 ring-1 ring-slate-200 hover:bg-slate-50'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </section>

        <section className="grid gap-4 md:grid-cols-3">
          <input
            value={filters.specialty}
            onChange={(e) => setFilters((prev) => ({ ...prev, specialty: e.target.value }))}
            placeholder="Filter by specialty"
            className="rounded-xl border border-slate-200 px-4 py-3 text-sm"
          />
          <input
            value={filters.location}
            onChange={(e) => setFilters((prev) => ({ ...prev, location: e.target.value }))}
            placeholder="Filter by location"
            className="rounded-xl border border-slate-200 px-4 py-3 text-sm"
          />
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => {
                setAppliedFilters(filters);
                setPage(0);
              }}
              className="flex-1 rounded-full bg-blue-700 px-5 py-3 text-sm font-bold text-white hover:bg-blue-800"
            >
              Apply Filters
            </button>
            <button
              type="button"
              onClick={() => {
                setFilters({ specialty: '', location: '' });
                setAppliedFilters({ specialty: '', location: '' });
                setPage(0);
              }}
              className="rounded-full border border-slate-200 px-5 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              Reset
            </button>
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-[1.2fr,1fr]">
          <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Preceptor Directory</h2>
                <p className="text-xs text-slate-500">{tabs.find((tab) => tab.key === activeTab)?.label} tab powered by backend status endpoints.</p>
              </div>
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">{totalElements} result(s)</span>
            </div>
            {isLoading ? (
              <div className="mt-4 space-y-3">
                {Array.from({ length: 4 }, (_, index) => <div key={index} className="h-16 animate-pulse rounded-xl bg-slate-200/70" />)}
              </div>
            ) : (
              <>
                <div className="mt-4 space-y-3">
                  {preceptors.map((preceptor) => (
                    <button
                      key={preceptor.userId}
                      type="button"
                      onClick={() => loadDetail(preceptor.userId)}
                      className={`w-full rounded-2xl border px-4 py-4 text-left transition ${selected?.userId === preceptor.userId ? 'border-blue-300 bg-blue-50' : 'border-slate-100 bg-slate-50/80 hover:border-slate-300'}`}
                    >
                      <div className="flex items-center justify-between gap-4">
                        <div>
                          <p className="text-base font-semibold text-slate-900">{preceptor.displayName}</p>
                          <p className="text-xs text-slate-500">{preceptor.email || 'N/A'}</p>
                          <p className="mt-1 text-xs text-slate-500">{preceptor.specialty || 'Specialty unavailable'} • {preceptor.location || 'Location unavailable'}</p>
                        </div>
                        <div className="text-right">
                          <p className="text-xs font-bold uppercase tracking-wider text-slate-500">{preceptor.verificationStatus || 'UNKNOWN'}</p>
                          <p className="mt-1 text-xs text-slate-400">{preceptor.isPremium ? 'Premium' : 'Standard'}</p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
                <div className="mt-5 flex items-center justify-between gap-3">
                  <p className="text-sm text-slate-500">
                    Page {page + 1} of {Math.max(totalPages, 1)}
                  </p>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      disabled={page <= 0}
                      onClick={() => setPage((current) => Math.max(current - 1, 0))}
                      className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-slate-50"
                    >
                      Previous
                    </button>
                    <button
                      type="button"
                      disabled={page + 1 >= Math.max(totalPages, 1)}
                      onClick={() => setPage((current) => current + 1)}
                      className="rounded-full border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-slate-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>

          <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
            <h2 className="text-lg font-semibold text-slate-900">Preceptor Detail</h2>
            {detailLoading ? (
              <div className="mt-4 h-52 animate-pulse rounded-xl bg-slate-200/70" />
            ) : selected && editForm ? (
              <div className="mt-4 space-y-4">
                <div className="flex flex-wrap gap-2">
                  {([
                    { key: 'overview', label: 'Overview' },
                    { key: 'billing', label: 'Billing Info' },
                    { key: 'contact', label: 'Contact Info' },
                  ] as Array<{ key: DetailTab; label: string }>).map((tab) => (
                    <button
                      key={tab.key}
                      type="button"
                      onClick={() => handleDetailTabChange(tab.key)}
                      className={`rounded-full px-4 py-2 text-xs font-bold transition ${
                        activeDetailTab === tab.key
                          ? 'bg-blue-700 text-white'
                          : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                      }`}
                    >
                      {tab.label}
                    </button>
                  ))}
                </div>

                {activeDetailTab === 'overview' ? (
                  <>
                    <div className="space-y-1">
                      <p className="text-xl font-bold text-slate-900">{selected.displayName}</p>
                      <p className="text-sm text-slate-500">{selected.email}</p>
                      <p className="text-sm text-slate-500">{selected.specialty || 'Specialty unavailable'} • {selected.location || 'Location unavailable'}</p>
                    </div>

                    <div className="grid grid-cols-2 gap-3 text-sm">
                      <div className="rounded-xl bg-slate-50 p-3"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Verification</p><p className="mt-1 font-semibold text-slate-800">{selected.verificationStatus || 'N/A'}</p></div>
                      <div className="rounded-xl bg-slate-50 p-3"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Premium</p><p className="mt-1 font-semibold text-slate-800">{selected.isPremium ? 'Yes' : 'No'}</p></div>
                    </div>

                    <div className="space-y-4 rounded-2xl border border-slate-200 bg-slate-50/70 p-4">
                      <div className="flex items-center justify-between gap-3">
                        <h3 className="text-sm font-bold text-slate-900">Edit Preceptor</h3>
                        <button
                          type="button"
                          onClick={handleSaveEdit}
                          disabled={isSavingEdit}
                          className="rounded-full bg-blue-700 px-4 py-2 text-xs font-bold text-white hover:bg-blue-800 disabled:opacity-60"
                        >
                          {isSavingEdit ? 'Saving...' : 'Save Changes'}
                        </button>
                      </div>
                      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div>
                          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Display Name</label>
                          <input
                            value={editForm.displayName}
                            onChange={(e) => handleEditFieldChange('displayName', e.target.value)}
                            className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Phone</label>
                          <input
                            value={editForm.phone}
                            onChange={(e) => handleEditFieldChange('phone', e.target.value)}
                            className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Specialties</label>
                          <input
                            value={editForm.specialty}
                            onChange={(e) => handleEditFieldChange('specialty', e.target.value)}
                            placeholder="Family Medicine, Pediatrics"
                            className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Location</label>
                          <input
                            value={editForm.location}
                            onChange={(e) => handleEditFieldChange('location', e.target.value)}
                            className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Honorarium</label>
                          <input
                            value={editForm.honorarium}
                            onChange={(e) => handleEditFieldChange('honorarium', e.target.value)}
                            className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-bold uppercase tracking-wider text-slate-500">Requirements</label>
                          <input
                            value={editForm.requirements}
                            onChange={(e) => handleEditFieldChange('requirements', e.target.value)}
                            className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                          />
                        </div>
                      </div>
                    </div>

                    <div className="flex flex-wrap gap-2">
                      <a href={adminService.getAdminLicenseReviewUrl(selected.userId)} target="_blank" rel="noreferrer" className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50">Review License</a>
                      <a href={adminService.getAdminLicenseDownloadUrl(selected.userId)} target="_blank" rel="noreferrer" className="rounded-full border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50">Download License</a>
                    </div>

                    <div>
                      <h3 className="text-sm font-bold text-slate-900">Add Verification Note</h3>
                      <textarea value={note} onChange={(e) => setNote(e.target.value)} rows={3} className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Add review note..." />
                      <button type="button" onClick={handleAddNote} className="mt-2 rounded-full bg-blue-700 px-4 py-2 text-xs font-bold text-white hover:bg-blue-800">Save Note</button>
                    </div>

                    <div>
                      <h3 className="text-sm font-bold text-slate-900">Reject Preceptor</h3>
                      <textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} rows={3} className="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="Provide rejection reason..." />
                      <button type="button" onClick={handleReject} className="mt-2 rounded-full border border-red-200 px-4 py-2 text-xs font-bold text-red-600 hover:bg-red-50">Reject with Reason</button>
                    </div>

                    <div>
                      <h3 className="text-sm font-bold text-slate-900">Verification History</h3>
                      <div className="mt-2 space-y-2">
                        {history.length === 0 ? (
                          <p className="text-sm text-slate-500">No verification history found.</p>
                        ) : (
                          history.map((item) => (
                            <div key={item.auditId} className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                              <p className="font-semibold text-slate-800">{item.previousStatus || 'N/A'} → {item.newStatus || 'N/A'}</p>
                              <p>{item.reviewNote || 'No note provided'}</p>
                              <p className="mt-1 text-xs text-slate-400">{item.changeTimestamp ? new Date(item.changeTimestamp).toLocaleString() : 'N/A'}</p>
                            </div>
                          ))
                        )}
                      </div>
                    </div>
                  </>
                ) : null}

                {activeDetailTab === 'billing' ? (
                  <div className="space-y-4">
                    {billingLoading ? (
                      <div className="h-40 animate-pulse rounded-xl bg-slate-100" />
                    ) : billingError ? (
                      <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{billingError}</div>
                    ) : billingInfo ? (
                      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Subscription</p><p className="mt-1 font-semibold text-slate-900">{billingInfo.subscriptionPlan || 'N/A'}</p></div>
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Status</p><p className="mt-1 font-semibold text-slate-900">{billingInfo.subscriptionStatus || 'N/A'}</p></div>
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Total Revenue</p><p className="mt-1 font-semibold text-slate-900">${Number(billingInfo.totalRevenue ?? 0).toLocaleString()}</p></div>
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Monthly Revenue</p><p className="mt-1 font-semibold text-slate-900">${Number(billingInfo.monthlyRevenue ?? 0).toLocaleString()}</p></div>
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Last Payment Status</p><p className="mt-1 font-semibold text-slate-900">{billingInfo.lastPaymentStatus || 'N/A'}</p></div>
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Last Transaction Date</p><p className="mt-1 font-semibold text-slate-900">{formatDateTime(billingInfo.lastPaymentDate)}</p></div>
                      </div>
                    ) : (
                      <p className="text-sm text-slate-500">No billing info available.</p>
                    )}
                  </div>
                ) : null}

                {activeDetailTab === 'contact' ? (
                  <div className="space-y-4">
                    {contactLoading ? (
                      <div className="h-32 animate-pulse rounded-xl bg-slate-100" />
                    ) : contactError ? (
                      <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{contactError}</div>
                    ) : contactInfo ? (
                      <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Email</p><p className="mt-1 font-semibold text-slate-900">{contactInfo.email || selected.email || 'N/A'}</p></div>
                        <div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-bold uppercase tracking-wider text-slate-500">Phone</p><p className="mt-1 font-semibold text-slate-900">{contactInfo.phone || selected.phone || 'N/A'}</p></div>
                      </div>
                    ) : (
                      <p className="text-sm text-slate-500">No contact info available.</p>
                    )}
                  </div>
                ) : null}
              </div>
            ) : (
              <p className="mt-4 text-sm text-slate-500">Select a preceptor to inspect detail, notes, and verification history.</p>
            )}
          </div>
        </section>
      </div>
    </AdminLayout>
  );
};

export default PreceptorManagement;
