import { FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';

interface LoginFormProps {
  onSubmit: (values: { email: string; password: string }) => Promise<void>;
  loading?: boolean;
}

export function LoginForm({ onSubmit, loading = false }: LoginFormProps) {
  const { t } = useTranslation('auth');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!email || !password) {
      setError(t('validation.required'));
      return;
    }
    setError('');
    await onSubmit({ email, password });
  };

  return (
    <form onSubmit={handleSubmit} className="card mx-auto flex w-full max-w-md flex-col gap-4 p-6">
      <h1 className="text-2xl font-semibold">{t('login.title')}</h1>
      <Input label={t('fields.email')} value={email} onChange={(event) => setEmail(event.target.value)} type="email" />
      <Input label={t('fields.password')} value={password} onChange={(event) => setPassword(event.target.value)} type="password" />
      {error ? <div className="text-sm text-rose-500">{error}</div> : null}
      <Button type="submit" fullWidth disabled={loading}>{loading ? t('actions.loading') : t('login.submit')}</Button>
    </form>
  );
}
