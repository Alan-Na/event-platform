import { Link } from 'react-router-dom';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { EventDetail } from '@/types/event';
import { Button } from '@/components/common/Button';
import { createBookingApi, cancelBookingApi } from '@/api/bookings.api';
import { useAuthStore } from '@/store/auth.store';
import { useUiStore } from '@/store/ui.store';

interface BookingActionPanelProps {
  event: EventDetail;
  onRefresh: () => Promise<void> | void;
}

export function BookingActionPanel({ event, onRefresh }: BookingActionPanelProps) {
  const { t, i18n } = useTranslation(['events', 'notifications']);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const addToast = useUiStore((state) => state.addToast);
  const [loading, setLoading] = useState(false);

  const registrationState = event.currentUserRegistration?.state;

  const handleBook = async () => {
    setLoading(true);
    try {
      const result = await createBookingApi(event.id);
      addToast({ title: i18n.t(result.messageKey), tone: 'success' });
      await onRefresh();
    } catch (error) {
      addToast({ title: t('detail.actionFailed'), tone: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    setLoading(true);
    try {
      const result = await cancelBookingApi(event.id);
      addToast({ title: i18n.t(result.messageKey), tone: 'info' });
      await onRefresh();
    } catch {
      addToast({ title: t('detail.actionFailed'), tone: 'error' });
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="card flex flex-col gap-4 p-5">
        <div className="text-sm text-slate-600">{t('detail.loginToRegister')}</div>
        <Link to="/login">
          <Button fullWidth>{t('detail.loginToRegister')}</Button>
        </Link>
      </div>
    );
  }

  if (registrationState === 'CONFIRMED' || registrationState === 'WAITING') {
    return (
      <div className="card space-y-4 p-5">
        <div className="text-sm text-slate-600">
          {registrationState === 'CONFIRMED' ? t('detail.alreadyBooked') : t('detail.alreadyWaiting')}
        </div>
        <Button fullWidth variant="danger" onClick={handleCancel} disabled={loading}>
          {t('detail.cancelBooking')}
        </Button>
      </div>
    );
  }

  if (!event.bookable) {
    return (
      <div className="card space-y-4 p-5">
        <div className="text-sm text-slate-600">{t(`detail.reasons.${event.bookableReason || 'EVENT_CLOSED'}`)}</div>
        <Button fullWidth variant="secondary" disabled>
          {t('detail.unavailable')}
        </Button>
      </div>
    );
  }

  return (
    <div className="card space-y-4 p-5">
      <div className="text-sm text-slate-600">
        {event.remainingSeats > 0 ? t('detail.seatsAvailable', { count: event.remainingSeats }) : t('detail.waitlistOpen')}
      </div>
      <Button fullWidth onClick={handleBook} disabled={loading}>
        {event.remainingSeats > 0 ? t('detail.register') : t('detail.joinWaitlist')}
      </Button>
    </div>
  );
}
