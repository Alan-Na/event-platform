import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { EventForm } from '@/components/admin/EventForm';
import { updateAdminEventApi } from '@/api/admin.api';
import { apiClient } from '@/api/axios';
import type { ApiResponse } from '@/types/common';
import type { EventDetail, EventUpsertRequest } from '@/types/event';
import { useUiStore } from '@/store/ui.store';

export function AdminEventEditPage() {
  const { t } = useTranslation('admin');
  const { id } = useParams();
  const navigate = useNavigate();
  const addToast = useUiStore((state) => state.addToast);
  const [event, setEvent] = useState<EventDetail | null>(null);

  useEffect(() => {
    if (!id) return;
    void apiClient.get<ApiResponse<EventDetail>>(`/admin/events/${id}`).then((response) => setEvent(response.data.data));
  }, [id]);

  const initialValue = useMemo(() => {
    if (!event) return undefined;
    return {
      title: event.title,
      summary: event.summary,
      description: event.description,
      coverImageUrl: event.coverImageUrl,
      locationName: event.locationName,
      address: event.address,
      city: event.city,
      startTime: event.startTime.slice(0, 16),
      endTime: event.endTime.slice(0, 16),
      registrationDeadline: event.registrationDeadline.slice(0, 16),
      capacity: event.capacity,
      featured: event.featured,
      categoryCode: event.categoryCode,
      tags: event.tags
    } satisfies Partial<EventUpsertRequest>;
  }, [event]);

  if (!event) {
    return <div className="card p-8 text-sm text-slate-500">Loading event...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">{t('edit.title')}</h1>
        <p className="mt-2 text-slate-500">{t('edit.subtitle')}</p>
      </div>
      <EventForm
        initialValue={initialValue}
        onSubmit={async (payload) => {
          if (!id) return;
          await updateAdminEventApi(id, payload);
          addToast({ title: t('messages.saved'), tone: 'success' });
          navigate('/admin/events');
        }}
      />
    </div>
  );
}
