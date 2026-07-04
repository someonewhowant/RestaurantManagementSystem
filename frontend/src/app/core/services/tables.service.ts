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

  addTable(table: Partial<Table>) {
    this.http.post<Table>('/api/tables', table).subscribe({
      next: (created) => this.tablesSignal.update(list => [...list, created]),
      error: (err) => console.error('Failed to create table', err)
    });
  }

  updateTable(id: string, updates: Partial<Table>) {
    this.http.put<Table>(`/api/tables/${id}`, updates).subscribe({
      next: (updated) => this.tablesSignal.update(list =>
        list.map(t => t.id === id ? updated : t)
      ),
      error: (err) => console.error('Failed to update table', err)
    });
  }

  deleteTable(id: string) {
    this.http.delete(`/api/tables/${id}`).subscribe({
      next: () => this.tablesSignal.update(list =>
        list.filter(t => t.id !== id)
      ),
      error: (err) => console.error('Failed to delete table', err)
    });
  }
}
