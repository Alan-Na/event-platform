export interface TicketTypeSummary {
  id: number;
  name: string;
  description?: string | null;
  priceAmount: number;
  currency: string;
  capacity: number;
  confirmedCount: number;
  remainingSeats: number;
  salesStartAt?: string | null;
  salesEndAt?: string | null;
  active: boolean;
}
