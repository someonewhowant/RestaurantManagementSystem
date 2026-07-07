import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../../core/services/order.service';
import { TablesService } from '../../../core/services/tables.service';
import { InventoryService } from '../../../core/services/inventory.service';
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

  public inventoryService = inject(InventoryService);

  public expandedItems = signal<Set<string>>(new Set());
  
  public currentFilter = signal<'all' | 'cooking' | 'ready'>('all');

  private intervalId: any;

  ngOnInit() {
    this.orderService.loadKitchenOrders();
    this.intervalId = setInterval(() => {
      this.orderService.loadKitchenOrders();
    }, 5000);
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  public activeItems = computed(() => {
    const list: any[] = [];
    const orders = this.orderService.orders();
    const filter = this.currentFilter();

    for (const [tableId, order] of Object.entries(orders)) {
      for (const item of order.items) {
        if (item.status === 'cooking' || item.status === 'ready') {
          if (filter === 'all' || filter === item.status) {
            list.push({ 
              tableId, 
              item, 
              key: item.id || `${tableId}-${item.dish.id}-${Math.random()}`
            });
          }
        }
      }
    }
    
    // Sort by status to prioritize 'ready' items or group them
    list.sort((a, b) => {
      if (a.item.status === 'ready' && b.item.status !== 'ready') return -1;
      if (a.item.status !== 'ready' && b.item.status === 'ready') return 1;
      return 0;
    });

    return list;
  });

  setFilter(filter: 'all' | 'cooking' | 'ready') {
    this.currentFilter.set(filter);
  }

  toggleDetails(key: string) {
    const current = new Set(this.expandedItems());
    if (current.has(key)) {
      current.delete(key);
    } else {
      current.add(key);
    }
    this.expandedItems.set(current);
  }

  getIngredientName(id: string): string {
    const item = this.inventoryService.items().find(i => i.id === id);
    return item ? item.name : 'Неизвестно';
  }

  getIngredientUnit(id: string): string {
    const item = this.inventoryService.items().find(i => i.id === id);
    return item ? item.unit : '';
  }

  markAsReady(tableId: string, itemId: string) {
    this.orderService.updateItemStatus(tableId, itemId, 'ready');
  }

  markAsServed(tableId: string, itemId: string) {
    this.orderService.updateItemStatus(tableId, itemId, 'served');
    
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
