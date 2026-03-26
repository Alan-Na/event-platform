import { PropsWithChildren } from 'react';
import { cn } from '@/utils/cn';

export function Badge({ children, className }: PropsWithChildren<{ className?: string }>) {
  return <span className={cn('inline-flex rounded-full border px-2.5 py-1 text-xs font-medium', className)}>{children}</span>;
}
