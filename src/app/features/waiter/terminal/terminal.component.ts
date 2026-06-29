import { Component, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MenuService, MenuCategory, Dish } from '../../../core/services/menu.service';
import { OrderService } from '../../../core/services/order.service';
import { BudgetService } from '../../../core/services/budget.service';
import { TablesService } from '../../../core/services/tables.service';
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
  
  public tableId = signal<string | null>(null);
  public selectedCategory = signal<MenuCategory>('Популярное');

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
    this.budgetService.addTransaction({
      date: new Date().toISOString(),
      amount: total,
      type: 'Доход',
      category: 'Оплата заказа',
      description: `Оплата заказа столика ID: ${tid}`
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
      this.orderService.sendToKitchen(tid);
      this.tablesService.changeStatus(tid, 'Ожидает блюда');
    }
  }
}
