import { Injectable, signal, computed } from '@angular/core';

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
  // Инициализируем начальное состояние
  private itemsSignal = signal<InventoryItem[]>([
    { id: '1', name: 'Лосось', category: 'Морепродукты', currentStock: 2, minStock: 5, unit: 'кг', pricePerUnit: 1200, expiresInDays: 2 },
    { id: '2', name: 'Говядина', category: 'Мясо', currentStock: 15, minStock: 10, unit: 'кг', pricePerUnit: 850 },
    { id: '3', name: 'Картофель', category: 'Овощи', currentStock: 40, minStock: 20, unit: 'кг', pricePerUnit: 40 },
    { id: '4', name: 'Помидоры', category: 'Овощи', currentStock: 18, minStock: 15, unit: 'кг', pricePerUnit: 150, expiresInDays: 1 },
    { id: '5', name: 'Оливковое масло', category: 'Бакалея', currentStock: 12, minStock: 5, unit: 'л', pricePerUnit: 800 },
    { id: '6', name: 'Соль', category: 'Бакалея', currentStock: 1, minStock: 3, unit: 'кг', pricePerUnit: 30 },
    { id: '7', name: 'Сливки 33%', category: 'Молочка', currentStock: 5, minStock: 3, unit: 'л', pricePerUnit: 300, expiresInDays: 0 },
  ]);

  // Публичный сигнал для чтения
  public items = this.itemsSignal.asReadonly();

  // Вычисляемый сигнал для получения позиций с низким запасом (дефицитом)
  public lowStockItems = computed(() => {
    return this.itemsSignal().filter(item => item.currentStock <= item.minStock);
  });

  // Вычисляемый сигнал для сроков годности (<= 3 дней)
  public expiringItems = computed(() => {
    return this.itemsSignal()
      .filter(item => item.expiresInDays !== undefined && item.expiresInDays <= 3)
      .sort((a, b) => (a.expiresInDays || 0) - (b.expiresInDays || 0));
  });

  // Добавление новой позиции
  addItem(item: Omit<InventoryItem, 'id'>) {
    const newItem: InventoryItem = {
      ...item,
      id: Math.random().toString(36).substr(2, 9)
    };
    this.itemsSignal.update(items => [...items, newItem]);
  }

  // Добавление на склад (Restock)
  restock(id: string, amount: number) {
    this.itemsSignal.update(items =>
      items.map(item =>
        item.id === id
          ? { ...item, currentStock: item.currentStock + amount }
          : item
      )
    );
  }

  // Списание со склада (при приготовлении)
  consume(id: string, amount: number) {
    this.itemsSignal.update(items =>
      items.map(item =>
        item.id === id
          ? { ...item, currentStock: Math.max(0, item.currentStock - amount) }
          : item
      )
    );
  }

  // Обновление параметров ингредиента (название, категория, минимум)
  updateItem(id: string, partial: Partial<InventoryItem>) {
    this.itemsSignal.update(items =>
      items.map(item =>
        item.id === id
          ? { ...item, ...partial }
          : item
      )
    );
  }

  // Массовое списание по рецептам
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

    this.itemsSignal.update(items =>
      items.map(item => {
        const consumed = consumptionMap.get(item.id);
        if (consumed) {
          return { ...item, currentStock: Math.max(0, parseFloat((item.currentStock - consumed).toFixed(3))) };
        }
        return item;
      })
    );
  }
}
