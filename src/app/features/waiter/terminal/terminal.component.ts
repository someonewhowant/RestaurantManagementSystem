import { Component, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MenuService, MenuCategory, Dish } from '../../../core/services/menu.service';
import { OrderService } from '../../../core/services/order.service';
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
  public menuService = inject(MenuService);
  public orderService = inject(OrderService);
  
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
}
