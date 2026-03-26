export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED' | 'CANCELLED';

export interface EventSummary {
  id: number;
  slug: string;
  title: string;
  summary: string;
  coverImageUrl?: string;
  city: string;
  locationName: string;
  startTime: string;
  endTime: string;
  registrationDeadline: string;
  capacity: number;
  confirmedCount: number;
  remainingSeats: number;
  featured: boolean;
  status: EventStatus;
  categoryCode: string;
  tags: string[];
}

export interface CurrentUserRegistration {
  state: 'CONFIRMED' | 'WAITING' | 'NONE' | 'CANCELLED';
  bookingId?: number | null;
  waitlistEntryId?: number | null;
  waitlistPosition?: number | null;
}

export interface EventDetail extends EventSummary {
  description: string;
  address?: string;
  waitlistCount: number;
  bookable: boolean;
  bookableReason?: string | null;
  currentUserRegistration?: CurrentUserRegistration | null;
}

export interface EventQuery {
  keyword?: string;
  category?: string;
  city?: string;
  startDateFrom?: string;
  startDateTo?: string;
  sort?: string;
  page?: number;
  size?: number;
}

export interface EventUpsertRequest {
  title: string;
  summary: string;
  description: string;
  coverImageUrl?: string;
  locationName: string;
  address?: string;
  city: string;
  startTime: string;
  endTime: string;
  registrationDeadline: string;
  capacity: number;
  featured: boolean;
  categoryCode: string;
  tags: string[];
}
