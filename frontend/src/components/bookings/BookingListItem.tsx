import { useTranslation } from 'react-i18next';
import type { MyBookingItem } from '@/types/booking';
import { Button } from '@/components/common/Button';
import { formatDateRange } from '@/utils/format';
import { useLocaleStore } from '@/store/locale.store';

interface BookingListItemProps {
  item: MyBookingItem;
  onCancel?: (eventId: number) => void;
}

export function BookingListItem({ item, onCancel }: BookingListItemProps) {
  const { t } = useTranslation('bookings');
  const language = useLocaleStore((state) => state.language);

  return (
    <div className="card flex flex-col gap-4 p-5 md:flex-row md:items-center md:justify-between">
      <div>
        <h3 className="text-lg font-semibold text-slate-900">{item.eventTitle}</h3>
        <p className="mt-1 text-sm text-slate-500">{item.city} · {item.locationName}</p>
        <p className="mt-2 text-sm text-slate-600">{formatDateRange(item.startTime, item.endTime, language)}</p>
        {item.waitlistPosition ? <p className="mt-2 text-sm text-slate-600">{t('waitlistPosition', { position: item.waitlistPosition })}</p> : null}
      </div>
      <div className="flex items-center gap-3">
        <span className="rounded-full bg-slate-100 px-3 py-2 text-sm text-slate-700">{item.status}</span>
        {item.canCancel && onCancel ? <Button variant="danger" onClick={() => onCancel(item.eventId)}>{t('actions.cancel')}</Button> : null}
      </div>
    </div>
  );
}
