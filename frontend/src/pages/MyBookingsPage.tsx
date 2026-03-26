import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { BookingTabs } from '@/components/bookings/BookingTabs';
import { BookingListItem } from '@/components/bookings/BookingListItem';
import { EmptyState } from '@/components/common/EmptyState';
import { getMyBookingsApi, cancelBookingApi } from '@/api/bookings.api';
import type { MyBookingItem } from '@/types/booking';
import { useUiStore } from '@/store/ui.store';

export function MyBookingsPage() {
  const { t } = useTranslation('bookings');
  const addToast = useUiStore((state) => state.addToast);
  const [tab, setTab] = useState('ALL');
  const [items, setItems] = useState<MyBookingItem[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchItems = async () => {
    setLoading(true);
    const result = await getMyBookingsApi(tab, 0, 20);
    setItems(result.items);
    setLoading(false);
  };

  useEffect(() => {
    void fetchItems();
  }, [tab]);

  const handleCancel = async (eventId: number) => {
    await cancelBookingApi(eventId);
    addToast({ title: t('messages.cancelled'), tone: 'info' });
    await fetchItems();
  };

  return (
    <div className="container-page space-y-6">
      <div>
        <h1 className="section-title">{t('title')}</h1>
        <p className="mt-2 text-slate-500">{t('subtitle')}</p>
      </div>
      <BookingTabs value={tab} onChange={setTab} />
      {loading ? <div className="card p-8 text-sm text-slate-500">{t('loading')}</div> : null}
      {!loading && items.length === 0 ? <EmptyState>{t('empty')}</EmptyState> : null}
      <div className="space-y-4">
        {items.map((item) => (
          <BookingListItem key={`${item.recordType}-${item.eventId}-${item.status}`} item={item} onCancel={handleCancel} />
        ))}
      </div>
    </div>
  );
}
