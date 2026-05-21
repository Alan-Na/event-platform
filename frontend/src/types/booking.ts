export interface BookingActionResponse {
  eventId: number;
  registrationState: 'CONFIRMED' | 'WAITING' | 'CANCELLED' | 'NONE';
  bookingId?: number | null;
  waitlistEntryId?: number | null;
  waitlistPosition?: number | null;
  remainingSeats: number;
  confirmedCount: number;
  waitlistCount: number;
  messageKey: string;
  confirmationCode?: string | null;
}

export interface MyBookingItem {
  eventId: number;
  eventTitle: string;
  coverImageUrl?: string;
  city: string;
  locationName: string;
  startTime: string;
  endTime: string;
  recordType: 'BOOKING' | 'WAITLIST';
  status: string;
  bookedAt?: string | null;
  joinedAt?: string | null;
  cancelledAt?: string | null;
  waitlistPosition?: number | null;
  canCancel: boolean;
  bookingId?: number | null;
  confirmationCode?: string | null;
  ticketTypeName?: string | null;
  checkedInAt?: string | null;
  qrPayload?: string | null;
}
