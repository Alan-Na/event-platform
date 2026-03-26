import { useTranslation } from 'react-i18next';
import { cn } from '@/utils/cn';

const tabs = ['ALL', 'CONFIRMED', 'WAITING', 'CANCELLED'] as const;

export function BookingTabs({ value, onChange }: { value: string; onChange: (value: string) => void }) {
  const { t } = useTranslation('bookings');
  return (
    <div className="flex flex-wrap gap-2">
      {tabs.map((tab) => (
        <button
          key={tab}
          className={cn(
            'rounded-full px-4 py-2 text-sm font-medium',
            value === tab ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
          )}
          onClick={() => onChange(tab)}
        >
          {t(`tabs.${tab.toLowerCase()}`)}
        </button>
      ))}
    </div>
  );
}
