import { Component, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuService, MenuCategory, Dish } from '../../../core/services/menu.service';
import { OrderService } from '../../../core/services/order.service';
import { BudgetService } from '../../../core/services/budget.service';
import { TablesService } from '../../../core/services/tables.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { StaffService } from '../../../core/services/staff.service';
import { SettingsService } from '../../../core/services/settings.service';
import { ToastService } from '../../../core/ui/toast/toast.service';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-waiter-terminal',
  standalone: true,
  imports: [CommonModule, FormsModule, UiButtonComponent, RouterLink],
  templateUrl: './terminal.component.html',
  styleUrl: './terminal.component.scss'
})
export class WaiterTerminalComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  public menuService = inject(MenuService);
  public orderService = inject(OrderService);
  public budgetService = inject(BudgetService);
  public tablesService = inject(TablesService);
  public inventoryService = inject(InventoryService);
  public staffService = inject(StaffService);
  public settingsService = inject(SettingsService);
  public toastService = inject(ToastService);
  
  public tableId = signal<string | null>(null);
  public selectedCategory = signal<MenuCategory>('Популярное');

  public searchQuery = signal<string>('');

  public displayedDishes = computed(() => {
    const category = this.selectedCategory();
    const query = this.searchQuery().toLowerCase().trim();
    let items = this.menuService.menu();

    if (query) {
      items = items.filter((d: Dish) => 
        d.name.toLowerCase().includes(query)
      );
    } else {
      if (category === 'Популярное') {
        items = items.slice(0, 4);
      } else {
        items = items.filter((d: Dish) => d.category === category);
      }
    }
    return items;
  });

  public tableInfo = computed(() => {
    const tid = this.tableId();
    if (!tid) return null;
    return this.tablesService.tables().find(t => t.id === tid) || null;
  });

  public waiterName = computed(() => {
    const table = this.tableInfo();
    if (!table || !table.waiterId) return null;
    const emp = this.staffService.staff().find(e => e.id === table.waiterId);
    return emp ? emp.name : null;
  });

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.tableId.set(params['tableId'] || null);
    });
  }

  selectCategory(category: MenuCategory) {
    this.selectedCategory.set(category);
  }

  addToCart(dish: Dish) {
    const tid = this.tableId();
    if (tid) {
      this.orderService.addItem(tid, dish);
    }
  }

  removeFromCart(dishId: string) {
    const tid = this.tableId();
    if (tid) {
      this.orderService.removeItem(tid, dishId);
    }
  }

  public currentOrderItems = computed(() => {
    const tid = this.tableId();
    const items = tid ? this.orderService.getOrderForTable(tid).items : [];
    
    // Filter out cancelled items
    const activeItems = items.filter(i => i.status !== 'cancelled');

    const groups = new Map<string, any>();
    
    activeItems.forEach(item => {
      const key = `${item.dish.id}-${item.status}`;
      if (groups.has(key)) {
        groups.get(key).quantity += 1;
      } else {
        groups.set(key, {
          key: key,
          dish: item.dish,
          status: item.status,
          quantity: 1
        });
      }
    });

    return Array.from(groups.values());
  });

  public orderTotal = computed(() => {
    const tid = this.tableId();
    return tid ? this.orderService.getTotal(tid) : 0;
  });

  public canPay = computed(() => {
    const items = this.currentOrderItems();
    if (items.length === 0) return false;
    // Оплата возможна только если все позиции выданы
    return items.every(group => group.status === 'served');
  });

  public canSendToKitchen = computed(() => {
    const items = this.currentOrderItems();
    return items.some(group => group.status === 'new');
  });

  payOrder() {
    const tid = this.tableId();
    if (!tid) return;

    // 1. Закрываем заказ (бэкенд сам фиксирует выручку и списывает ингредиенты)
    this.orderService.closeOrder(tid).subscribe({
      next: () => {
        this.toastService.success(`Заказ #${tid} успешно оплачен`);
        // 2. Освобождаем стол на Карте столов (теперь это делает бэкенд автоматически)
        // 3. Возвращаемся в зал
        this.router.navigate(['/waiter/tables']);
      },
      error: (err: any) => {
        console.error('Ошибка при оплате заказа:', err);
        if (err.message === 'Order ID is missing') {
          this.toastService.error('Ошибка: Заказ не найден или еще не отправлен на сервер. Пожалуйста, обновите страницу.', 5000);
        } else {
          this.toastService.error('Не удалось провести оплату. Проверьте наличие ингредиентов на складе или статус заказа.', 5000);
        }
      }
    });
  }

  sendToKitchen() {
    const tid = this.tableId();
    if (tid) {
      this.orderService.sendToKitchen(tid);
      this.tablesService.changeStatus(tid, 'Ожидает блюда');
    }
  }
}
