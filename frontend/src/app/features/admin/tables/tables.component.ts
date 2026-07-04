import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TablesService, Table } from '../../../core/services/tables.service';
import { UiButtonComponent } from '../../../core/ui/button/button.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';

@Component({
  selector: 'app-admin-tables',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, UiButtonComponent, UiModalComponent],
  templateUrl: './tables.component.html',
  styleUrl: './tables.component.scss'
})
export class AdminTablesComponent {
  public tablesService = inject(TablesService);
  private fb = inject(FormBuilder);

  public selectedEditTable = signal<Table | null>(null);
  public isAddingTable = signal<boolean>(false);

  public addForm = this.fb.nonNullable.group({
    number: [0, [Validators.required, Validators.min(1)]],
    capacity: [2, [Validators.required, Validators.min(1)]]
  });

  public editForm = this.fb.nonNullable.group({
    number: [0, [Validators.required, Validators.min(1)]],
    capacity: [2, [Validators.required, Validators.min(1)]]
  });

  openAddModal() {
    this.addForm.reset({ number: 1, capacity: 2 });
    this.isAddingTable.set(true);
  }

  closeAddModal() {
    this.isAddingTable.set(false);
  }

  submitAdd() {
    if (this.addForm.valid) {
      this.tablesService.addTable(this.addForm.getRawValue());
      this.closeAddModal();
    }
  }

  openEditModal(table: Table) {
    this.selectedEditTable.set(table);
    this.editForm.reset({
      number: table.number,
      capacity: table.capacity
    });
  }

  closeEditModal() {
    this.selectedEditTable.set(null);
  }

  submitEdit() {
    if (this.editForm.valid) {
      const table = this.selectedEditTable();
      if (table) {
        this.tablesService.updateTable(table.id, this.editForm.getRawValue());
        this.closeEditModal();
      }
    }
  }

  deleteTable(id: string) {
    if (confirm('Вы уверены, что хотите удалить этот стол?')) {
      this.tablesService.deleteTable(id);
    }
  }
}
