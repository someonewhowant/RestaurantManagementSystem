import { Injectable, signal, computed } from '@angular/core';

export interface Transaction {
  id: string;
  date: string;
  amount: number;
  type: 'Доход' | 'Расход';
  category: string;
  description: string;
  items?: { dishId: string, quantity: number }[];
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private transactionsSignal = signal<Transaction[]>([
    { 
      id: '1', date: new Date(Date.now() - 86400000 * 2).toISOString(), amount: 1500, type: 'Доход', category: 'Оплата заказа', description: 'Выручка за смену',
      items: [{ dishId: 'm1', quantity: 2 }, { dishId: 'm4', quantity: 1 }]
    },
    { id: '2', date: new Date(Date.now() - 86400000 * 1).toISOString(), amount: 300, type: 'Расход', category: 'Закупки', description: 'Закупка овощей' },
    { 
      id: '3', date: new Date().toISOString(), amount: 2100, type: 'Доход', category: 'Оплата заказа', description: 'Выручка за смену',
      items: [{ dishId: 'm1', quantity: 1 }, { dishId: 'm2', quantity: 3 }, { dishId: 'm6', quantity: 4 }]
    },
    { id: '4', date: new Date().toISOString(), amount: 500, type: 'Расход', category: 'Коммуналка', description: 'Оплата электричества' }
  ]);

  public transactions = this.transactionsSignal.asReadonly();

  public totalIncome = computed(() => 
    this.transactionsSignal()
      .filter(t => t.type === 'Доход')
      .reduce((sum, t) => sum + t.amount, 0)
  );

  public totalExpense = computed(() => 
    this.transactionsSignal()
      .filter(t => t.type === 'Расход')
      .reduce((sum, t) => sum + t.amount, 0)
  );

  public totalBalance = computed(() => this.totalIncome() - this.totalExpense());

  addTransaction(transaction: Omit<Transaction, 'id'>) {
    const newTx: Transaction = {
      ...transaction,
      id: Math.random().toString(36).substr(2, 9)
    };
    this.transactionsSignal.update(list => [newTx, ...list]); // Добавляем в начало
  }
}
