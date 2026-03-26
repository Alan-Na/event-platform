import { Link, NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { LanguageSwitcher } from '@/components/layout/LanguageSwitcher';
import { useAuthStore } from '@/store/auth.store';
import { Button } from '@/components/common/Button';
import { isAdmin } from '@/utils/guards';
import { NotificationBell } from '@/components/notifications/NotificationBell';

export function Navbar() {
  const { t } = useTranslation('common');
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `text-sm font-medium ${isActive ? 'text-slate-900' : 'text-slate-500 hover:text-slate-900'}`;

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="container-page flex h-16 items-center justify-between gap-4">
        <Link to="/" className="text-lg font-bold tracking-tight text-slate-900">
          EventFlow
        </Link>
        <nav className="hidden items-center gap-6 md:flex">
          <NavLink to="/" className={navLinkClass}>{t('nav.home')}</NavLink>
          <NavLink to="/events" className={navLinkClass}>{t('nav.events')}</NavLink>
          {isAuthenticated ? <NavLink to="/bookings" className={navLinkClass}>{t('nav.myBookings')}</NavLink> : null}
          {isAuthenticated ? <NavLink to="/profile" className={navLinkClass}>{t('nav.profile')}</NavLink> : null}
          {isAdmin(user) ? <NavLink to="/admin" className={navLinkClass}>{t('nav.admin')}</NavLink> : null}
        </nav>
        <div className="flex items-center gap-3">
          <NotificationBell />
          <LanguageSwitcher />
          {!isAuthenticated ? (
            <>
              <Link to="/login"><Button variant="ghost">{t('nav.login')}</Button></Link>
              <Link to="/register"><Button>{t('nav.register')}</Button></Link>
            </>
          ) : (
            <Button variant="secondary" onClick={logout}>{t('nav.logout')}</Button>
          )}
        </div>
      </div>
    </header>
  );
}
