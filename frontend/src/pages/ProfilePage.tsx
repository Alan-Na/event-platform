import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import i18n from '@/i18n';
import { getProfileApi, updateProfileApi } from '@/api/auth.api';
import { useAuthStore } from '@/store/auth.store';
import { useLocaleStore } from '@/store/locale.store';
import { useNotificationStore } from '@/store/notification.store';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Select } from '@/components/common/Select';
import { NotificationList } from '@/components/notifications/NotificationList';
import { useUiStore } from '@/store/ui.store';

export function ProfilePage() {
  const { t } = useTranslation(['profile', 'common']);
  const addToast = useUiStore((state) => state.addToast);
  const setUser = useAuthStore((state) => state.setUser);
  const user = useAuthStore((state) => state.user);
  const setLanguage = useLocaleStore((state) => state.setLanguage);
  const notifications = useNotificationStore((state) => state.items);
  const fetchNotifications = useNotificationStore((state) => state.fetchNotifications);
  const [fullName, setFullName] = useState('');
  const [preferredLanguage, setPreferredLanguageLocal] = useState('en');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void getProfileApi().then((profile) => {
      setUser(profile);
      setFullName(profile.fullName);
      setPreferredLanguageLocal(profile.preferredLanguage);
      setLoading(false);
    });
    void fetchNotifications();
  }, [setUser, fetchNotifications]);

  if (loading || !user) {
    return <div className="container-page card p-8 text-sm text-slate-500">Loading profile...</div>;
  }

  return (
    <div className="container-page grid gap-6 lg:grid-cols-[1fr_0.9fr]">
      <div className="card space-y-4 p-6">
        <h1 className="text-2xl font-semibold text-slate-900">{t('profile:title')}</h1>
        <Input label={t('profile:fullName')} value={fullName} onChange={(event) => setFullName(event.target.value)} />
        <Input label={t('profile:email')} value={user.email} disabled />
        <Select
          label={t('profile:language')}
          value={preferredLanguage}
          onChange={(event) => setPreferredLanguageLocal(event.target.value)}
          options={[
            { label: 'English', value: 'en' },
            { label: '中文', value: 'zh-CN' }
          ]}
        />
        <Button
          onClick={async () => {
            const updated = await updateProfileApi({ fullName, preferredLanguage });
            setUser(updated);
            setLanguage(updated.preferredLanguage);
            void i18n.changeLanguage(updated.preferredLanguage);
            addToast({ title: t('profile:messages.saved'), tone: 'success' });
          }}
        >
          {t('common:actions.save')}
        </Button>
      </div>
      <div className="space-y-4">
        <h2 className="text-xl font-semibold text-slate-900">{t('profile:notifications')}</h2>
        <NotificationList items={notifications} />
      </div>
    </div>
  );
}
