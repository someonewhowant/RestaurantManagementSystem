import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TablesService, Table, TableStatus } from '../../../core/services/tables.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiModalComponent } from '../../../core/ui/modal/modal.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-waiter-tables',
  standalone: true,
  imports: [UiBadgeComponent, UiModalComponent, UiButtonComponent],
  templateUrl: './tables.component.html',
  styleUrl: './tables.component.scss'
})
export class WaiterTablesComponent {
  public tablesService = inject(TablesService);
  private router = inject(Router);
  
  public selectedTable = signal<Table | null>(null);

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
      this.tablesService.changeStatus(table.id, newStatus);
      // Обновляем локальный стейт модалки
      this.selectedTable.set({ ...table, status: newStatus });
    }
  }

  goToOrder() {
    const table = this.selectedTable();
    if (table) {
      this.router.navigate(['/waiter/terminal'], { queryParams: { tableId: table.id } });
      this.closeModal();
    }
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
