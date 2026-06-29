import { Injectable, signal } from '@angular/core';

export interface Employee {
  id: string;
  name: string;
  role: 'Менеджер' | 'Официант' | 'Повар' | 'Кассир';
  status: 'Активен' | 'В отпуске' | 'Уволен';
  hireDate: string;
  fireDate?: string;
  vacationStart?: string;
  vacationEnd?: string;
  onShift?: boolean;
  shiftStartTime?: string;
}

@Injectable({
  providedIn: 'root'
})
export class StaffService {
  private staffSignal = signal<Employee[]>([
    { id: '1', name: 'Александр Иванов', role: 'Менеджер', status: 'Активен', hireDate: '2025-01-15', onShift: true, shiftStartTime: new Date().toISOString() },
    { id: '2', name: 'Мария Смирнова', role: 'Официант', status: 'Активен', hireDate: '2025-03-22', onShift: false },
    { id: '3', name: 'Дмитрий Кузнецов', role: 'Повар', status: 'В отпуске', hireDate: '2024-11-05', vacationStart: '2026-06-01', vacationEnd: '2026-06-30' },
    { id: '4', name: 'Анна Попова', role: 'Кассир', status: 'Активен', hireDate: '2026-02-10', onShift: true, shiftStartTime: new Date(Date.now() - 3600000).toISOString() },
  ]);

  public staff = this.staffSignal.asReadonly();

  addEmployee(employee: Omit<Employee, 'id'>) {
    const newEmployee: Employee = {
      ...employee,
      id: Math.random().toString(36).substr(2, 9)
    };
    this.staffSignal.update(list => [...list, newEmployee]);
  }

  changeStatus(id: string, newStatus: Employee['status']) {
    this.staffSignal.update(list => 
      list.map(emp => emp.id === id ? { ...emp, status: newStatus } : emp)
    );
  }

  updateEmployee(id: string, partial: Partial<Employee>) {
    this.staffSignal.update(list =>
      list.map(emp => emp.id === id ? { ...emp, ...partial } : emp)
    );
  }

  toggleShift(id: string) {
    this.staffSignal.update(list =>
      list.map(emp => {
        if (emp.id === id) {
          const isNowOnShift = !emp.onShift;
          return {
            ...emp,
            onShift: isNowOnShift,
            shiftStartTime: isNowOnShift ? new Date().toISOString() : undefined
          };
        }
        return emp;
      })
    );
  }
}
