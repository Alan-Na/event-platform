import { PropsWithChildren } from 'react';
import { Button } from '@/components/common/Button';

interface ErrorStateProps extends PropsWithChildren {
  onRetry?: () => void;
}

export function ErrorState({ children, onRetry }: ErrorStateProps) {
  return (
    <div className="card flex min-h-40 flex-col items-center justify-center gap-4 p-8 text-center text-sm text-rose-600">
      <div>{children}</div>
      {onRetry ? <Button onClick={onRetry}>Retry</Button> : null}
    </div>
  );
}
