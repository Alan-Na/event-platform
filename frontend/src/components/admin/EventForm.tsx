import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { EventUpsertRequest } from '@/types/event';
import { Input } from '@/components/common/Input';
import { Select } from '@/components/common/Select';
import { Button } from '@/components/common/Button';

interface EventFormProps {
  initialValue?: Partial<EventUpsertRequest>;
  loading?: boolean;
  onSubmit: (payload: EventUpsertRequest) => Promise<void>;
}

const defaultValue: EventUpsertRequest = {
  title: '',
  summary: '',
  description: '',
  coverImageUrl: '',
  locationName: '',
  address: '',
  city: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  capacity: 10,
  featured: false,
  categoryCode: 'TECH',
  tags: []
};

export function EventForm({ initialValue, loading = false, onSubmit }: EventFormProps) {
  const { t } = useTranslation('admin');
  const [value, setValue] = useState<EventUpsertRequest>({ ...defaultValue, ...initialValue });
  const [tagsInput, setTagsInput] = useState((initialValue?.tags || []).join(', '));

  useEffect(() => {
    setValue({ ...defaultValue, ...initialValue });
    setTagsInput((initialValue?.tags || []).join(', '));
  }, [initialValue]);

  const categories = useMemo(
    () => [
      { label: 'Technology', value: 'TECH' },
      { label: 'Career', value: 'CAREER' },
      { label: 'Design', value: 'DESIGN' },
      { label: 'Community', value: 'COMMUNITY' }
    ],
    []
  );

  const toIso = (raw: string) => (raw ? new Date(raw).toISOString() : raw);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    await onSubmit({
      ...value,
      startTime: toIso(value.startTime),
      endTime: toIso(value.endTime),
      registrationDeadline: toIso(value.registrationDeadline),
      tags: tagsInput
        .split(',')
        .map((tag) => tag.trim())
        .filter(Boolean),
      capacity: Number(value.capacity)
    });
  };

  return (
    <form onSubmit={handleSubmit} className="card grid gap-4 p-6 md:grid-cols-2">
      <Input label={t('form.title')} value={value.title} onChange={(event) => setValue({ ...value, title: event.target.value })} />
      <Input label={t('form.summary')} value={value.summary} onChange={(event) => setValue({ ...value, summary: event.target.value })} />
      <div className="md:col-span-2">
        <Input label={t('form.description')} value={value.description} onChange={(event) => setValue({ ...value, description: event.target.value })} />
      </div>
      <Input label={t('form.coverImageUrl')} value={value.coverImageUrl || ''} onChange={(event) => setValue({ ...value, coverImageUrl: event.target.value })} />
      <Select label={t('form.category')} value={value.categoryCode} onChange={(event) => setValue({ ...value, categoryCode: event.target.value })} options={categories} />
      <Input label={t('form.locationName')} value={value.locationName} onChange={(event) => setValue({ ...value, locationName: event.target.value })} />
      <Input label={t('form.address')} value={value.address || ''} onChange={(event) => setValue({ ...value, address: event.target.value })} />
      <Input label={t('form.city')} value={value.city} onChange={(event) => setValue({ ...value, city: event.target.value })} />
      <Input label={t('form.capacity')} type="number" value={value.capacity} onChange={(event) => setValue({ ...value, capacity: Number(event.target.value) })} />
      <Input label={t('form.startTime')} type="datetime-local" value={value.startTime} onChange={(event) => setValue({ ...value, startTime: event.target.value })} />
      <Input label={t('form.endTime')} type="datetime-local" value={value.endTime} onChange={(event) => setValue({ ...value, endTime: event.target.value })} />
      <Input label={t('form.registrationDeadline')} type="datetime-local" value={value.registrationDeadline} onChange={(event) => setValue({ ...value, registrationDeadline: event.target.value })} />
      <Input label={t('form.tags')} value={tagsInput} onChange={(event) => setTagsInput(event.target.value)} />
      <label className="flex items-center gap-3 text-sm font-medium text-slate-700 md:col-span-2">
        <input type="checkbox" checked={value.featured} onChange={(event) => setValue({ ...value, featured: event.target.checked })} />
        {t('form.featured')}
      </label>
      <div className="md:col-span-2">
        <Button type="submit" disabled={loading}>{loading ? t('actions.saving') : t('actions.save')}</Button>
      </div>
    </form>
  );
}
