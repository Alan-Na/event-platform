import type { AdminUserOverview } from '@/api/admin.api';

export function UserTable({ items }: { items: AdminUserOverview[] }) {
  return (
    <div className="card overflow-x-auto">
      <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
        <thead className="bg-slate-50 text-slate-500">
          <tr>
            <th className="px-4 py-3">User</th>
            <th className="px-4 py-3">Roles</th>
            <th className="px-4 py-3">Confirmed</th>
            <th className="px-4 py-3">Waitlist</th>
            <th className="px-4 py-3">Cancelled</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {items.map((item) => (
            <tr key={item.userId}>
              <td className="px-4 py-4">
                <div className="font-medium text-slate-900">{item.fullName}</div>
                <div className="text-xs text-slate-500">{item.email}</div>
              </td>
              <td className="px-4 py-4 text-slate-600">{item.roles.join(', ')}</td>
              <td className="px-4 py-4 text-slate-600">{item.confirmedBookings}</td>
              <td className="px-4 py-4 text-slate-600">{item.waitingEntries}</td>
              <td className="px-4 py-4 text-slate-600">{item.cancelledRecords}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
