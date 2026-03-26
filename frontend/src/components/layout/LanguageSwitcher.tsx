import { useTranslation } from 'react-i18next';
import { useLocaleStore } from '@/store/locale.store';

export function LanguageSwitcher() {
  const { i18n, t } = useTranslation('common');
  const setLanguage = useLocaleStore((state) => state.setLanguage);

  const toggleLanguage = () => {
    const nextLanguage = i18n.language === 'en' ? 'zh-CN' : 'en';
    setLanguage(nextLanguage);
    void i18n.changeLanguage(nextLanguage);
  };

  return (
    <button
      onClick={toggleLanguage}
      className="rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-700 hover:bg-slate-100"
      aria-label={t('nav.language')}
    >
      {i18n.language === 'en' ? '中文' : 'English'}
    </button>
  );
}
