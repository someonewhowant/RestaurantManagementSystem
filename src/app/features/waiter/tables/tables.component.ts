import { Component, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TablesService, Table, TableStatus } from '../../../core/services/tables.service';
import { StaffService } from '../../../core/services/staff.service';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-waiter-tables',
  standalone: true,
  imports: [CommonModule, FormsModule, UiBadgeComponent, UiModalComponent, UiButtonComponent],
  templateUrl: './tables.component.html',
  styleUrl: './tables.component.scss'
})
export class WaiterTablesComponent {
  public tablesService = inject(TablesService);
  public staffService = inject(StaffService);
  private router = inject(Router);
  
  public selectedTable = signal<Table | null>(null);

  // Compute only waiters who are on shift
  public activeWaiters = computed(() => {
    return this.staffService.staff().filter(e => 
      (e.role === 'Официант' || e.role === 'Менеджер') && e.onShift
    );
  });

  public currentWaiterId = signal<string>('');

  constructor() {
    // Attempt to set a default waiter if available
    const initialWaiters = this.activeWaiters();
    if (initialWaiters.length > 0) {
      this.currentWaiterId.set(initialWaiters[0].id);
    }
  }

  onTableClick(table: Table) {
    // Открываем модальное окно выбранного стола
    this.selectedTable.set(table);
  }

  closeModal() {
    this.selectedTable.set(null);
  }

  changeTableStatus(newStatus: TableStatus) {
    const table = this.selectedTable();
    if (table) {
      this.tablesService.changeStatus(table.id, newStatus, this.currentWaiterId() || undefined);
      // Обновляем локальный стейт модалки
      this.selectedTable.set({ ...table, status: newStatus, waiterId: this.currentWaiterId() || table.waiterId });
    }
  }

  goToOrder() {
    const table = this.selectedTable();
    if (table) {
      if (table.status === 'Свободен') {
        this.changeTableStatus('Занят');
      }
      this.router.navigate(['/waiter/terminal'], { queryParams: { tableId: table.id } });
      this.closeModal();
    }
  }

  getWaiterName(id?: string): string {
    if (!id) return '';
    const emp = this.staffService.staff().find(e => e.id === id);
    return emp ? emp.name : '';
  }

  getStatusColor(status: TableStatus): 'success' | 'danger' | 'warning' | 'primary' {
    switch (status) {
      case 'Свободен': return 'success';
      case 'Занят': return 'danger';
      case 'Ожидает блюда': return 'warning';
      case 'Оплата': return 'primary';
      default: return 'primary';
    }
  }
}
