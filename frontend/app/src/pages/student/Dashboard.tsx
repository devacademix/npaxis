import React, { useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import StudentLayout from '../../components/layout/StudentLayout';
import PreceptorCard from '../../components/student/PreceptorCard';
import StatsCard from '../../components/student/StatsCard';
import {
  studentService,
  type StudentPreceptor,
  type StudentProfile,
  type StudentUser,
} from '../../services/student';

interface ActivityItem {
  id: string;
  date: string;
  message: string;
}

const numberFormatter = new Intl.NumberFormat('en-IN');

const formatDate = (date: Date): string =>
  date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });

const Dashboard: React.FC = () => {
  const role = localStorage.getItem('role');
  const isStudent = role === 'STUDENT' || role === 'ROLE_STUDENT' || (role ?? '').includes('STUDENT');
  const navigate = useNavigate();

  const [user, setUser] = useState<StudentUser | null>(null);
  const [studentData, setStudentData] = useState<StudentProfile | null>(null);
  const [savedPreceptors, setSavedPreceptors] = useState<StudentPreceptor[]>([]);
  const [recommendedPreceptors, setRecommendedPreceptors] = useState<StudentPreceptor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [infoMessage, setInfoMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!isStudent) return;

    const loadStudentDashboard = async () => {
      try {
        setIsLoading(true);
        setError(null);
        setInfoMessage(null);

        const currentUser = await studentService.getLoggedInUser();
        setUser(currentUser);

        const nonCriticalErrors: string[] = [];
        const [profile, saved, recommended] = await Promise.all([
          studentService.getStudentProfile(currentUser.userId).catch((err: any) => {
            nonCriticalErrors.push(err?.message || 'Student profile unavailable.');
            return null;
          }),
          studentService.getSavedPreceptors(currentUser.userId).catch((err: any) => {
            nonCriticalErrors.push(err?.message || 'Saved preceptors unavailable.');
            return [] as StudentPreceptor[];
          }),
          studentService.searchPreceptors(10).catch((err: any) => {
            nonCriticalErrors.push(err?.message || 'Recommended preceptors unavailable.');
            return [] as StudentPreceptor[];
          }),
        ]);

        setStudentData(profile);
        setSavedPreceptors(saved);

        const savedIds = new Set(saved.map((item) => item.userId));
        const filteredRecommendations = recommended.filter((item) => !savedIds.has(item.userId));
        setRecommendedPreceptors(filteredRecommendations.length > 0 ? filteredRecommendations : recommended);

        if (nonCriticalErrors.length > 0) {
          setInfoMessage('Some dashboard widgets are temporarily unavailable. Core features are still usable.');
        }
      } catch (err: any) {
        setError(err?.message || 'Failed to load student dashboard.');
      } finally {
        setIsLoading(false);
      }
    };

    loadStudentDashboard();
  }, [isStudent]);

  const displayName = user?.displayName || localStorage.getItem('displayName') || 'Student';

  const savedCount = savedPreceptors.length;
  const inquiriesSent = Number(studentData?.inquiriesSent ?? 0);
  const recentlyViewed = Number(studentData?.recentlyViewed ?? 0);

  const recentActivity = useMemo<ActivityItem[]>(() => {
    const activities: ActivityItem[] = [];

    savedPreceptors.slice(0, 3).forEach((preceptor, index) => {
      const date = new Date();
      date.setDate(date.getDate() - index);
      activities.push({
        id: `saved-${preceptor.userId}-${index}`,
        date: formatDate(date),
        message: `Saved ${preceptor.displayName} to your shortlist.`,
      });
    });

    if (inquiriesSent > 0) {
      const inquiryDate = new Date();
      inquiryDate.setDate(inquiryDate.getDate() - 2);
      activities.push({
        id: 'inquiries-summary',
        date: formatDate(inquiryDate),
        message: `You have sent ${numberFormatter.format(inquiriesSent)} total inquiries so far.`,
      });
    }

    return activities.slice(0, 5);
  }, [savedPreceptors, inquiriesSent]);

  if (!isStudent) {
    return <Navigate to="/login" replace />;
  }

  const handleViewProfile = (preceptor: StudentPreceptor) => {
    navigate(`/student/preceptor-detail/${preceptor.userId}`);
  };

  return (
    <StudentLayout>
      <div id="student-dashboard-top" className="mx-auto max-w-7xl">
        <section className="mb-6 rounded-2xl bg-gradient-to-r from-blue-700 via-blue-600 to-indigo-600 p-6 text-white shadow-sm">
          <h2 className="text-3xl font-black tracking-tight">Hello, {displayName}</h2>
          <p className="mt-2 max-w-2xl text-sm text-blue-100">
            Discover verified preceptors, track your activity, and manage mentorship inquiries from one place.
          </p>
        </section>

        {error ? (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        ) : null}

        {infoMessage ? (
          <div className="mb-4 rounded-xl border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-medium text-blue-700">
            {infoMessage}
          </div>
        ) : null}

        <section id="saved-summary" className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {isLoading ? (
            Array.from({ length: 3 }, (_, index) => (
              <div key={index} className="h-36 animate-pulse rounded-2xl bg-slate-200/70" />
            ))
          ) : (
            <>
              <StatsCard
                title="Saved Preceptors"
                value={numberFormatter.format(savedCount)}
                subtitle="Profiles bookmarked for quick access"
                icon="favorite"
                tone="blue"
              />
              <StatsCard
                title="Total Inquiries Sent"
                value={numberFormatter.format(inquiriesSent)}
                subtitle="Outreach requests sent by you"
                icon="send"
                tone="emerald"
              />
              <StatsCard
                title="Recently Viewed"
                value={numberFormatter.format(recentlyViewed)}
                subtitle="Preceptor profiles viewed recently"
                icon="history"
                tone="amber"
              />
            </>
          )}
        </section>

        <section className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h3 className="text-xl font-bold text-slate-900">Quick Actions</h3>
          <p className="mt-1 text-sm text-slate-500">Jump to key sections and continue your discovery flow.</p>
          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => navigate('/student/browse')}
              className="inline-flex items-center gap-2 rounded-full bg-blue-700 px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-blue-800"
            >
              <span className="material-symbols-outlined text-base">travel_explore</span>
              Browse Preceptors
            </button>
            <button
              type="button"
              onClick={() => navigate('/student/saved')}
              className="inline-flex items-center gap-2 rounded-full border border-slate-300 bg-white px-5 py-2.5 text-sm font-bold text-slate-700 transition-colors hover:bg-slate-50"
            >
              <span className="material-symbols-outlined text-base">favorite</span>
              View Saved
            </button>
          </div>
        </section>

        <section id="browse-preceptors" className="mb-6 rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <div className="mb-4 flex items-center justify-between gap-3">
            <div>
              <h3 className="text-xl font-bold text-slate-900">Recommended Preceptors</h3>
              <p className="text-sm text-slate-500">Based on popular specialties and availability.</p>
            </div>
          </div>

          {isLoading ? (
            <div className="flex gap-4 overflow-x-auto pb-2">
              {Array.from({ length: 4 }, (_, index) => (
                <div key={index} className="h-72 min-w-[260px] animate-pulse rounded-2xl bg-slate-200/70" />
              ))}
            </div>
          ) : recommendedPreceptors.length > 0 ? (
            <div className="flex gap-4 overflow-x-auto pb-2">
              {recommendedPreceptors.map((preceptor) => (
                <PreceptorCard key={preceptor.userId} preceptor={preceptor} onViewProfile={handleViewProfile} />
              ))}
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-12 text-center text-sm font-medium text-slate-500">
              No recommendations available right now. Please check back shortly.
            </div>
          )}
        </section>

        <section className="grid grid-cols-1 gap-6 xl:grid-cols-3">
          <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200 xl:col-span-2">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-xl font-bold text-slate-900">Recent Activity</h3>
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500">Interactions</span>
            </div>

            {isLoading ? (
              <div className="space-y-3">
                {Array.from({ length: 4 }, (_, index) => (
                  <div key={index} className="h-14 animate-pulse rounded-xl bg-slate-200/70" />
                ))}
              </div>
            ) : recentActivity.length > 0 ? (
              <div className="overflow-hidden rounded-xl border border-slate-200">
                <table className="min-w-full divide-y divide-slate-200">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Date</th>
                      <th className="px-4 py-3 text-left text-xs font-bold uppercase tracking-wider text-slate-500">Activity</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 bg-white">
                    {recentActivity.map((item) => (
                      <tr key={item.id} className="transition-colors hover:bg-slate-50">
                        <td className="px-4 py-3 text-sm font-semibold text-slate-700">{item.date}</td>
                        <td className="px-4 py-3 text-sm text-slate-600">{item.message}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-12 text-center text-sm font-medium text-slate-500">
                No recent activity yet. Start browsing preceptors to begin.
              </div>
            )}
          </article>

          <article id="student-profile-card" className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
            <h3 className="text-xl font-bold text-slate-900">Profile Snapshot</h3>
            <p className="mt-1 text-sm text-slate-500">Your account details at a glance.</p>

            {isLoading ? (
              <div className="mt-4 space-y-3">
                {Array.from({ length: 5 }, (_, index) => (
                  <div key={index} className="h-8 animate-pulse rounded-lg bg-slate-200/70" />
                ))}
              </div>
            ) : (
              <dl className="mt-4 space-y-3 text-sm">
                <div>
                  <dt className="text-xs font-bold uppercase tracking-wider text-slate-500">Name</dt>
                  <dd className="mt-1 font-semibold text-slate-800">{displayName}</dd>
                </div>
                <div>
                  <dt className="text-xs font-bold uppercase tracking-wider text-slate-500">Email</dt>
                  <dd className="mt-1 break-all text-slate-700">{user?.email || studentData?.email || 'Not available'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-bold uppercase tracking-wider text-slate-500">University</dt>
                  <dd className="mt-1 text-slate-700">{studentData?.university || 'Not provided'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-bold uppercase tracking-wider text-slate-500">Program</dt>
                  <dd className="mt-1 text-slate-700">{studentData?.program || 'Not provided'}</dd>
                </div>
                <div>
                  <dt className="text-xs font-bold uppercase tracking-wider text-slate-500">Graduation Year</dt>
                  <dd className="mt-1 text-slate-700">{studentData?.graduationYear || 'Not provided'}</dd>
                </div>
              </dl>
            )}
          </article>
        </section>
      </div>
    </StudentLayout>
  );
};

export default Dashboard;
