import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Select } from '@/components/common/Select';
import { EventTable } from '@/components/admin/EventTable';
import { getAdminEventsApi, publishAdminEventApi, closeAdminEventApi, cancelAdminEventApi } from '@/api/admin.api';
import type { EventSummary } from '@/types/event';
import { useUiStore } from '@/store/ui.store';

export function AdminEventsPage() {
  const { t } = useTranslation('admin');
  const addToast = useUiStore((state) => state.addToast);
  const [items, setItems] = useState<EventSummary[]>([]);
  const [status, setStatus] = useState('');
  const [keyword, setKeyword] = useState('');

  const fetchItems = async () => {
    const result = await getAdminEventsApi({ status: status || undefined, keyword: keyword || undefined, page: 0, size: 20 });
    setItems(result.items);
  };

  useEffect(() => {
    void fetchItems();
  }, [status]);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="section-title">{t('events.title')}</h1>
          <p className="mt-2 text-slate-500">{t('events.subtitle')}</p>
        </div>
        <Link to="/admin/events/new"><Button>{t('actions.createEvent')}</Button></Link>
      </div>
      <div className="card grid gap-4 p-4 md:grid-cols-[1fr_220px_auto]">
        <Input label={t('filters.keyword')} value={keyword} onChange={(event) => setKeyword(event.target.value)} />
        <Select
          label={t('filters.status')}
          value={status}
          onChange={(event) => setStatus(event.target.value)}
          options={[
            { label: t('filters.allStatuses'), value: '' },
            { label: 'DRAFT', value: 'DRAFT' },
            { label: 'PUBLISHED', value: 'PUBLISHED' },
            { label: 'CLOSED', value: 'CLOSED' },
            { label: 'CANCELLED', value: 'CANCELLED' }
          ]}
        />
        <div className="flex items-end"><Button variant="secondary" onClick={() => void fetchItems()}>{t('actions.search')}</Button></div>
      </div>
      <EventTable
        items={items}
        onPublish={async (id) => {
          await publishAdminEventApi(id);
          addToast({ title: t('messages.published'), tone: 'success' });
          await fetchItems();
        }}
        onClose={async (id) => {
          await closeAdminEventApi(id);
          addToast({ title: t('messages.closed'), tone: 'info' });
          await fetchItems();
        }}
        onCancel={async (id) => {
          await cancelAdminEventApi(id, 'Cancelled by admin');
          addToast({ title: t('messages.cancelled'), tone: 'error' });
          await fetchItems();
        }}
      />
    </div>
  );
}
