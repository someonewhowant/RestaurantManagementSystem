import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { StaffService, Employee } from '../../../core/services/staff.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-admin-staff',
  standalone: true,
  imports: [UiCardComponent, UiBadgeComponent, UiButtonComponent, ReactiveFormsModule, DatePipe],
  templateUrl: './staff.component.html',
  styleUrl: './staff.component.scss'
})
export class AdminStaffComponent {
  public staffService = inject(StaffService);
  private fb = inject(FormBuilder);

  public showAddForm = signal(false);

  public addForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    role: ['Официант' as Employee['role'], Validators.required],
    status: ['Активен' as Employee['status'], Validators.required],
    hireDate: [new Date().toISOString().split('T')[0], Validators.required]
  });

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
    this.staffService.changeStatus(id, 'Уволен');
  }

  activateEmployee(id: string) {
    this.staffService.changeStatus(id, 'Активен');
  }
}
