import type { CurrentUser } from '@/types/auth';

export function isAdmin(user?: CurrentUser | null) {
  return !!user?.roles?.includes('ROLE_ADMIN');
}
