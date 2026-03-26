import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import commonEn from '@/locales/en/common.json';
import authEn from '@/locales/en/auth.json';
import eventsEn from '@/locales/en/events.json';
import bookingsEn from '@/locales/en/bookings.json';
import profileEn from '@/locales/en/profile.json';
import adminEn from '@/locales/en/admin.json';
import notificationsEn from '@/locales/en/notifications.json';
import commonZh from '@/locales/zh-CN/common.json';
import authZh from '@/locales/zh-CN/auth.json';
import eventsZh from '@/locales/zh-CN/events.json';
import bookingsZh from '@/locales/zh-CN/bookings.json';
import profileZh from '@/locales/zh-CN/profile.json';
import adminZh from '@/locales/zh-CN/admin.json';
import notificationsZh from '@/locales/zh-CN/notifications.json';
import { getStoredLanguage, setStoredLanguage } from '@/utils/storage';

void i18n.use(initReactI18next).init({
  lng: getStoredLanguage(),
  fallbackLng: 'en',
  defaultNS: 'common',
  ns: ['common', 'auth', 'events', 'bookings', 'profile', 'admin', 'notifications'],
  interpolation: { escapeValue: false },
  resources: {
    en: {
      common: commonEn,
      auth: authEn,
      events: eventsEn,
      bookings: bookingsEn,
      profile: profileEn,
      admin: adminEn,
      notifications: notificationsEn
    },
    'zh-CN': {
      common: commonZh,
      auth: authZh,
      events: eventsZh,
      bookings: bookingsZh,
      profile: profileZh,
      admin: adminZh,
      notifications: notificationsZh
    }
  }
});

i18n.on('languageChanged', (language) => {
  setStoredLanguage(language);
});

export default i18n;
