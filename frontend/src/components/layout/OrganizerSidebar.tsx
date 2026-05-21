import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export function OrganizerSidebar() {
  const { t } = useTranslation('organizer');
  const itemClass = ({ isActive }: { isActive: boolean }) =>
    `rounded-xl px-4 py-3 text-sm font-medium ${isActive ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'}`;

  return (
    <aside className="card h-fit p-4">
      <nav className="flex flex-col gap-2">
        <NavLink to="/organizer" end className={itemClass}>{t('nav.dashboard')}</NavLink>
        <NavLink to="/organizer/events" className={itemClass}>{t('nav.myEvents')}</NavLink>
      </nav>
    </aside>
  );
}
