import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export type TableStatus = 'Свободен' | 'Занят' | 'Ожидает блюда' | 'Оплата';

export interface Table {
  id: string;
  number: number;
  capacity: number;
  status: TableStatus;
  waiterId?: string;
  statusUpdatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TablesService {
  private http = inject(HttpClient);

  private tablesSignal = signal<Table[]>([]);
  public tables = this.tablesSignal.asReadonly();

  constructor() {
    this.fetchTables();
  }

  fetchTables() {
    this.http.get<Table[]>('/api/tables').subscribe({
      next: (tables) => this.tablesSignal.set(tables),
      error: (err) => console.error('Failed to fetch tables', err)
    });
  }

  changeStatus(id: string, newStatus: TableStatus, waiterId?: string) {
    this.http.patch<Table>(`/api/tables/${id}/status`, { status: newStatus, waiterId }).subscribe({
      next: (updated) => this.tablesSignal.update(list =>
        list.map(t => t.id === id ? updated : t)
      ),
      error: (err) => console.error('Failed to change table status', err)
    });
  }

  assignWaiter(id: string, waiterId: string) {
    this.http.patch<Table>(`/api/tables/${id}/waiter`, { waiterId }).subscribe({
      next: (updated) => this.tablesSignal.update(list =>
        list.map(t => t.id === id ? updated : t)
      ),
      error: (err) => console.error('Failed to assign waiter', err)
    });
  }
}
