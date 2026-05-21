import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { checkInApi, type CheckInResponse } from '@/api/organizer.api';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { useUiStore } from '@/store/ui.store';
import { formatDateTime } from '@/utils/format';
import { useLocaleStore } from '@/store/locale.store';

export function OrganizerCheckInPage() {
  const { t } = useTranslation('organizer');
  const { id } = useParams();
  const addToast = useUiStore((state) => state.addToast);
  const language = useLocaleStore((state) => state.language);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<CheckInResponse | null>(null);

  const handleCheckIn = async () => {
    if (!id || !code.trim()) return;
    setLoading(true);
    setResult(null);
    try {
      const response = await checkInApi(Number(id), code.trim().toUpperCase());
      setResult(response);
      addToast({ title: t('checkin.success'), tone: 'success' });
      setCode('');
    } catch (error: unknown) {
      const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Check-in failed';
      addToast({ title: message, tone: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">{t('checkin.title')}</h1>
        <p className="mt-2 text-slate-500">{t('checkin.subtitle')}</p>
      </div>
      <div className="card max-w-md space-y-4 p-6">
        <Input
          label={t('checkin.confirmationCode')}
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder={t('checkin.placeholder')}
          onKeyDown={(e) => { if (e.key === 'Enter') void handleCheckIn(); }}
        />
        <Button fullWidth onClick={() => void handleCheckIn()} disabled={loading || !code.trim()}>
          {t('checkin.submit')}
        </Button>
      </div>
      {result && (
        <div className="card max-w-md space-y-3 border-green-200 p-6">
          <div className="text-sm font-medium text-green-700">{t('checkin.success')}</div>
          <div className="space-y-1 text-sm text-slate-700">
            <div><span className="font-medium">{t('checkin.attendee')}:</span> {result.userDisplayName}</div>
            <div><span className="font-medium">{t('checkin.email')}:</span> {result.userEmail}</div>
            {result.ticketTypeName && <div><span className="font-medium">{t('checkin.ticket')}:</span> {result.ticketTypeName}</div>}
            <div><span className="font-medium">{t('checkin.checkedInAt')}:</span> {formatDateTime(result.checkedInAt, language)}</div>
          </div>
        </div>
      )}
    </div>
  );
}
