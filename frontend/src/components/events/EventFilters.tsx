import { useTranslation } from 'react-i18next';
import { Input } from '@/components/common/Input';
import { Select } from '@/components/common/Select';
import { Button } from '@/components/common/Button';
import { useEventFiltersStore } from '@/store/eventFilters.store';

export function EventFilters() {
  const { t } = useTranslation('events');
  const filters = useEventFiltersStore();

  return (
    <div className="card grid gap-4 p-4 md:grid-cols-2 xl:grid-cols-6">
      <Input
        label={t('list.search')}
        placeholder={t('list.searchPlaceholder')}
        value={filters.keyword}
        onChange={(event) => filters.setField('keyword', event.target.value)}
      />
      <Select
        label={t('list.filterCategory')}
        value={filters.category}
        onChange={(event) => filters.setField('category', event.target.value)}
        options={[
          { label: t('list.allCategories'), value: '' },
          { label: t('categories.TECH'), value: 'TECH' },
          { label: t('categories.CAREER'), value: 'CAREER' },
          { label: t('categories.DESIGN'), value: 'DESIGN' },
          { label: t('categories.COMMUNITY'), value: 'COMMUNITY' }
        ]}
      />
      <Input label={t('list.filterCity')} value={filters.city} onChange={(event) => filters.setField('city', event.target.value)} />
      <Input label={t('list.dateFrom')} type="date" value={filters.startDateFrom} onChange={(event) => filters.setField('startDateFrom', event.target.value)} />
      <Input label={t('list.dateTo')} type="date" value={filters.startDateTo} onChange={(event) => filters.setField('startDateTo', event.target.value)} />
      <div className="flex flex-col justify-end gap-2">
        <Select
          label={t('list.sort')}
          value={filters.sort}
          onChange={(event) => filters.setField('sort', event.target.value)}
          options={[
            { label: t('list.sortStartAsc'), value: 'START_ASC' },
            { label: t('list.sortStartDesc'), value: 'START_DESC' },
            { label: t('list.sortNewest'), value: 'NEWEST' },
            { label: t('list.sortPopular'), value: 'POPULAR' }
          ]}
        />
        <Button variant="secondary" onClick={filters.resetFilters}>{t('list.reset')}</Button>
      </div>
    </div>
  );
}
