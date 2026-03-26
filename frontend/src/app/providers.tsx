import { PropsWithChildren, useEffect } from 'react';
import { ToastCenter } from '@/components/common/ToastCenter';
import { useLocaleSync } from '@/hooks/useLocaleSync';
import { useAuthStore } from '@/store/auth.store';

export function AppProviders({ children }: PropsWithChildren) {
  useLocaleSync();
  const token = useAuthStore((state) => state.token);
  const fetchMe = useAuthStore((state) => state.fetchMe);

  useEffect(() => {
    if (token) {
      void fetchMe();
    }
  }, [token, fetchMe]);

  return (
    <>
      {children}
      <ToastCenter />
    </>
  );
}
