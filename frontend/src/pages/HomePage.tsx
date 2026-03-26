import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { getFeaturedEventsApi } from '@/api/events.api';
import type { EventSummary } from '@/types/event';
import { FeaturedEvents } from '@/components/events/FeaturedEvents';
import { Button } from '@/components/common/Button';

export function HomePage() {
  const { t } = useTranslation(['events', 'common']);
  const [items, setItems] = useState<EventSummary[]>([]);

  useEffect(() => {
    void getFeaturedEventsApi(6).then(setItems);
  }, []);

  return (
    <div className="container-page space-y-16">
      <section className="rounded-[32px] bg-gradient-to-br from-slate-900 to-slate-700 px-8 py-16 text-white">
        <div>
          <p className="mb-4 inline-flex rounded-full bg-white/10 px-4 py-2 text-sm">Event Booking & Waitlist Platform</p>
          <h1 className="max-w-2xl text-4xl font-semibold tracking-tight sm:text-5xl">{t('events:home.heroTitle')}</h1>
          <p className="mt-5 max-w-2xl text-lg text-slate-200">{t('events:home.heroSubtitle')}</p>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link to="/events"><Button>{t('events:home.ctaBrowse')}</Button></Link>
            <Link to="/register"><Button variant="secondary">{t('events:home.ctaStart')}</Button></Link>
          </div>
        </div>
      </section>

      <section className="space-y-6">
        <div className="flex items-center justify-between gap-4">
          <h2 className="section-title">{t('events:home.featuredTitle')}</h2>
          <Link to="/events" className="text-sm font-medium text-slate-600 hover:text-slate-900">{t('common:actions.search')}</Link>
        </div>
        <FeaturedEvents items={items} />
      </section>
    </div>
  );
}
