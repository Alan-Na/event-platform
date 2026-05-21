import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import axios from 'axios';
import { EventForm } from '@/components/admin/EventForm';
import { createOrganizerEventApi } from '@/api/organizer.api';
import { useUiStore } from '@/store/ui.store';
import type { EventUpsertRequest } from '@/types/event';
import type { ApiValidationError } from '@/types/common';

export function OrganizerEventCreatePage() {
  const { t } = useTranslation('admin');
  const navigate = useNavigate();
  const addToast = useUiStore((state) => state.addToast);
  const [saving, setSaving] = useState(false);

  const handleSubmit = async (payload: EventUpsertRequest) => {
    setSaving(true);
    try {
      await createOrganizerEventApi(payload);
      addToast({ title: t('messages.created'), tone: 'success' });
      navigate('/organizer/events');
    } catch (error) {
      let message = 'Save failed';
      if (axios.isAxiosError<ApiValidationError>(error)) {
        message = error.response?.data?.errors?.[0]?.message || error.response?.data?.message || message;
      }
      addToast({ title: message, tone: 'error' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="section-title">{t('create.title')}</h1>
      <EventForm loading={saving} onSubmit={handleSubmit} />
    </div>
  );
}
