import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { setStoredLanguage } from '@/utils/storage';

interface LocaleState {
  language: string;
  setLanguage: (language: string) => void;
}

export const useLocaleStore = create<LocaleState>()(
  persist(
    (set) => ({
      language: 'en',
      setLanguage(language) {
        setStoredLanguage(language);
        set({ language });
      }
    }),
    { name: 'eventflow-locale' }
  )
);
