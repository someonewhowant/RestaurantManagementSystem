import { Component, inject, signal, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { StaffService, Employee } from '../../../core/services/staff.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-admin-staff',
  standalone: true,
  imports: [UiCardComponent, UiBadgeComponent, UiButtonComponent, UiModalComponent, ReactiveFormsModule, FormsModule, DatePipe],
  templateUrl: './staff.component.html',
  styleUrl: './staff.component.scss'
})
export class AdminStaffComponent {
  public staffService = inject(StaffService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);

  public searchQuery = signal<string>('');
  public roleFilter = signal<string>('Все');
  public statusFilter = signal<string>('Все');

  public filteredStaff = computed(() => {
    let list = this.staffService.staff();
    
    const query = this.searchQuery().toLowerCase().trim();
    if (query) {
      list = list.filter(e => e.name.toLowerCase().includes(query));
    }
    
    const role = this.roleFilter();
    if (role !== 'Все') {
      list = list.filter(e => e.role === role);
    }
    
    const status = this.statusFilter();
    if (status !== 'Все') {
      list = list.filter(e => e.status === status);
    }
    
    return list;
  });

  public addForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    phone: [''],
    email: [''],
    role: ['Официант' as Employee['role'], Validators.required],
    status: ['Активен' as Employee['status'], Validators.required],
    hireDate: [new Date().toISOString().split('T')[0], Validators.required],
    fireDate: [''],
    vacationStart: [''],
    vacationEnd: ['']
  });

  public selectedEditEmployee = signal<Employee | null>(null);

  public editForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    phone: [''],
    email: [''],
    role: ['Официант' as Employee['role'], Validators.required],
    status: ['Активен' as Employee['status'], Validators.required],
    hireDate: ['', Validators.required],
    fireDate: [''],
    vacationStart: [''],
    vacationEnd: ['']
  });

  openEditModal(emp: Employee) {
    this.selectedEditEmployee.set(emp);
    this.editForm.reset({
      name: emp.name,
      phone: emp.phone || '',
      email: emp.email || '',
      role: emp.role,
      status: emp.status,
      hireDate: emp.hireDate,
      fireDate: emp.fireDate || '',
      vacationStart: emp.vacationStart || '',
      vacationEnd: emp.vacationEnd || ''
    });
  }

  closeEditModal() {
    this.selectedEditEmployee.set(null);
  }

  submitEdit() {
    const emp = this.selectedEditEmployee();
    if (emp && this.editForm.valid) {
      this.staffService.updateEmployee(emp.id, this.editForm.getRawValue());
      this.closeEditModal();
    }
  }

  toggleAddForm() {
    this.showAddForm.update(v => !v);
  }

  onSubmitAdd() {
    if (this.addForm.valid) {
      this.staffService.addEmployee(this.addForm.getRawValue());
      this.addForm.reset({ role: 'Официант', status: 'Активен', hireDate: new Date().toISOString().split('T')[0] });
      this.showAddForm.set(false);
    }
  }

  fireEmployee(id: string) {
    this.staffService.updateEmployee(id, { 
      status: 'Уволен', 
      fireDate: new Date().toISOString().split('T')[0] 
    });
  }

  activateEmployee(id: string) {
    this.staffService.updateEmployee(id, { 
      status: 'Активен', 
      fireDate: undefined, 
      vacationStart: undefined, 
      vacationEnd: undefined 
    });
  }

  toggleShift(id: string) {
    this.staffService.toggleShift(id);
  }

  getInitials(name: string): string {
    if (!name) return '??';
    const parts = name.split(' ').filter(p => p.length > 0);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    } else if (parts.length === 1) {
      return parts[0].substring(0, 2).toUpperCase();
    }
    return '??';
  }

  getShiftDuration(startTime: string | undefined): string {
    if (!startTime) return '';
    const start = new Date(startTime).getTime();
    const now = new Date().getTime();
    const diffMs = now - start;
    if (diffMs < 0) return 'Только что';
    
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    if (diffHours > 0) {
      return `${diffHours} ч. ${diffMinutes} мин.`;
    }
    return `${diffMinutes} мин.`;
  }
}
