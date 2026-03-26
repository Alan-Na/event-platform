import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { NotificationItem } from '@/types/notification';

export function NotificationList({ items }: { items: NotificationItem[] }) {
  const { i18n } = useTranslation();

  const parsedItems = useMemo(
    () => items.map((item) => {
      try {
        return { ...item, payload: JSON.parse(item.payloadJson || '{}') as Record<string, string> };
      } catch {
        return { ...item, payload: {} as Record<string, string> };
      }
    }),
    [items]
  );

  return (
    <div className="space-y-3">
      {parsedItems.map((item) => (
        <div key={item.id} className="card p-4">
          <div className="text-sm font-medium text-slate-900">{i18n.t(item.templateKey, item.payload)}</div>
          <div className="mt-2 text-xs text-slate-500">{new Date(item.createdAt).toLocaleString()}</div>
        </div>
      ))}
    </div>
  );
}
