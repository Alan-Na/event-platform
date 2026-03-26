import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { EventForm } from '@/components/admin/EventForm';
import { createAdminEventApi } from '@/api/admin.api';
import { useUiStore } from '@/store/ui.store';
import type { EventUpsertRequest } from '@/types/event';

export function AdminEventCreatePage() {
  const { t } = useTranslation('admin');
  const navigate = useNavigate();
  const addToast = useUiStore((state) => state.addToast);

  const handleSubmit = async (payload: EventUpsertRequest) => {
    await createAdminEventApi(payload);
    addToast({ title: t('messages.created'), tone: 'success' });
    navigate('/admin/events');
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">{t('create.title')}</h1>
        <p className="mt-2 text-slate-500">{t('create.subtitle')}</p>
      </div>
      <EventForm onSubmit={handleSubmit} />
    </div>
  );
}
