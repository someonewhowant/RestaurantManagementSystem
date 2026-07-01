import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Dish } from './menu.service';

export type OrderItemStatus = 'new' | 'cooking' | 'ready' | 'served';

export interface OrderItem {
  id?: string;
  dish: Dish;
  quantity: number;
  status: OrderItemStatus;
}

export interface ActiveOrder {
  id?: string;
  tableId: string;
  status?: string;
  items: OrderItem[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);

  private ordersSignal = signal<Record<string, ActiveOrder>>({});
  public orders = this.ordersSignal.asReadonly();

  getOrderForTable(tableId: string): ActiveOrder {
    const currentOrders = this.ordersSignal();
    if (currentOrders[tableId]) {
      return currentOrders[tableId];
    }
    // Fetch from server
    this.http.get<ActiveOrder>('/api/orders', { params: { tableId } }).subscribe({
      next: (order) => {
        if (order && order.items && order.items.length > 0) {
          this.ordersSignal.update(orders => ({ ...orders, [tableId]: order }));
        }
      },
      error: (err) => console.error('Failed to fetch order', err)
    });
    return { tableId, items: [] };
  }

  addItem(tableId: string, dish: Dish) {
    this.http.post<ActiveOrder>(`/api/orders/table/${tableId}/items`, {
      dishId: dish.id, quantity: 1
    }).subscribe({
      next: (order) => {
        this.ordersSignal.update(orders => ({ ...orders, [tableId]: order }));
      },
      error: (err) => console.error('Failed to add item', err)
    });
  }

  removeItem(tableId: string, dishId: string) {
    const order = this.ordersSignal()[tableId];
    if (!order || !order.id) return;

    // Find the item to remove
    const item = order.items.find(i => i.dish.id === dishId && i.status === 'new');
    if (!item || !item.id) return;

    this.http.delete<ActiveOrder>(`/api/orders/${order.id}/items/${item.id}`).subscribe({
      next: (updated) => {
        if (updated.items.length === 0) {
          this.ordersSignal.update(orders => {
            const newOrders = { ...orders };
            delete newOrders[tableId];
            return newOrders;
          });
        } else {
          this.ordersSignal.update(orders => ({ ...orders, [tableId]: updated }));
        }
      },
      error: (err) => console.error('Failed to remove item', err)
    });
  }

  clearOrder(tableId: string) {
    this.http.delete(`/api/orders/table/${tableId}`).subscribe({
      next: () => {
        this.ordersSignal.update(orders => {
          const newOrders = { ...orders };
          delete newOrders[tableId];
          return newOrders;
        });
      },
      error: (err) => console.error('Failed to clear order', err)
    });
  }

  sendToKitchen(tableId: string) {
    const order = this.ordersSignal()[tableId];
    if (!order || !order.id) return;

    this.http.patch<ActiveOrder>(`/api/orders/${order.id}/send-to-kitchen`, {}).subscribe({
      next: (updated) => {
        this.ordersSignal.update(orders => ({ ...orders, [tableId]: updated }));
      },
      error: (err) => console.error('Failed to send to kitchen', err)
    });
  }

  updateItemStatus(tableId: string, dishId: string, newStatus: OrderItemStatus) {
    const order = this.ordersSignal()[tableId];
    if (!order || !order.id) return;

    const item = order.items.find(i => i.dish.id === dishId);
    if (!item || !item.id) return;

    this.http.patch<ActiveOrder>(`/api/orders/${order.id}/items/${item.id}/status`, {
      status: newStatus
    }).subscribe({
      next: (updated) => {
        this.ordersSignal.update(orders => ({ ...orders, [tableId]: updated }));
      },
      error: (err) => console.error('Failed to update item status', err)
    });
  }

  closeOrder(tableId: string) {
    const order = this.ordersSignal()[tableId];
    if (!order || !order.id) return;

    this.http.post<ActiveOrder>(`/api/orders/${order.id}/close`, {}).subscribe({
      next: () => {
        this.ordersSignal.update(orders => {
          const newOrders = { ...orders };
          delete newOrders[tableId];
          return newOrders;
        });
      },
      error: (err) => console.error('Failed to close order', err)
    });
  }

  getTotal(tableId: string): number {
    const order = this.ordersSignal()[tableId];
    if (!order) return 0;
    return order.items.reduce((sum, item) => sum + (item.dish.price * item.quantity), 0);
  }
}
