import { useUiStore } from '@/store/ui.store';
import { cn } from '@/utils/cn';

export function ToastCenter() {
  const toasts = useUiStore((state) => state.toasts);
  return (
    <div className="pointer-events-none fixed right-4 top-4 z-[60] flex max-w-sm flex-col gap-3">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={cn(
            'rounded-2xl px-4 py-3 text-sm text-white shadow-soft',
            toast.tone === 'success' && 'bg-emerald-600',
            toast.tone === 'error' && 'bg-rose-600',
            toast.tone === 'info' && 'bg-slate-900'
          )}
        >
          {toast.title}
        </div>
      ))}
    </div>
  );
}
