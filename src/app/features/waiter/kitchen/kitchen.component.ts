import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../core/services/order.service';
import { TablesService } from '../../../core/services/tables.service';
import { UiBadgeComponent } from '../../../core/ui/badge/badge.component';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-kitchen',
  standalone: true,
  imports: [CommonModule, UiBadgeComponent, UiButtonComponent],
  templateUrl: './kitchen.component.html',
  styleUrl: './kitchen.component.scss'
})
export class KitchenComponent {
  public orderService = inject(OrderService);
  public tablesService = inject(TablesService);

  public activeItems = computed(() => {
    const list: any[] = [];
    const orders = this.orderService.orders();
    for (const [tableId, order] of Object.entries(orders)) {
      for (const item of order.items) {
        if (item.status === 'cooking' || item.status === 'ready') {
          list.push({ tableId, item });
        }
      }
    }
    return list;
  });

  markAsReady(tableId: string, dishId: string) {
    this.orderService.updateItemStatus(tableId, dishId, 'ready');
  }

  markAsServed(tableId: string, dishId: string) {
    this.orderService.updateItemStatus(tableId, dishId, 'served');
    
    // Check if all items for this table are served or new
    // If there are no 'cooking' or 'ready' items, change table status back to 'Занят'
    const orders = this.orderService.orders();
    const order = orders[tableId];
    if (order) {
      const hasCooking = order.items.some(i => i.status === 'cooking' || i.status === 'ready');
      if (!hasCooking) {
        this.tablesService.changeStatus(tableId, 'Занят');
      }
    }
  }
}
