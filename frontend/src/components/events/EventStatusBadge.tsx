import { useTranslation } from 'react-i18next';
import { Badge } from '@/components/common/Badge';
import type { EventStatus } from '@/types/event';

export function EventStatusBadge({ status }: { status: EventStatus }) {
  const { t } = useTranslation('common');
  const classes: Record<EventStatus, string> = {
    DRAFT: 'border-slate-200 bg-slate-100 text-slate-700',
    PUBLISHED: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    CLOSED: 'border-amber-200 bg-amber-50 text-amber-700',
    CANCELLED: 'border-rose-200 bg-rose-50 text-rose-700'
  };
  return <Badge className={classes[status]}>{t(`status.${status.toLowerCase()}`)}</Badge>;
}
