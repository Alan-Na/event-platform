import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { apiClient } from '@/api/axios';
import type { ApiResponse } from '@/types/common';
import type { EventDetail } from '@/types/event';
import { getAdminEventBookingsApi, getAdminEventWaitlistApi, type AdminBookingItem, type AdminWaitlistItem } from '@/api/admin.api';
import { BookingTable } from '@/components/admin/BookingTable';

export function AdminRegistrationsPage() {
  const { id } = useParams();
  const [event, setEvent] = useState<EventDetail | null>(null);
  const [bookings, setBookings] = useState<AdminBookingItem[]>([]);
  const [waitlist, setWaitlist] = useState<AdminWaitlistItem[]>([]);

  useEffect(() => {
    if (!id) return;
    void apiClient.get<ApiResponse<EventDetail>>(`/admin/events/${id}`).then((response) => setEvent(response.data.data));
    void getAdminEventBookingsApi(id, 0, 50).then((result) => setBookings(result.items));
    void getAdminEventWaitlistApi(id, 0, 50).then((result) => setWaitlist(result.items));
  }, [id]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="section-title">{event?.title || 'Event registrations'}</h1>
        <p className="mt-2 text-slate-500">Review confirmed attendees and waitlist order.</p>
      </div>
      <BookingTable title="Confirmed bookings" items={bookings} variant="bookings" />
      <BookingTable title="Waitlist" items={waitlist} variant="waitlist" />
    </div>
  );
}
