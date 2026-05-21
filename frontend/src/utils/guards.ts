import type { CurrentUser } from '@/types/auth';

export function isAdmin(user?: CurrentUser | null) {
  return !!user?.roles?.includes('ROLE_ADMIN');
}

export function isOrganizer(user?: CurrentUser | null) {
  return !!user?.roles?.includes('ROLE_ORGANIZER') || isAdmin(user);
}
