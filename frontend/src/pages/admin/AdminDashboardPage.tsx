import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getDashboardStatsApi, type DashboardStats } from '@/api/admin.api';
import { StatCard } from '@/components/admin/StatCard';

export function AdminDashboardPage() {
  const { t } = useTranslation('admin');
  const [stats, setStats] = useState<DashboardStats | null>(null);

  useEffect(() => {
    void getDashboardStatsApi().then(setStats);
  }, []);

  if (!stats) {
    return <div className="card p-8 text-sm text-slate-500">Loading dashboard...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">{t('dashboard.title')}</h1>
        <p className="mt-2 text-slate-500">{t('dashboard.subtitle')}</p>
      </div>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title={t('dashboard.totalEvents')} value={stats.totalEvents} />
        <StatCard title={t('dashboard.totalUsers')} value={stats.totalUsers} />
        <StatCard title={t('dashboard.confirmedBookings')} value={stats.confirmedBookings} />
        <StatCard title={t('dashboard.waitingEntries')} value={stats.waitingEntries} />
        <StatCard title={t('dashboard.publishedEvents')} value={stats.publishedEvents} />
        <StatCard title={t('dashboard.upcomingEvents')} value={stats.upcomingEvents} />
        <StatCard title={t('dashboard.fillRate')} value={`${stats.averageFillRate}%`} />
      </div>
    </div>
  );
}
