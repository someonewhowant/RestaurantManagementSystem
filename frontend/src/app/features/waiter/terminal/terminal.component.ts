import { Component, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MenuService, MenuCategory, Dish } from '../../../core/services/menu.service';
import { OrderService } from '../../../core/services/order.service';
import { BudgetService } from '../../../core/services/budget.service';
import { TablesService } from '../../../core/services/tables.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { StaffService } from '../../../core/services/staff.service';
import { UiButtonComponent } from '../../../core/ui/button/button.component';

@Component({
  selector: 'app-waiter-terminal',
  standalone: true,
  imports: [CommonModule, UiButtonComponent, RouterLink],
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
  
  public tableId = signal<string | null>(null);
  public selectedCategory = signal<MenuCategory>('Популярное');

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

  public displayedDishes = computed(() => {
    return this.menuService.getDishesByCategory(this.selectedCategory());
  });

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
    return tid ? this.orderService.getOrderForTable(tid).items : [];
  });

  public orderTotal = computed(() => {
    const tid = this.tableId();
    return tid ? this.orderService.getTotal(tid) : 0;
  });

  payOrder() {
    const tid = this.tableId();
    if (!tid) return;

    const total = this.orderTotal();
    
    // 1. Фиксируем выручку в бюджете
    const wName = this.waiterName();
    const items = this.currentOrderItems().map(item => ({ dishId: item.dish.id, quantity: item.quantity }));
    this.budgetService.addTransaction({
      date: new Date().toISOString(),
      amount: total,
      type: 'Доход',
      category: 'Оплата заказа',
      description: `Оплата заказа столика ID: ${tid}` + (wName ? `. Официант: ${wName}` : ''),
      items
    });

    // 2. Очищаем корзину
    this.orderService.clearOrder(tid);

    // 3. Освобождаем стол на Карте столов
    this.tablesService.changeStatus(tid, 'Свободен');

    // 4. Возвращаемся в зал
    this.router.navigate(['/waiter/tables']);
  }

  sendToKitchen() {
    const tid = this.tableId();
    if (tid) {
      // Ищем блюда в статусе 'new', чтобы списать их ингредиенты
      const order = this.orderService.orders()[tid];
      if (order) {
        const newItems = order.items.filter(i => i.status === 'new');
        this.inventoryService.consumeForOrderItems(newItems);
      }

      this.orderService.sendToKitchen(tid);
      this.tablesService.changeStatus(tid, 'Ожидает блюда');
    }
  }
}
