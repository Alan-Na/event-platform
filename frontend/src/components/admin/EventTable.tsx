import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import type { EventSummary } from '@/types/event';
import { Button } from '@/components/common/Button';
import { EventStatusBadge } from '@/components/events/EventStatusBadge';
import { formatDateTime } from '@/utils/format';
import { useLocaleStore } from '@/store/locale.store';

interface EventTableProps {
  items: EventSummary[];
  onPublish: (id: number) => void;
  onClose: (id: number) => void;
  onCancel: (id: number) => void;
}

export function EventTable({ items, onPublish, onClose, onCancel }: EventTableProps) {
  const { t } = useTranslation('admin');
  const language = useLocaleStore((state) => state.language);

  return (
    <div className="card overflow-x-auto">
      <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead className="bg-slate-50 text-slate-500">
          <tr>
            <th className="px-4 py-3">{t('table.event')}</th>
            <th className="px-4 py-3">{t('table.time')}</th>
            <th className="px-4 py-3">{t('table.status')}</th>
            <th className="px-4 py-3">{t('table.capacity')}</th>
            <th className="px-4 py-3">{t('table.actions')}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={item.id}>
              <td className="px-4 py-4">
                <div className="font-medium text-slate-900">{item.title}</div>
                <div className="text-xs text-slate-500">{item.city}</div>
              </td>
              <td className="px-4 py-4 text-slate-600">{formatDateTime(item.startTime, language)}</td>
              <td className="px-4 py-4"><EventStatusBadge status={item.status} /></td>
              <td className="px-4 py-4 text-slate-600">{item.confirmedCount} / {item.capacity}</td>
              <td className="px-4 py-4">
                <div className="flex flex-wrap gap-2">
                  <Link to={`/admin/events/${item.id}/edit`}><Button variant="secondary">{t('actions.edit')}</Button></Link>
                  <Link to={`/admin/events/${item.id}/registrations`}><Button variant="secondary">{t('actions.viewRegistrations')}</Button></Link>
                  {item.status === 'DRAFT' || item.status === 'CLOSED' ? <Button onClick={() => onPublish(item.id)}>{t('actions.publish')}</Button> : null}
                  {item.status === 'PUBLISHED' ? <Button variant="secondary" onClick={() => onClose(item.id)}>{t('actions.close')}</Button> : null}
                  {item.status !== 'CANCELLED' ? <Button variant="danger" onClick={() => onCancel(item.id)}>{t('actions.cancel')}</Button> : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
