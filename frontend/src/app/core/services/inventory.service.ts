import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import { ToastService } from '../ui/toast/toast.service';

export interface InventoryItem {
  id: string;
  name: string;
  category: string;
  currentStock: number;
  minStock: number;
  unit: string;
  pricePerUnit?: number;
  expiresInDays?: number;
}

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private toastService = inject(ToastService);

  private itemsSignal = signal<InventoryItem[]>([]);
  public items = this.itemsSignal.asReadonly();

  // Computed signals can still work perfectly on top of our loaded state
  public lowStockItems = computed(() => {
    return this.itemsSignal().filter(item => item.currentStock <= item.minStock);
  });

  public expiringItems = computed(() => {
    return this.itemsSignal()
      .filter(item => item.expiresInDays !== undefined && item.expiresInDays <= 3)
      .sort((a, b) => (a.expiresInDays || 0) - (b.expiresInDays || 0));
  });

  constructor() {
    this.fetchItems();
  }

  fetchItems() {
    this.http.get<InventoryItem[]>('/api/inventory').subscribe({
      next: (items) => this.itemsSignal.set(items),
      error: (err) => console.error('Failed to fetch inventory', err)
    });
  }

  addItem(item: Omit<InventoryItem, 'id'>) {
    this.http.post<InventoryItem>('/api/inventory', item).subscribe({
      next: (created) => {
        this.itemsSignal.update(items => [...items, created]);
        this.toastService.success(`Ингредиент "${created.name}" добавлен`);
      },
      error: (err) => {
        console.error('Failed to create inventory item', err);
        this.toastService.error('Ошибка при добавлении ингредиента');
      }
    });
  }

  updateItem(id: string, partial: Partial<InventoryItem>) {
    this.http.put<InventoryItem>(`/api/inventory/${id}`, partial).subscribe({
      next: (updated) => {
        this.itemsSignal.update(items => items.map(item => item.id === id ? updated : item));
        this.toastService.success(`Ингредиент "${updated.name}" обновлен`);
      },
      error: (err) => {
        console.error('Failed to update inventory item', err);
        this.toastService.error('Ошибка при обновлении ингредиента');
      }
    });
  }

  restock(id: string, amount: number) {
    this.http.patch<InventoryItem>(`/api/inventory/${id}/restock`, { amount }).subscribe({
      next: (updated) => {
        this.itemsSignal.update(items => items.map(item => item.id === id ? updated : item));
        this.toastService.success(`Поступление для "${updated.name}" зафиксировано`);
      },
      error: (err) => {
        console.error('Failed to restock item', err);
        this.toastService.error('Ошибка при пополнении запаса');
      }
    });
  }

  consume(id: string, amount: number) {
    this.http.patch<InventoryItem>(`/api/inventory/${id}/consume`, { amount }).subscribe({
      next: (updated) => {
        this.itemsSignal.update(items => items.map(item => item.id === id ? updated : item));
        this.toastService.info(`Списание для "${updated.name}" зафиксировано`);
      },
      error: (err) => {
        console.error('Failed to consume item', err);
        this.toastService.error('Ошибка при списании запаса');
      }
    });
  }

  // Массовое списание по рецептам (вызывается при закрытии заказа)
  consumeForOrderItems(orderItems: { dish: any, quantity: number }[]) {
    const consumptionMap = new Map<string, number>();

    for (const item of orderItems) {
      if (item.dish.recipe) {
        for (const ingredient of item.dish.recipe) {
          const totalAmount = ingredient.amount * item.quantity;
          const current = consumptionMap.get(ingredient.ingredientId) || 0;
          consumptionMap.set(ingredient.ingredientId, current + totalAmount);
        }
      }
    }

    const payload = Array.from(consumptionMap.entries()).map(([ingredientId, amount]) => ({
      ingredientId,
      amount
    }));

    if (payload.length > 0) {
      this.http.post('/api/inventory/consume-batch', { items: payload }).subscribe({
        next: () => {
          this.fetchItems();
        },
        error: (err) => {
          console.error('Failed to consume batch', err);
          this.toastService.error('Ошибка при пакетном списании запасов');
        }
      });
    }
  }

  exportCsv() {
    this.http.get('/api/inventory/export/csv', { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'inventory_report.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.toastService.success('Отчет по инвентаризации скачан');
      },
      error: (err) => {
        console.error('Failed to export CSV', err);
        this.toastService.error('Ошибка при скачивании отчета');
      }
    });
  }
}
