import { PropsWithChildren } from 'react';

export function EmptyState({ children }: PropsWithChildren) {
  return <div className="card flex min-h-40 items-center justify-center p-8 text-center text-sm text-slate-500">{children}</div>;
}
