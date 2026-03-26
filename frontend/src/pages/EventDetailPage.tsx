import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getEventDetailApi } from '@/api/events.api';
import type { EventDetail } from '@/types/event';
import { BookingActionPanel } from '@/components/events/BookingActionPanel';
import { EventStatusBadge } from '@/components/events/EventStatusBadge';
import { formatDateRange, formatDateTime } from '@/utils/format';
import { useLocaleStore } from '@/store/locale.store';

export function EventDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation('events');
  const language = useLocaleStore((state) => state.language);
  const [event, setEvent] = useState<EventDetail | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchEvent = async () => {
    if (!id) return;
    setLoading(true);
    const result = await getEventDetailApi(id);
    setEvent(result);
    setLoading(false);
  };

  useEffect(() => {
    void fetchEvent();
  }, [id]);

  if (loading || !event) {
    return <div className="container-page card p-8 text-sm text-slate-500">Loading event...</div>;
  }

  return (
    <div className="container-page grid gap-8 lg:grid-cols-[1.2fr_0.8fr]">
      <div className="space-y-6">
        <div className="overflow-hidden rounded-[28px] bg-slate-200">
          {event.coverImageUrl ? <img src={event.coverImageUrl} alt={event.title} className="h-[360px] w-full object-cover" /> : null}
        </div>
        <div className="space-y-4">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-4xl font-semibold tracking-tight text-slate-900">{event.title}</h1>
              <p className="mt-3 text-lg text-slate-500">{event.summary}</p>
            </div>
            <EventStatusBadge status={event.status} />
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="card p-5">
              <div className="text-sm text-slate-500">{t('detail.schedule')}</div>
              <div className="mt-2 font-medium text-slate-900">{formatDateRange(event.startTime, event.endTime, language)}</div>
              <div className="mt-3 text-sm text-slate-600">{t('detail.deadline')}: {formatDateTime(event.registrationDeadline, language)}</div>
            </div>
            <div className="card p-5">
              <div className="text-sm text-slate-500">{t('detail.location')}</div>
              <div className="mt-2 font-medium text-slate-900">{event.locationName}</div>
              <div className="mt-3 text-sm text-slate-600">{event.address || event.city}</div>
            </div>
          </div>
          <div className="card p-5">
            <div className="grid gap-4 sm:grid-cols-3">
              <div>
                <div className="text-sm text-slate-500">{t('detail.capacity')}</div>
                <div className="mt-2 text-2xl font-semibold text-slate-900">{event.capacity}</div>
              </div>
              <div>
                <div className="text-sm text-slate-500">{t('detail.remainingSeats')}</div>
                <div className="mt-2 text-2xl font-semibold text-slate-900">{event.remainingSeats}</div>
              </div>
              <div>
                <div className="text-sm text-slate-500">{t('detail.waitlistCount')}</div>
                <div className="mt-2 text-2xl font-semibold text-slate-900">{event.waitlistCount}</div>
              </div>
            </div>
          </div>
          <div className="card p-6">
            <h2 className="text-xl font-semibold text-slate-900">{t('detail.about')}</h2>
            <p className="mt-4 whitespace-pre-line text-slate-700">{event.description}</p>
            <div className="mt-6 flex flex-wrap gap-2">
              {event.tags.map((tag) => (
                <span key={tag} className="rounded-full bg-slate-100 px-3 py-1.5 text-sm text-slate-600">#{tag}</span>
              ))}
            </div>
          </div>
        </div>
      </div>
      <div className="space-y-4">
        <BookingActionPanel event={event} onRefresh={fetchEvent} />
      </div>
    </div>
  );
}
