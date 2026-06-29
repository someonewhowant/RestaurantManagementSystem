import { Injectable, signal } from '@angular/core';

export type TableStatus = 'Свободен' | 'Занят' | 'Ожидает блюда' | 'Оплата';

export interface Table {
  id: string;
  number: number;
  capacity: number;
  status: TableStatus;
}

@Injectable({
  providedIn: 'root'
})
export class TablesService {
  private tablesSignal = signal<Table[]>([
    { id: 't1', number: 1, capacity: 2, status: 'Свободен' },
    { id: 't2', number: 2, capacity: 2, status: 'Занят' },
    { id: 't3', number: 3, capacity: 4, status: 'Ожидает блюда' },
    { id: 't4', number: 4, capacity: 4, status: 'Свободен' },
    { id: 't5', number: 5, capacity: 6, status: 'Оплата' },
    { id: 't6', number: 6, capacity: 8, status: 'Свободен' },
    { id: 't7', number: 7, capacity: 2, status: 'Занят' },
    { id: 't8', number: 8, capacity: 4, status: 'Свободен' },
  ]);

  public tables = this.tablesSignal.asReadonly();

  changeStatus(id: string, newStatus: TableStatus) {
    this.tablesSignal.update(list => 
      list.map(t => t.id === id ? { ...t, status: newStatus } : t)
    );
  }
}
