import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

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
  expenseByCategory?: { [key: string]: number };
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private http = inject(HttpClient);

  private transactionsSignal = signal<Transaction[]>([]);
  public transactions = this.transactionsSignal.asReadonly();
  
  public hasMore = signal<boolean>(true);
  public currentPage = signal<number>(0);

  public dateRange = signal<{start: string | null, end: string | null}>({start: null, end: null});

  private summarySignal = signal<BudgetSummary>({ totalIncome: 0, totalExpense: 0, balance: 0 });
  public totalIncome = computed(() => this.summarySignal().totalIncome);
  public totalExpense = computed(() => this.summarySignal().totalExpense);
  public totalBalance = computed(() => this.summarySignal().balance);
  public expenseByCategory = computed(() => this.summarySignal().expenseByCategory || {});

  constructor() {
    this.fetchTransactions(0);
    this.fetchSummary();
  }

  setDateRange(start: string | null, end: string | null) {
    this.dateRange.set({start, end});
    this.fetchSummary();
    this.fetchTransactions(0);
  }

  fetchSummary() {
    let params = new HttpParams();
    const range = this.dateRange();
    if (range.start) params = params.set('startDate', range.start);
    if (range.end) params = params.set('endDate', range.end);

    this.http.get<BudgetSummary>('/api/budget/summary', { params }).subscribe({
      next: (summary) => this.summarySignal.set(summary),
      error: (err) => console.error('Failed to fetch summary', err)
    });
  }

  fetchTransactions(page: number = 0, size: number = 20) {
    let params = new HttpParams().set('page', page).set('size', size);
    const range = this.dateRange();
    if (range.start) params = params.set('startDate', range.start);
    if (range.end) params = params.set('endDate', range.end);
    
    this.http.get<Page<Transaction>>('/api/budget/transactions', { params }).subscribe({
      next: (pageData) => {
        if (page === 0) {
          this.transactionsSignal.set(pageData.content);
        } else {
          this.transactionsSignal.update(list => [...list, ...pageData.content]);
        }
        this.currentPage.set(pageData.number);
        this.hasMore.set(!pageData.last);
      },
      error: (err) => console.error('Failed to fetch transactions', err)
    });
  }

  loadMore() {
    if (this.hasMore()) {
      this.fetchTransactions(this.currentPage() + 1);
    }
  }

  addTransaction(transaction: Omit<Transaction, 'id'>) {
    this.http.post<Transaction>('/api/budget/transactions', transaction).subscribe({
      next: (created) => {
        this.transactionsSignal.update(list => [created, ...list]);
        this.fetchSummary(); // Update summary after a new transaction
      },
      error: (err) => console.error('Failed to create transaction', err)
    });
  }

  refundTransaction(transactionId: string) {
    this.http.post<Transaction>(`/api/budget/transactions/${transactionId}/refund`, {}).subscribe({
      next: (refundTx) => {
        this.transactionsSignal.update(list => [refundTx, ...list]);
        this.fetchSummary();
      },
      error: (err) => console.error('Failed to refund transaction', err)
    });
  }

  exportCsv() {
    let params = new HttpParams();
    const range = this.dateRange();
    if (range.start) params = params.set('startDate', range.start);
    if (range.end) params = params.set('endDate', range.end);
    
    this.http.get('/api/budget/export/csv', { params, responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'budget_report.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Failed to export CSV', err)
    });
  }
}
