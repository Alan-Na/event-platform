import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import type { EventSummary } from '@/types/event';
import { EventStatusBadge } from '@/components/events/EventStatusBadge';
import { formatDateTime } from '@/utils/format';
import { useLocaleStore } from '@/store/locale.store';

export function EventCard({ event }: { event: EventSummary }) {
  const { t } = useTranslation('events');
  const language = useLocaleStore((state) => state.language);

  return (
    <Link to={`/events/${event.id}`} className="card overflow-hidden transition hover:-translate-y-1">
      <div className="aspect-[16/9] bg-slate-200">
        {event.coverImageUrl ? (
          <img src={event.coverImageUrl} alt={event.title} className="h-full w-full object-cover" />
        ) : null}
      </div>
      <div className="space-y-4 p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h3 className="text-lg font-semibold text-slate-900">{event.title}</h3>
            <p className="mt-1 text-sm text-slate-500">{event.city} · {event.locationName}</p>
          </div>
          <EventStatusBadge status={event.status} />
        </div>
        <p className="text-sm text-slate-600">{event.summary}</p>
        <div className="flex flex-wrap gap-2">
          {event.tags.slice(0, 3).map((tag) => (
            <span key={tag} className="rounded-full bg-slate-100 px-2.5 py-1 text-xs text-slate-600">#{tag}</span>
          ))}
        </div>
        <div className="grid grid-cols-2 gap-3 text-sm text-slate-500">
          <div>
            <span className="font-medium text-slate-700">{formatDateTime(event.startTime, language)}</span>
          </div>
          <div className="text-right">
            {t('detail.remainingSeats')}: <span className="font-medium text-slate-700">{event.remainingSeats}</span>
          </div>
        </div>
      </div>
    </Link>
  );
}
