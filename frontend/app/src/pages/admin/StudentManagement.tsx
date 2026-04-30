import React, { useEffect, useMemo, useState } from 'react';
import AdminLayout from '../../components/layout/AdminLayout';
import { studentService, type StudentProfile } from '../../services/student';
import { authService } from '../../services/auth';

interface EditableStudent {
  userId: number;
  displayName: string;
  email: string;
}

const StudentManagement: React.FC = () => {
  const [students, setStudents] = useState<StudentProfile[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const [editingStudent, setEditingStudent] = useState<EditableStudent | null>(null);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);
  const [isActionLoading, setIsActionLoading] = useState(false);
  const [showAddStudentModal, setShowAddStudentModal] = useState(false);
  const [newStudentForm, setNewStudentForm] = useState({
    displayName: '',
    email: '',
    password: '',
    university: '',
    program: '',
    graduationYear: '',
    phone: '',
  });

  useEffect(() => {
    loadStudents();
  }, []);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => setToast(null), 3000);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const loadStudents = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const directory = await studentService.getActiveStudents();
      setStudents(directory);
    } catch (err: any) {
      setError(err?.message || 'Failed to load students.');
    } finally {
      setIsLoading(false);
    }
  };

  const determineStatus = (student: StudentProfile) => {
    const deleted = Boolean((student as any).isDeleted ?? (student as any).deleted ?? false);
    if (deleted) return 'Deleted';
    return 'Active';
  };

  const handleEditClick = (student: StudentProfile) => {
    setEditingStudent({
      userId: student.userId,
      displayName: student.displayName,
      email: student.email,
    });
  };

  const handleEditSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!editingStudent) return;

    setActionLoadingId(editingStudent.userId);
    try {
      const updated = await studentService.updateStudentDetails(editingStudent.userId, {
        displayName: editingStudent.displayName,
        email: editingStudent.email,
      });
      setStudents((prev) =>
        prev.map((student) => (student.userId === editingStudent.userId ? { ...student, ...updated } : student))
      );
      setToast({ type: 'success', message: 'Student updated successfully.' });
      setEditingStudent(null);
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Failed to save student.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleSoftDelete = async (student: StudentProfile) => {
    setActionLoadingId(student.userId);
    try {
      await studentService.softDeleteStudent(student.userId);
      setStudents((prev) =>
        prev.map((item) => (item.userId === student.userId ? { ...item, isDeleted: true } : item))
      );
      setToast({ type: 'success', message: 'Student soft deleted.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Delete failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleHardDelete = async (student: StudentProfile) => {
    if (!window.confirm('This will permanently remove the student. Continue?')) return;
    setActionLoadingId(student.userId);
    try {
      await studentService.hardDeleteStudent(student.userId);
      setStudents((prev) => prev.filter((item) => item.userId !== student.userId));
      setToast({ type: 'success', message: 'Student permanently removed.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Hard delete failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleRestore = async (student: StudentProfile) => {
    setActionLoadingId(student.userId);
    try {
      await studentService.restoreStudent(student.userId);
      setStudents((prev) =>
        prev.map((item) => (item.userId === student.userId ? { ...item, isDeleted: false } : item))
      );
      setToast({ type: 'success', message: 'Student restored.' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.message || 'Restore failed.' });
    } finally {
      setActionLoadingId(null);
    }
  };

  const handleCreateStudent = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!newStudentForm.email || !newStudentForm.password || !newStudentForm.displayName) {
      setToast({ type: 'error', message: 'Name, email, and password are required.' });
      return;
    }
    setIsActionLoading(true);
    try {
      await authService.register({
        ...newStudentForm,
        roleId: 1,
      });
      setToast({ type: 'success', message: 'Student created successfully.' });
      setShowAddStudentModal(false);
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
      setToast({ type: 'error', message: err?.message || 'Failed to create student.' });
    } finally {
      setIsActionLoading(false);
    }
  };

  const nonDeletedStudents = useMemo(() => students.filter((student) => !Boolean((student as any).isDeleted ?? (student as any).deleted)), [students]);
  const deletedStudents = useMemo(() => students.filter((student) => Boolean((student as any).isDeleted ?? (student as any).deleted)), [students]);

  return (
    <AdminLayout>
        <div className="space-y-6">
          <header>
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Student Module</p>
            <h1 className="text-3xl font-bold text-slate-900">Student Management</h1>
            <p className="text-sm text-slate-500">
              Monitor student records, edit profile details, and manage deletion/restore workflows.
            </p>
          </header>
          <div className="flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => setShowAddStudentModal(true)}
              className="rounded-full bg-blue-600 px-5 py-2.5 text-sm font-bold text-white hover:bg-blue-700"
            >
              + Add Student
            </button>
          </div>

        <div>
          {toast && (
            <div
              className={`rounded-lg px-4 py-3 text-sm font-medium ${
                toast.type === 'success' ? 'bg-emerald-50 text-emerald-800' : 'bg-rose-50 text-rose-800'
              }`}
            >
              {toast.message}
            </div>
          )}
        </div>

        <section className="grid gap-6 lg:grid-cols-3">
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Total</p>
            <p className="text-4xl font-bold">{students.length}</p>
            <p className="text-xs text-slate-400">Students synced from backend</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Active</p>
            <p className="text-4xl font-bold">{nonDeletedStudents.length}</p>
            <p className="text-xs text-slate-400">Available student accounts</p>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.4em] text-slate-500">Deleted</p>
            <p className="text-4xl font-bold text-rose-500">{deletedStudents.length}</p>
            <p className="text-xs text-slate-400">Soft-deleted or disabled</p>
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-[2fr,1fr]">
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-900">Student records</h2>
              <button
                type="button"
                onClick={loadStudents}
                className="text-xs font-semibold text-blue-600 hover:text-blue-500"
              >
                Refresh
              </button>
            </div>
            {isLoading ? (
              <p className="mt-4 text-sm text-slate-500">Loading students...</p>
            ) : error ? (
              <p className="mt-4 text-sm text-rose-600">{error}</p>
            ) : (
              <div className="mt-4 space-y-3 text-sm text-slate-600">
                {students.length === 0 && (
                  <p className="py-6 text-center text-sm text-slate-500">No students available.</p>
                )}
                {students.map((student) => {
                  const status = determineStatus(student);
                  const isDeleted = status === 'Deleted';
                  const loading = actionLoadingId === student.userId;
                  return (
                    <div key={student.userId} className="rounded-2xl border border-slate-100 bg-slate-50/80 p-4">
                      <div className="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                        <div>
                          <p className="text-base font-semibold text-slate-900">{student.displayName}</p>
                          <p className="text-xs text-slate-500">{student.email}</p>
                        </div>
                        <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.3em]">
                          <span
                            className={`rounded-full px-2 py-1 ${
                              isDeleted ? 'bg-rose-100 text-rose-600' : 'bg-emerald-100 text-emerald-600'
                            }`}
                          >
                            {status}
                          </span>
                          <span className="text-xs text-slate-400">ID: {student.userId}</span>
                        </div>
                      </div>
                      <div className="mt-4 flex flex-wrap items-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleEditClick(student)}
                          disabled={loading}
                          className="rounded-full border border-slate-200 px-3 py-1 text-xs font-semibold text-slate-600 hover:border-slate-300"
                        >
                          Edit
                        </button>
                        {!isDeleted && (
                          <>
                            <button
                              type="button"
                              onClick={() => handleSoftDelete(student)}
                              disabled={loading}
                              className="rounded-full border border-slate-200 px-3 py-1 text-xs font-semibold text-amber-600 hover:border-amber-300"
                            >
                              Soft delete
                            </button>
                            <button
                              type="button"
                              onClick={() => handleHardDelete(student)}
                              disabled={loading}
                              className="rounded-full border border-rose-200 px-3 py-1 text-xs font-semibold text-rose-600 hover:border-rose-400"
                            >
                              Hard delete
                            </button>
                          </>
                        )}
                        {isDeleted && (
                          <button
                            type="button"
                            onClick={() => handleRestore(student)}
                            disabled={loading}
                            className="rounded-full border border-emerald-200 px-3 py-1 text-xs font-semibold text-emerald-600 hover:border-emerald-400"
                          >
                            Restore
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-slate-900">Edit student</h2>
            {editingStudent ? (
              <form onSubmit={handleEditSubmit} className="mt-4 space-y-4 text-sm text-slate-600">
                <div>
                  <label className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">Name</label>
                  <input
                    type="text"
                    value={editingStudent.displayName}
                    onChange={(event) =>
                      setEditingStudent({ ...editingStudent, displayName: event.target.value })
                    }
                    className="mt-1 w-full rounded-xl border border-slate-200 p-3 text-sm text-slate-700 focus:border-blue-500 focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">Email</label>
                  <input
                    type="email"
                    value={editingStudent.email}
                    onChange={(event) => setEditingStudent({ ...editingStudent, email: event.target.value })}
                    className="mt-1 w-full rounded-xl border border-slate-200 p-3 text-sm text-slate-700 focus:border-blue-500 focus:outline-none"
                    required
                  />
                </div>
                <div className="flex items-center gap-3">
                  <button
                    type="submit"
                    disabled={actionLoadingId === editingStudent.userId}
                    className="rounded-full bg-blue-600 px-4 py-2 text-xs font-semibold uppercase tracking-[0.4em] text-white transition hover:bg-blue-700 disabled:opacity-60"
                  >
                    {actionLoadingId === editingStudent.userId ? 'Saving...' : 'Save changes'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingStudent(null)}
                    className="text-xs font-semibold uppercase tracking-[0.4em] text-slate-400 hover:text-slate-600"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <p className="mt-4 text-sm text-slate-500">Select a student from the list to edit details.</p>
            )}
          </div>
        </section>
        {showAddStudentModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
            <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-bold text-slate-900">Create Student</h2>
                <button
                  type="button"
                  onClick={() => setShowAddStudentModal(false)}
                  className="text-slate-400 hover:text-slate-600"
                >
                  <span className="material-symbols-outlined text-lg">close</span>
                </button>
              </div>
              <form className="mt-4 space-y-4" onSubmit={handleCreateStudent}>
                <div className="grid gap-4 md:grid-cols-2">
                  <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                    Full Name
                    <input
                      value={newStudentForm.displayName}
                      onChange={(event) =>
                        setNewStudentForm((prev) => ({ ...prev, displayName: event.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                      required
                    />
                  </label>
                  <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                    Email
                    <input
                      type="email"
                      value={newStudentForm.email}
                      onChange={(event) =>
                        setNewStudentForm((prev) => ({ ...prev, email: event.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                      required
                    />
                  </label>
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                    Password
                    <input
                      type="password"
                      value={newStudentForm.password}
                      onChange={(event) =>
                        setNewStudentForm((prev) => ({ ...prev, password: event.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                      required
                    />
                  </label>
                  <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                    Program
                    <input
                      value={newStudentForm.program}
                      onChange={(event) =>
                        setNewStudentForm((prev) => ({ ...prev, program: event.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                    />
                  </label>
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                    University
                    <input
                      value={newStudentForm.university}
                      onChange={(event) =>
                        setNewStudentForm((prev) => ({ ...prev, university: event.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                    />
                  </label>
                  <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                    Graduation Year
                    <input
                      value={newStudentForm.graduationYear}
                      onChange={(event) =>
                        setNewStudentForm((prev) => ({ ...prev, graduationYear: event.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                    />
                  </label>
                </div>
                <label className="space-y-1 text-xs font-semibold uppercase text-slate-500">
                  Phone
                  <input
                    value={newStudentForm.phone}
                    onChange={(event) =>
                      setNewStudentForm((prev) => ({ ...prev, phone: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
                  />
                </label>
                <div className="flex justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setShowAddStudentModal(false)}
                    className="rounded-full border border-slate-200 px-5 py-2 text-sm font-semibold text-slate-600 hover:border-slate-300"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isActionLoading}
                    className="rounded-full bg-blue-600 px-5 py-2 text-sm font-bold text-white hover:bg-blue-700 disabled:opacity-60"
                  >
                    {isActionLoading ? 'Creating...' : 'Create Student'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </AdminLayout>
  );
};

export default StudentManagement;
