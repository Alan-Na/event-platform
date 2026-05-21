import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getOrganizerEventBookingsApi, getOrganizerCsvUrl, type AdminBookingItem } from '@/api/organizer.api';
import { Button } from '@/components/common/Button';
import { Spinner } from '@/components/common/Spinner';
import { ErrorState } from '@/components/common/ErrorState';
import { formatDateTime } from '@/utils/format';
import { useLocaleStore } from '@/store/locale.store';

export function OrganizerRegistrationsPage() {
  const { t } = useTranslation('organizer');
  const { id } = useParams();
  const language = useLocaleStore((state) => state.language);
  const [bookings, setBookings] = useState<AdminBookingItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!id) return;
    getOrganizerEventBookingsApi(Number(id))
      .then((res) => setBookings(res.items))
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spinner />;
  if (error) return <ErrorState />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="section-title">{t('registrations.title')}</h1>
        <a href={getOrganizerCsvUrl(Number(id))} download>
          <Button variant="secondary">{t('registrations.exportCsv')}</Button>
        </a>
      </div>
      <div className="card overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
          <thead className="bg-slate-50 text-slate-500">
            <tr>
              <th className="px-4 py-3">{t('registrations.bookingId')}</th>
              <th className="px-4 py-3">{t('registrations.name')}</th>
              <th className="px-4 py-3">{t('registrations.email')}</th>
              <th className="px-4 py-3">{t('registrations.status')}</th>
              <th className="px-4 py-3">{t('registrations.ticket')}</th>
              <th className="px-4 py-3">{t('registrations.code')}</th>
              <th className="px-4 py-3">{t('registrations.checkedIn')}</th>
              <th className="px-4 py-3">{t('registrations.bookedAt')}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {bookings.map((b) => (
              <tr key={b.bookingId}>
                <td className="px-4 py-3 font-mono text-slate-600">#{b.bookingId}</td>
                <td className="px-4 py-3 text-slate-900">{b.userFullName}</td>
                <td className="px-4 py-3 text-slate-500">{b.userEmail}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2 py-1 text-xs font-medium ${b.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'}`}>
                    {b.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-slate-500">{b.ticketTypeName ?? '-'}</td>
                <td className="px-4 py-3 font-mono text-xs text-slate-600">{b.confirmationCode ?? '-'}</td>
                <td className="px-4 py-3">
                  {b.checkedInAt ? (
                    <span className="text-green-600 font-medium">{formatDateTime(b.checkedInAt, language)}</span>
                  ) : (
                    <span className="text-slate-400">-</span>
                  )}
                </td>
                <td className="px-4 py-3 text-slate-500">{formatDateTime(b.bookedAt, language)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {bookings.length === 0 && (
          <div className="p-8 text-center text-sm text-slate-500">No registrations yet</div>
        )}
      </div>
    </div>
  );
}
