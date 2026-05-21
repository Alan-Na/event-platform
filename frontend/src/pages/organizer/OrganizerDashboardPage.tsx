import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getOrganizerDashboardApi, type OrganizerDashboardDto } from '@/api/organizer.api';
import { Spinner } from '@/components/common/Spinner';
import { ErrorState } from '@/components/common/ErrorState';

export function OrganizerDashboardPage() {
  const { t } = useTranslation('organizer');
  const [dashboard, setDashboard] = useState<OrganizerDashboardDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    getOrganizerDashboardApi()
      .then(setDashboard)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;
  if (error || !dashboard) return <ErrorState />;

  const stats = [
    { label: t('dashboard.totalEvents'), value: dashboard.totalEvents },
    { label: t('dashboard.publishedEvents'), value: dashboard.publishedEvents },
    { label: t('dashboard.totalConfirmed'), value: dashboard.totalConfirmedBookings },
    { label: t('dashboard.totalWaitlist'), value: dashboard.totalWaitlistCount },
    { label: t('dashboard.totalCheckedIn'), value: dashboard.totalCheckedIn },
  ];

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold text-slate-900">{t('dashboard.title')}</h1>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {stats.map((s) => (
          <div key={s.label} className="card p-5 text-center">
            <div className="text-3xl font-bold text-slate-900">{s.value}</div>
            <div className="mt-1 text-sm text-slate-500">{s.label}</div>
          </div>
        ))}
      </div>
      {dashboard.upcomingEvents.length > 0 && (
        <div>
          <h2 className="mb-4 text-lg font-semibold text-slate-800">{t('dashboard.upcomingEvents')}</h2>
          <div className="space-y-3">
            {dashboard.upcomingEvents.map((e) => (
              <div key={e.eventId} className="card flex items-center justify-between p-4">
                <div>
                  <div className="font-medium text-slate-900">{e.eventTitle}</div>
                  <div className="text-sm text-slate-500">{e.confirmedCount} / {e.capacity} confirmed</div>
                </div>
                <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-700">{e.status}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
