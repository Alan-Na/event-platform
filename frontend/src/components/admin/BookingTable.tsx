import type { AdminBookingItem, AdminWaitlistItem } from '@/api/admin.api';

interface BookingTableProps {
  title: string;
  items: Array<AdminBookingItem | AdminWaitlistItem>;
  variant: 'bookings' | 'waitlist';
}

export function BookingTable({ title, items, variant }: BookingTableProps) {
  return (
    <div className="card overflow-x-auto p-4">
      <h2 className="mb-4 text-lg font-semibold text-slate-900">{title}</h2>
      <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead className="bg-slate-50 text-slate-500">
          <tr>
            <th className="px-4 py-3">Name</th>
            <th className="px-4 py-3">Email</th>
            <th className="px-4 py-3">Status</th>
            <th className="px-4 py-3">{variant === 'bookings' ? 'Booked At' : 'Position'}</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={variant === 'bookings' ? (item as AdminBookingItem).bookingId : (item as AdminWaitlistItem).waitlistEntryId}>
              <td className="px-4 py-4 text-slate-900">{item.userName}</td>
              <td className="px-4 py-4 text-slate-600">{item.userEmail}</td>
              <td className="px-4 py-4 text-slate-600">{item.status}</td>
              <td className="px-4 py-4 text-slate-600">
                {variant === 'bookings' ? (item as AdminBookingItem).bookedAt : (item as AdminWaitlistItem).position}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
