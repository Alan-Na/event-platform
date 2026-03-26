import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { EventFilters } from '@/components/events/EventFilters';
import { EmptyState } from '@/components/common/EmptyState';
import { ErrorState } from '@/components/common/ErrorState';
import { Pagination } from '@/components/common/Pagination';
import { EventCard } from '@/components/events/EventCard';
import { getEventsApi } from '@/api/events.api';
import type { EventSummary } from '@/types/event';
import { useEventFiltersStore } from '@/store/eventFilters.store';
import { useDebounce } from '@/hooks/useDebounce';

export function EventsPage() {
  const { t } = useTranslation('events');
  const filters = useEventFiltersStore();
  const debouncedKeyword = useDebounce(filters.keyword);
  const [items, setItems] = useState<EventSummary[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const fetchEvents = async () => {
    setLoading(true);
    setError(false);
    try {
      const result = await getEventsApi({
        keyword: debouncedKeyword,
        category: filters.category || undefined,
        city: filters.city || undefined,
        startDateFrom: filters.startDateFrom || undefined,
        startDateTo: filters.startDateTo || undefined,
        sort: filters.sort,
        page: filters.page,
        size: filters.size
      });
      setItems(result.items);
      setTotalPages(result.totalPages);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void fetchEvents();
  }, [debouncedKeyword, filters.category, filters.city, filters.startDateFrom, filters.startDateTo, filters.sort, filters.page, filters.size]);

  return (
    <div className="container-page space-y-6">
      <div>
        <h1 className="section-title">{t('list.title')}</h1>
        <p className="mt-2 text-slate-500">{t('list.subtitle')}</p>
      </div>
      <EventFilters />
      {loading ? <div className="card p-8 text-sm text-slate-500">{t('list.loading')}</div> : null}
      {error ? <ErrorState onRetry={() => void fetchEvents()}>{t('list.error')}</ErrorState> : null}
      {!loading && !error && items.length === 0 ? <EmptyState>{t('list.empty')}</EmptyState> : null}
      {!loading && !error && items.length > 0 ? (
        <>
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {items.map((item) => (
              <EventCard key={item.id} event={item} />
            ))}
          </div>
          <Pagination page={filters.page} totalPages={totalPages} onChange={(page) => filters.setField('page', page)} />
        </>
      ) : null}
    </div>
  );
}
