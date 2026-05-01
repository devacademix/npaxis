import React, { useEffect, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { authService } from '../../services/auth';
import { studentService, type StudentInquirySummary, type StudentProfile } from '../../services/student';

const StudentManagement: React.FC = () => {
  const [students, setStudents] = useState<StudentProfile[]>([]);
  const [selected, setSelected] = useState<StudentProfile | null>(null);
  const [inquiries, setInquiries] = useState<StudentInquirySummary[]>([]);
  const [filters, setFilters] = useState({ university: '', program: '' });
  const [newStudentForm, setNewStudentForm] = useState({
    displayName: '',
    email: '',
    password: '',
    university: '',
    program: '',
    graduationYear: '',
    phone: '',
  });
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const loadStudents = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const directory = await studentService.searchAdminStudents({
        university: filters.university || undefined,
        program: filters.program || undefined,
      });
      setStudents(directory);
    } catch (err: any) {
      setError(err?.message || 'Failed to load students.');
    } finally {
      setIsLoading(false);
    }
  };

  const loadStudentDetail = async (userId: number) => {
    try {
      const [detail, inquiryList] = await Promise.all([
        studentService.getAdminStudentDetail(userId),
        studentService.getAdminStudentInquiries(userId).catch(() => []),
      ]);
      setSelected(detail);
      setInquiries(inquiryList);
    } catch (err: any) {
      setError(err?.message || 'Unable to load student detail.');
    }
  };

  useEffect(() => {
    loadStudents();
  }, []);

  const handleSaveStudent = async () => {
    if (!selected) return;
    try {
      setIsSaving(true);
      await studentService.updateAdminStudentDetail(selected.userId, selected);
      setSuccess('Student updated successfully.');
      await loadStudents();
    } catch (err: any) {
      setError(err?.message || 'Failed to save student.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteStudent = async () => {
    if (!selected) return;
    try {
      setIsSaving(true);
      await studentService.deleteAdminStudent(selected.userId);
      setSuccess('Student deleted successfully.');
      setSelected(null);
      setInquiries([]);
      await loadStudents();
    } catch (err: any) {
      setError(err?.message || 'Failed to delete student.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCreateStudent = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      setIsSaving(true);
      await authService.register({
        ...newStudentForm,
        roleId: 1,
      });
      setSuccess('Student created successfully.');
      setNewStudentForm({
        displayName: '',
        email: '',
        password: '',
        university: '',
        program: '',
        graduationYear: '',
        phone: '',
      });
      await loadStudents();
    } catch (err: any) {
      setError(err?.message || 'Failed to create student.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <AdminLayout>
      <div className="space-y-6">
        <header>
          <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Student Management</p>
          <h1 className="text-3xl font-bold text-slate-900">Admin Student Operations</h1>
          <p className="text-sm text-slate-500">Use the documented admin student endpoints for search, detail, update, delete, and inquiry review.</p>
        </header>

        {error ? <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div> : null}
        {success ? <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{success}</div> : null}

        <section className="grid gap-4 lg:grid-cols-[1fr,1.4fr]">
          <div className="space-y-4">
            <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <h2 className="text-lg font-semibold text-slate-900">Create Student</h2>
              <form className="mt-4 space-y-3" onSubmit={handleCreateStudent}>
                {[
                  ['displayName', 'Full Name'],
                  ['email', 'Email'],
                  ['password', 'Password'],
                  ['university', 'University'],
                  ['program', 'Program'],
                  ['graduationYear', 'Graduation Year'],
                  ['phone', 'Phone'],
                ].map(([key, label]) => (
                  <input
                    key={key}
                    type={key === 'password' ? 'password' : 'text'}
                    placeholder={label}
                    value={(newStudentForm as any)[key]}
                    onChange={(event) => setNewStudentForm((prev) => ({ ...prev, [key]: event.target.value }))}
                    className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm"
                  />
                ))}
                <button type="submit" disabled={isSaving} className="w-full rounded-full bg-blue-700 px-4 py-3 text-sm font-bold text-white hover:bg-blue-800 disabled:opacity-60">
                  {isSaving ? 'Saving...' : 'Create Student'}
                </button>
              </form>
            </div>

            <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <h2 className="text-lg font-semibold text-slate-900">Search Students</h2>
              <div className="mt-4 space-y-3">
                <input value={filters.university} onChange={(e) => setFilters((prev) => ({ ...prev, university: e.target.value }))} placeholder="University" className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm" />
                <input value={filters.program} onChange={(e) => setFilters((prev) => ({ ...prev, program: e.target.value }))} placeholder="Program" className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm" />
                <button type="button" onClick={loadStudents} className="w-full rounded-full border border-slate-200 px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50">
                  Apply Filters
                </button>
              </div>
            </div>
          </div>

          <div className="grid gap-6 xl:grid-cols-[1.1fr,1fr]">
            <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-slate-900">Student Directory</h2>
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-500">{students.length} result(s)</span>
              </div>
              {isLoading ? (
                <div className="mt-4 space-y-3">
                  {Array.from({ length: 5 }, (_, index) => <div key={index} className="h-16 animate-pulse rounded-xl bg-slate-200/70" />)}
                </div>
              ) : (
                <div className="mt-4 space-y-3">
                  {students.map((student) => (
                    <button
                      key={student.userId}
                      type="button"
                      onClick={() => loadStudentDetail(student.userId)}
                      className={`w-full rounded-2xl border px-4 py-4 text-left transition ${selected?.userId === student.userId ? 'border-blue-300 bg-blue-50' : 'border-slate-100 bg-slate-50/80 hover:border-slate-300'}`}
                    >
                      <p className="text-base font-semibold text-slate-900">{student.displayName}</p>
                      <p className="text-xs text-slate-500">{student.email}</p>
                      <p className="mt-1 text-xs text-slate-500">{student.university || 'University unavailable'} • {student.program || 'Program unavailable'}</p>
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
              <h2 className="text-lg font-semibold text-slate-900">Student Detail</h2>
              {selected ? (
                <div className="mt-4 space-y-4">
                  {[
                    ['displayName', 'Full Name'],
                    ['email', 'Email'],
                    ['university', 'University'],
                    ['program', 'Program'],
                    ['graduationYear', 'Graduation Year'],
                    ['phone', 'Phone'],
                  ].map(([key, label]) => (
                    <label key={key} className="block">
                      <span className="mb-1 block text-xs font-semibold uppercase tracking-wider text-slate-500">{label}</span>
                      <input
                        value={(selected as any)[key] || ''}
                        onChange={(event) => setSelected((prev) => (prev ? { ...prev, [key]: event.target.value } : prev))}
                        className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"
                      />
                    </label>
                  ))}

                  <div className="flex flex-wrap gap-2">
                    <button type="button" onClick={handleSaveStudent} disabled={isSaving} className="rounded-full bg-blue-700 px-4 py-2 text-xs font-bold text-white hover:bg-blue-800 disabled:opacity-60">
                      Save
                    </button>
                    <button type="button" onClick={handleDeleteStudent} disabled={isSaving} className="rounded-full border border-red-200 px-4 py-2 text-xs font-bold text-red-600 hover:bg-red-50 disabled:opacity-60">
                      Delete
                    </button>
                  </div>

                  <div>
                    <h3 className="text-sm font-bold text-slate-900">Student Inquiries</h3>
                    <div className="mt-2 space-y-2">
                      {inquiries.length === 0 ? (
                        <p className="text-sm text-slate-500">No inquiries found for this student.</p>
                      ) : (
                        inquiries.map((inquiry, index) => (
                          <div key={`${inquiry.inquiryId ?? index}`} className="rounded-xl bg-slate-50 p-3 text-sm text-slate-600">
                            <p className="font-semibold text-slate-800">{inquiry.subject || 'Untitled Inquiry'}</p>
                            <p>{inquiry.message || 'No message available.'}</p>
                            <p className="mt-1 text-xs text-slate-400">{inquiry.status || 'N/A'} • {inquiry.createdAt ? new Date(inquiry.createdAt).toLocaleString() : 'N/A'}</p>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                </div>
              ) : (
                <p className="mt-4 text-sm text-slate-500">Select a student to inspect detail and inquiry history.</p>
              )}
            </div>
          </div>
        </section>
      </div>
    </AdminLayout>
  );
};

export default StudentManagement;
