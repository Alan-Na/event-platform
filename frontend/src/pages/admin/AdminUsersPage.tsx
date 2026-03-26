import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { UserTable } from '@/components/admin/UserTable';
import { getAdminUsersApi, type AdminUserOverview } from '@/api/admin.api';

export function AdminUsersPage() {
  const { t } = useTranslation('admin');
  const [keyword, setKeyword] = useState('');
  const [items, setItems] = useState<AdminUserOverview[]>([]);

  const fetchUsers = async () => {
    const result = await getAdminUsersApi({ keyword: keyword || undefined, page: 0, size: 30 });
    setItems(result.items);
  };

  useEffect(() => {
    void fetchUsers();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">{t('users.title')}</h1>
        <p className="mt-2 text-slate-500">{t('users.subtitle')}</p>
      </div>
      <div className="card grid gap-4 p-4 md:grid-cols-[1fr_auto]">
        <Input label={t('filters.keyword')} value={keyword} onChange={(event) => setKeyword(event.target.value)} />
        <div className="flex items-end"><Button variant="secondary" onClick={() => void fetchUsers()}>{t('actions.search')}</Button></div>
      </div>
      <UserTable items={items} />
    </div>
  );
}
