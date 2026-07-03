import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

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
  private http = inject(HttpClient);

  private staffSignal = signal<Employee[]>([]);
  public staff = this.staffSignal.asReadonly();

  constructor() {
    this.fetchStaff();
  }

  fetchStaff() {
    this.http.get<Employee[]>('/api/staff').subscribe({
      next: (staff) => this.staffSignal.set(staff),
      error: (err) => console.error('Failed to fetch staff', err)
    });
  }

  addEmployee(employee: Omit<Employee, 'id'>) {
    this.http.post<Employee>('/api/staff', employee).subscribe({
      next: (created) => this.staffSignal.update(list => [...list, created]),
      error: (err) => console.error('Failed to create employee', err)
    });
  }

  changeStatus(id: string, newStatus: Employee['status']) {
    this.http.patch<Employee>(`/api/staff/${id}/status`, { status: newStatus }).subscribe({
      next: (updated) => this.staffSignal.update(list =>
        list.map(emp => emp.id === id ? updated : emp)
      ),
      error: (err) => console.error('Failed to change status', err)
    });
  }

  updateEmployee(id: string, partial: Partial<Employee>) {
    this.http.put<Employee>(`/api/staff/${id}`, partial).subscribe({
      next: (updated) => this.staffSignal.update(list =>
        list.map(emp => emp.id === id ? updated : emp)
      ),
      error: (err) => console.error('Failed to update employee', err)
    });
  }

  toggleShift(id: string) {
    this.http.patch<Employee>(`/api/staff/${id}/shift`, {}).subscribe({
      next: (updated) => this.staffSignal.update(list =>
        list.map(emp => emp.id === id ? updated : emp)
      ),
      error: (err) => console.error('Failed to toggle shift', err)
    });
  }

  exportCsv() {
    this.http.get('/api/staff/export/csv', { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'staff_report.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Failed to export CSV', err)
    });
  }
}
