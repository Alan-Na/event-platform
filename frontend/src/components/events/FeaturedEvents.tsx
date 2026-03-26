import type { EventSummary } from '@/types/event';
import { EventCard } from '@/components/events/EventCard';

export function FeaturedEvents({ items }: { items: EventSummary[] }) {
  return (
    <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      {items.map((item) => (
        <EventCard key={item.id} event={item} />
      ))}
    </div>
  );
}
