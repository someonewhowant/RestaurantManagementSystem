import { Component, inject } from '@angular/core';
import { TablesService, Table, TableStatus } from '../../../core/services/tables.service';
import { UiCardComponent } from '../../../core/ui/card/card.component';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';

@Component({
  selector: 'app-waiter-tables',
  standalone: true,
  imports: [UiCardComponent, UiBadgeComponent],
  templateUrl: './tables.component.html',
  styleUrl: './tables.component.scss'
})
export class WaiterTablesComponent {
  public tablesService = inject(TablesService);

  onTableClick(table: Table) {
    // Временная логика для демонстрации реактивности.
    // Позже клик по столу будет открывать его заказ.
    const statuses: TableStatus[] = ['Свободен', 'Занят', 'Ожидает блюда', 'Оплата'];
    const currentIndex = statuses.indexOf(table.status);
    const nextStatus = statuses[(currentIndex + 1) % statuses.length];
    this.tablesService.changeStatus(table.id, nextStatus);
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
