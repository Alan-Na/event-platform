import { FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Input } from '@/components/common/Input';
import { Select } from '@/components/common/Select';
import { Button } from '@/components/common/Button';

interface RegisterFormProps {
  onSubmit: (values: { email: string; password: string; fullName: string; preferredLanguage: string }) => Promise<void>;
  loading?: boolean;
}

export function RegisterForm({ onSubmit, loading = false }: RegisterFormProps) {
  const { t } = useTranslation('auth');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [preferredLanguage, setPreferredLanguage] = useState('en');
  const [error, setError] = useState('');

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!email || !password || !fullName) {
      setError(t('validation.required'));
      return;
    }
    setError('');
    await onSubmit({ email, password, fullName, preferredLanguage });
  };

  return (
    <form onSubmit={handleSubmit} className="card mx-auto flex w-full max-w-md flex-col gap-4 p-6">
      <h1 className="text-2xl font-semibold">{t('register.title')}</h1>
      <Input label={t('fields.fullName')} value={fullName} onChange={(event) => setFullName(event.target.value)} />
      <Input label={t('fields.email')} value={email} onChange={(event) => setEmail(event.target.value)} type="email" />
      <Input label={t('fields.password')} value={password} onChange={(event) => setPassword(event.target.value)} type="password" />
      <Select
        label={t('fields.language')}
        value={preferredLanguage}
        onChange={(event) => setPreferredLanguage(event.target.value)}
        options={[
          { label: 'English', value: 'en' },
          { label: '中文', value: 'zh-CN' }
        ]}
      />
      {error ? <div className="text-sm text-rose-500">{error}</div> : null}
      <Button type="submit" fullWidth disabled={loading}>{loading ? t('actions.loading') : t('register.submit')}</Button>
    </form>
  );
}
