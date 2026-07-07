import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../ui/toast/toast.service';

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
  private toastService = inject(ToastService);

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
      next: (updated) => {
        this.tablesSignal.update(list => list.map(t => t.id === id ? updated : t));
        this.toastService.info(`Статус стола #${updated.number} обновлен на "${newStatus}"`);
      },
      error: (err) => {
        console.error('Failed to change table status', err);
        this.toastService.error('Ошибка при обновлении статуса стола');
      }
    });
  }

  assignWaiter(id: string, waiterId: string) {
    this.http.patch<Table>(`/api/tables/${id}/waiter`, { waiterId }).subscribe({
      next: (updated) => {
        this.tablesSignal.update(list => list.map(t => t.id === id ? updated : t));
        this.toastService.success(`Официант назначен на стол #${updated.number}`);
      },
      error: (err) => {
        console.error('Failed to assign waiter', err);
        this.toastService.error('Ошибка при назначении официанта');
      }
    });
  }

  addTable(table: Partial<Table>) {
    this.http.post<Table>('/api/tables', table).subscribe({
      next: (created) => {
        this.tablesSignal.update(list => [...list, created]);
        this.toastService.success(`Стол #${created.number} успешно добавлен`);
      },
      error: (err) => {
        console.error('Failed to create table', err);
        this.toastService.error('Ошибка при добавлении стола');
      }
    });
  }

  updateTable(id: string, updates: Partial<Table>) {
    this.http.put<Table>(`/api/tables/${id}`, updates).subscribe({
      next: (updated) => {
        this.tablesSignal.update(list => list.map(t => t.id === id ? updated : t));
        this.toastService.success(`Настройки стола #${updated.number} сохранены`);
      },
      error: (err) => {
        console.error('Failed to update table', err);
        this.toastService.error('Ошибка при сохранении настроек стола');
      }
    });
  }

  deleteTable(id: string) {
    this.http.delete(`/api/tables/${id}`).subscribe({
      next: () => {
        this.tablesSignal.update(list => list.filter(t => t.id !== id));
        this.toastService.info('Стол удален');
      },
      error: (err) => {
        console.error('Failed to delete table', err);
        this.toastService.error('Ошибка при удалении стола');
      }
    });
  }
}
