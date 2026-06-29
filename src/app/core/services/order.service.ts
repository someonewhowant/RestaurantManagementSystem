import { Injectable, signal } from '@angular/core';
import { Dish } from './menu.service';

export type OrderItemStatus = 'new' | 'cooking' | 'ready' | 'served';

export interface OrderItem {
  dish: Dish;
  quantity: number;
  status: OrderItemStatus;
}

export interface ActiveOrder {
  tableId: string;
  items: OrderItem[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private ordersSignal = signal<Record<string, ActiveOrder>>({});

  public orders = this.ordersSignal.asReadonly();

  getOrderForTable(tableId: string): ActiveOrder {
    const currentOrders = this.ordersSignal();
    if (!currentOrders[tableId]) {
      return { tableId, items: [] };
    }
    return currentOrders[tableId];
  }

  addItem(tableId: string, dish: Dish) {
    this.ordersSignal.update(orders => {
      const order = orders[tableId] || { tableId, items: [] };
      const existingItemIndex = order.items.findIndex(i => i.dish.id === dish.id && i.status === 'new');
      
      let newItems = [...order.items];
      if (existingItemIndex >= 0) {
        newItems[existingItemIndex] = {
          ...newItems[existingItemIndex],
          quantity: newItems[existingItemIndex].quantity + 1
        };
      } else {
        newItems.push({ dish, quantity: 1, status: 'new' });
      }

      return {
        ...orders,
        [tableId]: { ...order, items: newItems }
      };
    });
  }

  removeItem(tableId: string, dishId: string) {
    this.ordersSignal.update(orders => {
      const order = orders[tableId];
      if (!order) return orders;

      const existingItemIndex = order.items.findIndex(i => i.dish.id === dishId && i.status === 'new');
      if (existingItemIndex >= 0) {
        let newItems = [...order.items];
        if (newItems[existingItemIndex].quantity > 1) {
           newItems[existingItemIndex] = {
            ...newItems[existingItemIndex],
            quantity: newItems[existingItemIndex].quantity - 1
          };
        } else {
          newItems.splice(existingItemIndex, 1);
        }
        return { ...orders, [tableId]: { ...order, items: newItems } };
      }
      return orders;
    });
  }

  clearOrder(tableId: string) {
    this.ordersSignal.update(orders => {
      const newOrders = { ...orders };
      delete newOrders[tableId];
      return newOrders;
    });
  }

  sendToKitchen(tableId: string) {
    this.ordersSignal.update(orders => {
      const order = orders[tableId];
      if (!order) return orders;

      const newItems = order.items.map(item => {
        if (item.status === 'new') return { ...item, status: 'cooking' as OrderItemStatus };
        return item;
      });

      return { ...orders, [tableId]: { ...order, items: newItems } };
    });
  }

  updateItemStatus(tableId: string, dishId: string, newStatus: OrderItemStatus) {
    this.ordersSignal.update(orders => {
      const order = orders[tableId];
      if (!order) return orders;

      const newItems = order.items.map(item => {
        if (item.dish.id === dishId) return { ...item, status: newStatus };
        return item;
      });

      return { ...orders, [tableId]: { ...order, items: newItems } };
    });
  }

  getTotal(tableId: string): number {
    const order = this.ordersSignal()[tableId];
    if (!order) return 0;
    return order.items.reduce((sum, item) => sum + (item.dish.price * item.quantity), 0);
  }
}
