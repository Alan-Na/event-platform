import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Select } from '@/components/common/Select';
import { EventTable } from '@/components/admin/EventTable';
import {
  getMyEventsApi,
  publishOrganizerEventApi,
  closeOrganizerEventApi,
} from '@/api/organizer.api';
import type { EventSummary } from '@/types/event';
import { useUiStore } from '@/store/ui.store';

export function OrganizerEventsPage() {
  const { t } = useTranslation(['organizer', 'admin']);
  const addToast = useUiStore((state) => state.addToast);
  const [items, setItems] = useState<EventSummary[]>([]);
  const [status, setStatus] = useState('');
  const [keyword, setKeyword] = useState('');

  const fetchItems = async () => {
    const result = await getMyEventsApi({ status: status || undefined, keyword: keyword || undefined });
    setItems(result.items);
  };

  useEffect(() => {
    void fetchItems();
  }, [status]);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="section-title">{t('organizer:events.title')}</h1>
        <Link to="/organizer/events/new">
          <Button>{t('organizer:events.createNew')}</Button>
        </Link>
      </div>
      <div className="card grid gap-4 p-4 md:grid-cols-[1fr_220px_auto]">
        <Input label={t('admin:filters.keyword')} value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        <Select
          label={t('admin:filters.status')}
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          options={[
            { label: t('admin:filters.allStatuses'), value: '' },
            { label: 'DRAFT', value: 'DRAFT' },
            { label: 'PUBLISHED', value: 'PUBLISHED' },
            { label: 'CLOSED', value: 'CLOSED' },
            { label: 'CANCELLED', value: 'CANCELLED' },
          ]}
        />
        <div className="flex items-end">
          <Button variant="secondary" onClick={() => void fetchItems()}>{t('admin:actions.search')}</Button>
        </div>
      </div>
      <EventTable
        items={items}
        editBasePath="/organizer/events"
        registrationsBasePath="/organizer/events"
        onPublish={async (id) => {
          await publishOrganizerEventApi(id);
          addToast({ title: 'Event published', tone: 'success' });
          await fetchItems();
        }}
        onClose={async (id) => {
          await closeOrganizerEventApi(id);
          addToast({ title: 'Event closed', tone: 'info' });
          await fetchItems();
        }}
      />
    </div>
  );
}
