import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Transaction {
  id: string;
  date: string;
  amount: number;
  type: 'Доход' | 'Расход';
  category: string;
  description: string;
  items?: { dishId: string, quantity: number }[];
}

export interface BudgetSummary {
  totalIncome: number;
  totalExpense: number;
  balance: number;
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private http = inject(HttpClient);

  private transactionsSignal = signal<Transaction[]>([]);
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

  constructor() {
    this.fetchTransactions();
  }

  fetchTransactions() {
    this.http.get<Transaction[]>('/api/budget/transactions').subscribe({
      next: (txs) => this.transactionsSignal.set(txs),
      error: (err) => console.error('Failed to fetch transactions', err)
    });
  }

  addTransaction(transaction: Omit<Transaction, 'id'>) {
    this.http.post<Transaction>('/api/budget/transactions', transaction).subscribe({
      next: (created) => this.transactionsSignal.update(list => [created, ...list]),
      error: (err) => console.error('Failed to create transaction', err)
    });
  }
}
