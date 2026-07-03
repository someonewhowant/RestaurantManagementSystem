import { Injectable, signal, computed } from '@angular/core';

export interface AppNotification {
  id: string;
  type: 'inventory' | 'staff' | 'kitchen' | 'system';
  title: string;
  message: string;
  time: Date;
  read: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSignal = signal<AppNotification[]>([
    {
      id: '1',
      type: 'inventory',
      title: 'Низкий запас',
      message: 'Заканчивается "Молоко" (осталось 2 л)',
      time: new Date(Date.now() - 1000 * 60 * 5),
      read: false
    },
    {
      id: '2',
      type: 'kitchen',
      title: 'Задержка на кухне',
      message: 'Заказ Стол №4 ожидает более 20 минут',
      time: new Date(Date.now() - 1000 * 60 * 15),
      read: false
    },
    {
      id: '3',
      type: 'staff',
      title: 'Смена',
      message: 'Смена официанта Ивана длится более 12 часов',
      time: new Date(Date.now() - 1000 * 60 * 60 * 2),
      read: true
    }
  ]);

  public notifications = this.notificationsSignal.asReadonly();
  public unreadCount = computed(() => this.notificationsSignal().filter(n => !n.read).length);

  markAsRead(id: string) {
    this.notificationsSignal.update(list => 
      list.map(n => n.id === id ? { ...n, read: true } : n)
    );
  }

  markAllAsRead() {
    this.notificationsSignal.update(list => 
      list.map(n => ({ ...n, read: true }))
    );
  }

  addNotification(notification: Omit<AppNotification, 'id' | 'time' | 'read'>) {
    const newNotification: AppNotification = {
      ...notification,
      id: Math.random().toString(36).substr(2, 9),
      time: new Date(),
      read: false
    };
    this.notificationsSignal.update(list => [newNotification, ...list]);
  }
}
