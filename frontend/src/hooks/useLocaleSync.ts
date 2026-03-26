import { useEffect } from 'react';
import i18n from '@/i18n';
import { useLocaleStore } from '@/store/locale.store';

export function useLocaleSync() {
  const language = useLocaleStore((state) => state.language);

  useEffect(() => {
    if (i18n.language !== language) {
      void i18n.changeLanguage(language);
    }
  }, [language]);
}
