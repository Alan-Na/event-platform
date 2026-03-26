export interface NotificationItem {
  id: number;
  type: string;
  templateKey: string;
  payloadJson: string;
  isRead: boolean;
  createdAt: string;
}
