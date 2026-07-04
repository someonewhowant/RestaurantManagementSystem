import { Component, inject, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TablesService, Table, TableStatus } from '../../../core/services/tables.service';
import { StaffService } from '../../../core/services/staff.service';
import { AuthService } from '../../../core/services/auth.service';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-waiter-tables',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, UiBadgeComponent, UiModalComponent, UiButtonComponent],
  templateUrl: './tables.component.html',
  styleUrl: './tables.component.scss'
})
export class WaiterTablesComponent {
  public tablesService = inject(TablesService);
  public staffService = inject(StaffService);
  public authService = inject(AuthService);
  private router = inject(Router);
  
  public selectedTable = signal<Table | null>(null);

  // Compute only waiters who are on shift
  public activeWaiters = computed(() => {
    return this.staffService.staff().filter(e => 
      e.role === 'Официант' && e.onShift
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
      // Если у столика уже есть официант, сохраняем его, иначе назначаем текущего
      const wId = table.waiterId || this.currentWaiterId() || undefined;
      this.tablesService.changeStatus(table.id, newStatus, wId);
      // Обновляем локальный стейт модалки
      this.selectedTable.set({ ...table, status: newStatus, waiterId: newStatus === 'Свободен' ? undefined : wId });
    }
  }

  assignWaiter(waiterId: string) {
    const table = this.selectedTable();
    if (table) {
      this.tablesService.assignWaiter(table.id, waiterId);
      this.selectedTable.set({ ...table, waiterId });
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

  getDuration(table: Table): string {
    if (table.status === 'Свободен' || !table.statusUpdatedAt) return '';
    const minutes = this.getDurationMinutes(table);
    if (minutes >= 60) {
      const h = Math.floor(minutes / 60);
      const m = minutes % 60;
      return `${h} ч ${m} мин`;
    }
    return `${minutes} мин`;
  }

  getDurationMinutes(table: Table): number {
    if (table.status === 'Свободен' || !table.statusUpdatedAt) return 0;
    const date = new Date(table.statusUpdatedAt);
    const diff = Date.now() - date.getTime();
    return Math.floor(diff / 60000);
  }

  getSeatsArray(capacity: number): number[] {
    return Array.from({ length: capacity }, (_, i) => i);
  }
}
